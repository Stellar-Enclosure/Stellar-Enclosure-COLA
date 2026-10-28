package top.egon.cola.archetype.source.agent.application.knowledge;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import top.egon.cola.archetype.source.agent.application.knowledge.service.KnowledgeIngestQueueService;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeDocumentBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeDocumentRepository;
import top.egon.cola.archetype.source.agent.domain.knowledge.service.KnowledgeIngestPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The actual queue must retain trusted audit facts without changing its business payload. */
class KnowledgeIngestAuditContextTest {
    @AfterEach
    void clearScope() { MDC.clear(); }

    @Test
    void missingAndConflictingActorFactsStopBeforeEitherDocumentWrite() {
        var documents = mock(KnowledgeDocumentRepository.class);
        var publisher = mock(KnowledgeIngestPublisher.class);
        var queue = new KnowledgeIngestQueueService(documents, publisher,
                new top.egon.cola.component.common.mybatis.business.EgonColaMdcUserIdProvider(
                        new top.egon.cola.component.common.mybatis.autoconfigure.EgonColaMybatisPlusProperties()));
        var document = KnowledgeDocumentBO.create(1001L, 2001L, "notes", "notes.txt", "text/plain",
                5, "a".repeat(64), "LOCAL", "notes.txt", "notes");
        MDC.put("tenantId", "1001");
        org.junit.jupiter.api.Assertions.assertThrows(top.egon.cola.archetype.source.agent.common.exception.KnowledgeApplicationException.class,
                () -> queue.storeAndEnqueue(document, "trace-1"));
        MDC.put("userId", "subject-1001"); MDC.put("egon.auth.actor-kind", "SYSTEM");
        org.junit.jupiter.api.Assertions.assertThrows(top.egon.cola.archetype.source.agent.common.exception.KnowledgeApplicationException.class,
                () -> queue.storeAndEnqueue(document, "trace-1"));
        MDC.put("egon.auth.actor-kind", "USER"); MDC.put("tenantId", "1002");
        org.junit.jupiter.api.Assertions.assertThrows(top.egon.cola.archetype.source.agent.common.exception.KnowledgeApplicationException.class,
                () -> queue.resetAndEnqueue(document, "trace-1"));
        org.mockito.Mockito.verifyNoInteractions(documents, publisher);
    }

    @Test
    void writesProtectedActorFacts() {
        var documents = mock(KnowledgeDocumentRepository.class);
        var publisher = mock(KnowledgeIngestPublisher.class);
        var queue = new KnowledgeIngestQueueService(documents, publisher,
                new top.egon.cola.component.common.mybatis.business.EgonColaMdcUserIdProvider(
                        new top.egon.cola.component.common.mybatis.autoconfigure.EgonColaMybatisPlusProperties()));
        var document = new KnowledgeDocumentBO(3001L, 1001L, 2001L, "notes", "notes.txt", "text/plain",
                5, "a".repeat(64), "LOCAL", "notes.txt", "notes",
                top.egon.cola.archetype.source.agent.domain.knowledge.model.DocumentIngestStatusEnum.PENDING,
                0, 0, null, null, null, null);
        MDC.put("tenantId", "1001");
        MDC.put("userId", "subject-1001");
        MDC.put("egon.auth.actor-kind", "USER");
        when(documents.resetForIngest(eq(document.documentId()), org.mockito.ArgumentMatchers.anyString())).thenReturn(true);
        assertTrue(queue.resetAndEnqueue(document, "trace-1"));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> headers = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> generation = ArgumentCaptor.forClass(String.class);
        verify(documents).resetForIngest(eq(document.documentId()), generation.capture());
        verify(publisher).enqueue(eq(document), eq(generation.getValue()), eq("trace-1"), headers.capture());
        assertEquals(generation.getValue(), java.util.UUID.fromString(generation.getValue()).toString());
        assertEquals(Map.of("egon.auth.actor-id", "subject-1001", "egon.auth.actor-kind", "USER",
                "egon.auth.tenant-id", "1001"), headers.getValue());
    }

    @Test
    void enqueueFailureRollsBackTheTransactionThatBoundTheGeneration() {
        var documents = mock(KnowledgeDocumentRepository.class);
        var publisher = mock(KnowledgeIngestPublisher.class);
        var target = new KnowledgeIngestQueueService(documents, publisher,
                new top.egon.cola.component.common.mybatis.business.EgonColaMdcUserIdProvider(
                        new top.egon.cola.component.common.mybatis.autoconfigure.EgonColaMybatisPlusProperties()));
        var manager = new RecordingTransactions();
        var factory = new org.springframework.aop.framework.ProxyFactory(target);
        factory.setProxyTargetClass(true);
        factory.addAdvice(new org.springframework.transaction.interceptor.TransactionInterceptor(manager,
                new org.springframework.transaction.annotation.AnnotationTransactionAttributeSource()));
        var queue = (KnowledgeIngestQueueService) factory.getProxy();
        var document = KnowledgeDocumentBO.create(1001L, 2001L, "notes", "notes.txt", "text/plain",
                5, "a".repeat(64), "LOCAL", "notes.txt", "notes");
        MDC.put("tenantId", "1001"); MDC.put("userId", "subject-1001"); MDC.put("egon.auth.actor-kind", "USER");
        when(documents.insertForIngest(eq(document), org.mockito.ArgumentMatchers.anyString())).thenReturn(document);
        org.mockito.Mockito.doThrow(new IllegalStateException("outbox unavailable")).when(publisher)
                .enqueue(eq(document), org.mockito.ArgumentMatchers.anyString(), eq("trace-1"), org.mockito.ArgumentMatchers.anyMap());
        org.junit.jupiter.api.Assertions.assertThrows(top.egon.cola.archetype.source.agent.common.exception.KnowledgeApplicationException.class,
                () -> queue.storeAndEnqueue(document, "trace-1"));
        ArgumentCaptor<String> generation = ArgumentCaptor.forClass(String.class);
        verify(documents).insertForIngest(eq(document), generation.capture());
        verify(publisher).enqueue(eq(document), eq(generation.getValue()), eq("trace-1"), org.mockito.ArgumentMatchers.anyMap());
        assertEquals(0, manager.commits);
        assertEquals(1, manager.rollbacks);
    }

    private static final class RecordingTransactions extends org.springframework.transaction.support.AbstractPlatformTransactionManager {
        private int commits;
        private int rollbacks;
        @Override protected Object doGetTransaction() { return new Object(); }
        @Override protected void doBegin(Object transaction, org.springframework.transaction.TransactionDefinition definition) { }
        @Override protected void doCommit(org.springframework.transaction.support.DefaultTransactionStatus status) { commits++; }
        @Override protected void doRollback(org.springframework.transaction.support.DefaultTransactionStatus status) { rollbacks++; }
    }
}
