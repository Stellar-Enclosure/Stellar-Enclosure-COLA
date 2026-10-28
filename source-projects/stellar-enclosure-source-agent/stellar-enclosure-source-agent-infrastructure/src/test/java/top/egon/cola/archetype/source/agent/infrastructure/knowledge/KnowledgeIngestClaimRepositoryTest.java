package top.egon.cola.archetype.source.agent.infrastructure.knowledge;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeDocumentRepository;
import top.egon.cola.archetype.source.agent.infrastructure.knowledge.dao.KnowledgeDocumentDAO;
import top.egon.cola.archetype.source.agent.infrastructure.knowledge.po.KnowledgeDocumentPO;
import top.egon.cola.archetype.source.agent.infrastructure.knowledge.service.KnowledgeDocumentRepositoryImpl;
import top.egon.cola.component.common.core.validation.ValidationUtils;
import top.egon.cola.component.common.mybatis.autoconfigure.EgonColaMybatisPlusProperties;
import top.egon.cola.component.common.mybatis.business.EgonColaTenantIdProvider;
import top.egon.cola.component.common.mybatis.model.EgonColaModelValidationUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KnowledgeIngestClaimRepositoryTest {
    private static final String GENERATION = "242ababa-20c3-4ffa-82f0-856060b06bb2";

    @Test
    void staleReplicaAndConcurrentAttemptsCannotCompleteAReclaimedGeneration() throws Exception {
        var dao = mock(KnowledgeDocumentDAO.class);
        var primary = new AtomicReference<>(new State(3, "PENDING", 0));
        var replica = new AtomicReference<>(new State(3, "PENDING", 0));
        when(dao.selectActiveById(any())).thenAnswer(call -> document(replica.get()));
        when(dao.claimIngest(any(), anyString(), anyString(), anyInt())).thenAnswer(call -> {
            KnowledgeDocumentPO change = call.getArgument(0);
            String generation = call.getArgument(1), expectedStatus = call.getArgument(2);
            int expectedAttempt = call.getArgument(3);
            synchronized (primary) {
                State state = primary.get();
                if (!GENERATION.equals(generation) || !state.status().equals(expectedStatus)
                        || state.attempt() != expectedAttempt || state.version() != change.getVersion()) return 0;
                long version = state.version() + 1;
                primary.set(new State(version, "PROCESSING", change.getAttemptCount()));
                change.setVersion(version); // the MP optimistic interceptor mutates the carrier
                return 1;
            }
        });
        when(dao.finishIngest(any(), anyString(), anyInt())).thenAnswer(call -> {
            KnowledgeDocumentPO change = call.getArgument(0);
            String generation = call.getArgument(1);
            int attempt = call.getArgument(2);
            synchronized (primary) {
                State state = primary.get();
                if (!GENERATION.equals(generation) || !state.status().equals("PROCESSING")
                        || state.attempt() != attempt || state.version() != change.getVersion()) return 0;
                primary.set(new State(state.version() + 1, change.getStatus(), attempt));
                return 1;
            }
        });
        try (var factory = Validation.buildDefaultValidatorFactory();
             var binding = EgonColaModelValidationUtils.initialize(new ValidationUtils(factory.getValidator()), new Object());
             var workers = Executors.newFixedThreadPool(2)) {
            EgonColaTenantIdProvider.initialize("tenantId");
            var repository = new KnowledgeDocumentRepositoryImpl(dao, new EgonColaMybatisPlusProperties(),
                    new ValidationUtils(factory.getValidator()), () -> "actor", Clock.systemUTC());
            var start = new CountDownLatch(1);
            var futures = List.of(workers.submit(() -> claim(repository, start)), workers.submit(() -> claim(repository, start)));
            start.countDown();
            var first = futures.get(0).get();
            var second = futures.get(1).get();
            assertEquals(1, List.of(first, second).stream().filter(value -> value.status() == KnowledgeDocumentRepository.IngestClaimStatus.ACQUIRED).count());
            var oldClaim = first.claim() == null ? second.claim() : first.claim();
            MDC.put("tenantId", "42");
            assertFalse(repository.markProcessing(2001L, 1), "the legacy API cannot mutate a generation-bound document");
            verify(dao, never()).updateState(any(), anyList());
            assertEquals(KnowledgeDocumentRepository.IngestClaimStatus.RETRY,
                    repository.claimForIngest(2001L, GENERATION, 2).status(), "a stale replica cannot advance primary state");
            replica.set(primary.get());
            var recovered = repository.claimForIngest(2001L, GENERATION, 2);
            assertEquals(KnowledgeDocumentRepository.IngestClaimStatus.ACQUIRED, recovered.status());
            assertFalse(repository.completeIngest(oldClaim, 3), "a superseded worker must not finish");
            assertTrue(repository.completeIngest(recovered.claim(), 3));
            replica.set(primary.get());
            assertEquals(KnowledgeDocumentRepository.IngestClaimStatus.ALREADY_SUCCEEDED,
                    repository.claimForIngest(2001L, GENERATION, 3).status());
            assertEquals(KnowledgeDocumentRepository.IngestClaimStatus.RETRY,
                    repository.claimForIngest(2001L, "f5f6a44f-c0d1-4c27-a46f-c02b0150d4c0", 1).status());
            MDC.put("tenantId", "43");
            assertThrows(IllegalArgumentException.class, () -> repository.completeIngest(recovered.claim(), 3));
            verify(dao, times(2)).finishIngest(any(), anyString(), anyInt());
        } finally {
            MDC.clear();
            EgonColaTenantIdProvider.initialize(EgonColaTenantIdProvider.DEFAULT_MDC_KEY);
        }
    }

    @Test
    void mapperFencesEveryClaimAndCompletionByGenerationAndVersion() throws Exception {
        String xml = Files.readString(Path.of("src/main/resources/mybatis/mapper/knowledge/KnowledgeDocumentDAO.xml"));
        for (String statement : List.of("claimIngest", "finishIngest")) {
            String body = xml.split("<update id=\"" + statement + "\">", 2)[1].split("</update>", 2)[0];
            assertTrue(body.contains("version = #{MP_OPTLOCK_VERSION_ORIGINAL}"));
            assertTrue(body.contains("ingest_generation = #{generation}"));
            assertTrue(body.contains("attempt_count = #{expectedAttempt}"));
            assertTrue(body.contains("deleted_at IS NULL"));
        }
        String legacy = xml.split("<update id=\"updateState\">", 2)[1].split("</update>", 2)[0];
        assertTrue(legacy.contains("ingest_generation IS NULL"));
    }

    @Test
    void actualMapperWritesPassTenantAndPrimaryRoutingGuards() throws Exception {
        String mapper = Files.readString(Path.of("src/main/resources/mybatis/mapper/knowledge/KnowledgeDocumentDAO.xml"));
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            EgonColaTenantIdProvider.initialize("tenantId");
            MDC.put("tenantId", "42");
            var configuration = new com.baomidou.mybatisplus.core.MybatisConfiguration();
            var source = mock(javax.sql.DataSource.class);
            configuration.setEnvironment(new org.apache.ibatis.mapping.Environment("test",
                    new org.mybatis.spring.transaction.SpringManagedTransactionFactory(), source));
            new org.apache.ibatis.builder.xml.XMLMapperBuilder(new java.io.ByteArrayInputStream(
                    mapper.getBytes(java.nio.charset.StandardCharsets.UTF_8)), configuration, "knowledge.xml",
                    configuration.getSqlFragments()).parse();
            var properties = new EgonColaMybatisPlusProperties();
            var sqlGuard = new top.egon.cola.component.common.mybatis.interceptor.EgonColaTenantIdGuardInnerInterceptor(() -> "actor", properties);
            var tenant = new com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor(
                    new top.egon.cola.component.common.mybatis.business.EgonColaTenantIdTenantLineHandler(properties));
            var route = new top.egon.cola.component.common.mybatis.routing.EgonColaTwoLevelRouteStrategy(new ValidationUtils(factory.getValidator()));
            var profile = new top.egon.cola.component.common.mybatis.routing.EgonColaRoutingProfileBO("knowledge_document",
                    top.egon.cola.component.common.mybatis.routing.EgonColaRoutingProfileBO.TableKindEnum.SINGLE,
                    "single-v1", 1, 1, java.util.Map.of(), null, null, 0x9e3779b97f4a7c15L,
                    java.util.Map.of(new top.egon.cola.component.common.mybatis.routing.EgonColaRoutingProfileBO.PartitionKeyBO(0, 0),
                            List.of(new top.egon.cola.component.common.mybatis.routing.EgonColaPhysicalTargetBO("primary", "public", "knowledge_document"))), 1, null);
            var guard = new top.egon.cola.component.common.mybatis.interceptor.EgonColaLocalWriteGuardInnerInterceptor(
                    query -> { throw new AssertionError("unexpected legacy route"); }, sqlGuard, route, java.util.Map.of("knowledge_document", profile));
            com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                    new org.apache.ibatis.builder.MapperBuilderAssistant(configuration, "knowledge.xml"), KnowledgeDocumentPO.class);
            for (String name : List.of("resetForIngest", "claimIngest", "finishIngest")) {
                var statement = configuration.getMappedStatement(KnowledgeDocumentDAO.class.getName() + "." + name);
                var entity = document(new State(3, "PROCESSING", 1));
                entity.setIngestGeneration(GENERATION);
                var parameters = new java.util.HashMap<String, Object>();
                parameters.put("et", entity);
                parameters.put("generation", GENERATION); parameters.put("expectedStatus", "PENDING");
                parameters.put("expectedAttempt", 0); parameters.put("expectedStatuses", List.of("SUCCEEDED", "DEAD"));
                new com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor()
                        .beforeUpdate(mock(org.apache.ibatis.executor.Executor.class), statement, parameters);
                assertEquals(3L, parameters.get("MP_OPTLOCK_VERSION_ORIGINAL"));
                assertEquals(4L, entity.getVersion());
                var handler = configuration.newStatementHandler(mock(org.apache.ibatis.executor.Executor.class), statement,
                        parameters, org.apache.ibatis.session.RowBounds.DEFAULT, null, statement.getBoundSql(parameters));
                var connection = mock(java.sql.Connection.class);
                sqlGuard.beforePrepare(handler, connection, null);
                tenant.beforePrepare(handler, connection, null);
                assertDoesNotThrow(() -> guard.beforePrepare(handler, connection, null), name);
                verifyNoInteractions(connection);
            }
        } finally {
            MDC.clear();
            EgonColaTenantIdProvider.initialize(EgonColaTenantIdProvider.DEFAULT_MDC_KEY);
        }
    }

    private static KnowledgeDocumentRepository.IngestClaimResult claim(KnowledgeDocumentRepository repository, CountDownLatch start) throws Exception {
        MDC.put("tenantId", "42");
        try { start.await(); return repository.claimForIngest(2001L, GENERATION, 1); }
        finally { MDC.clear(); }
    }

    private static KnowledgeDocumentPO document(State state) {
        var po = new KnowledgeDocumentPO();
        po.setId(2001L); po.setTenantId(42L); po.setVersion(state.version());
        po.setCreateUserId("actor"); po.setUpdateUserId("actor");
        po.setCreateTime(Instant.parse("2026-01-01T00:00:00Z")); po.setUpdateTime(Instant.parse("2026-01-01T00:00:00Z"));
        po.setKnowledgeBaseId(1001L).setDisplayName("notes").setSizeBytes(5L).setContentHash("a".repeat(64))
                .setStorageType("LOCAL").setStorageKey("notes.txt").setStatus(state.status()).setChunkCount(0)
                .setAttemptCount(state.attempt()).setIngestGeneration(GENERATION);
        return po;
    }

    private record State(long version, String status, int attempt) { }
}
