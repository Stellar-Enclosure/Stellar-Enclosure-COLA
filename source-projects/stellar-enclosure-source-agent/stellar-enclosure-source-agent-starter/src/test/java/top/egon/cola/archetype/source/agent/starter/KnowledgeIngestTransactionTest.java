package top.egon.cola.archetype.source.agent.starter;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import top.egon.cola.archetype.source.agent.application.knowledge.service.KnowledgeIngestQueueService;
import top.egon.cola.archetype.source.agent.common.exception.KnowledgeApplicationException;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.DocumentIngestStatusEnum;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeDocumentBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeDocumentRepository;
import top.egon.cola.archetype.source.agent.domain.knowledge.service.KnowledgeIngestPublisher;
import top.egon.cola.archetype.source.agent.infrastructure.knowledge.service.KnowledgeIngestOutboxPublisher;
import top.egon.cola.component.common.mybatis.autoconfigure.EgonColaMybatisPlusProperties;
import top.egon.cola.component.common.mybatis.business.EgonColaMdcUserIdProvider;
import top.egon.cola.component.common.mybatis.business.EgonColaUserIdProvider;
import top.egon.cola.component.outbox.api.OutboxReceipt;
import top.egon.cola.component.outbox.api.TransactionalOutbox;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KnowledgeIngestTransactionTest {

    @Test
    void document_and_outbox_writes_share_the_transaction() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);
            jdbc.execute("CREATE TABLE documents (id BIGINT PRIMARY KEY)");
            jdbc.execute("CREATE TABLE messages (id BIGINT PRIMARY KEY)");
            KnowledgeIngestQueueService service = context.getBean(KnowledgeIngestQueueService.class);
            assertTrue(AopUtils.isAopProxy(service));

            MDC.put("tenantId", "1001");
            MDC.put("userId", "subject-1001");
            MDC.put("egon.auth.actor-kind", "USER");
            KnowledgeDocumentBO document = new KnowledgeDocumentBO(3001L, 1001L, 2001L, "notes", "notes.txt",
                    "text/plain", 5, "a".repeat(64), "LOCAL", "notes.txt", "notes",
                    DocumentIngestStatusEnum.PENDING, 0, 0, null, null, null, null);

            AtomicBoolean failAfterOutboxWrite = context.getBean(AtomicBoolean.class);
            failAfterOutboxWrite.set(true);
            assertThrows(KnowledgeApplicationException.class, () -> service.storeAndEnqueue(document, "trace-1"));
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM documents", Integer.class));
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM messages", Integer.class));

            failAfterOutboxWrite.set(false);
            service.storeAndEnqueue(document, "trace-1");
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM documents", Integer.class));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM messages", Integer.class));
        } finally {
            MDC.clear();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement
    static class TestConfig {

        @Bean
        DataSource dataSource() {
            JdbcDataSource source = new JdbcDataSource();
            source.setURL("jdbc:h2:mem:cqers_ingest_transaction;DB_CLOSE_DELAY=-1");
            return source;
        }

        @Bean
        JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        AtomicBoolean failAfterOutboxWrite() {
            return new AtomicBoolean();
        }

        @Bean
        KnowledgeDocumentRepository knowledgeDocumentRepository(JdbcTemplate jdbc) {
            KnowledgeDocumentRepository repository = mock(KnowledgeDocumentRepository.class);
            when(repository.insert(any())).thenAnswer(invocation -> {
                KnowledgeDocumentBO document = invocation.getArgument(0);
                jdbc.update("INSERT INTO documents (id) VALUES (?)", document.documentId());
                return document;
            });
            return repository;
        }

        @Bean
        TransactionalOutbox transactionalOutbox(JdbcTemplate jdbc, AtomicBoolean failAfterOutboxWrite) {
            return message -> {
                jdbc.update("INSERT INTO messages (id) VALUES (?)", 3001L);
                if (failAfterOutboxWrite.get()) {
                    throw new IllegalStateException("outbox failure after write");
                }
                return new OutboxReceipt("message-1", "key-1", true);
            };
        }

        @Bean
        KnowledgeIngestPublisher knowledgeIngestPublisher(TransactionalOutbox outbox) {
            return new KnowledgeIngestOutboxPublisher(outbox);
        }

        @Bean
        EgonColaUserIdProvider actorProvider() {
            return new EgonColaMdcUserIdProvider(new EgonColaMybatisPlusProperties());
        }

        @Bean
        KnowledgeIngestQueueService queueService(KnowledgeDocumentRepository repository,
                                                  KnowledgeIngestPublisher publisher,
                                                  EgonColaUserIdProvider actorProvider) {
            return new KnowledgeIngestQueueService(repository, publisher, actorProvider);
        }
    }
}
