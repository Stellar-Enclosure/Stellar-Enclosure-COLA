package top.egon.cola.archetype.source.agent.infrastructure.knowledge;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.egon.cola.archetype.source.agent.common.knowledge.KnowledgeIngestChannel;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.DocumentIngestStatusEnum;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeDocumentBO;
import top.egon.cola.archetype.source.agent.infrastructure.knowledge.service.KnowledgeIngestOutboxPublisher;
import top.egon.cola.component.outbox.api.OutboxMessage;
import top.egon.cola.component.outbox.api.TransactionalOutbox;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

class KnowledgeIngestOutboxPublisherTest {

    @Test
    void writes_the_existing_envelope_and_propagates_outbox_failure() {
        TransactionalOutbox outbox = mock(TransactionalOutbox.class);
        KnowledgeIngestOutboxPublisher publisher = new KnowledgeIngestOutboxPublisher(outbox);
        KnowledgeDocumentBO document = new KnowledgeDocumentBO(3001L, 1001L, 2001L, "notes", "notes.txt",
                "text/plain", 5, "a".repeat(64), "LOCAL", "notes.txt", "notes",
                DocumentIngestStatusEnum.PENDING, 0, 0, null, null, null, null);
        Map<String, String> headers = Map.of("egon.auth.actor-id", "subject-1001",
                "egon.auth.actor-kind", "USER", "egon.auth.tenant-id", "1001");

        publisher.enqueue(document, "trace-1", headers);

        ArgumentCaptor<OutboxMessage> captured = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outbox).enqueue(captured.capture());
        OutboxMessage message = captured.getValue();
        assertEquals(KnowledgeIngestChannel.NAME, message.channel());
        assertEquals(KnowledgeIngestChannel.SCHEMA_VERSION, message.schemaVersion());
        assertEquals("3001", message.destination());
        assertEquals("trace-1", message.traceId());
        assertEquals(headers, message.headers());
        assertEquals(Map.of("schemaVersion", "1", "documentId", 3001L, "tenantId", 1001L), message.payload());

        doThrow(new IllegalStateException("outbox unavailable")).when(outbox).enqueue(any());
        assertThrows(IllegalStateException.class, () -> publisher.enqueue(document, "trace-1", headers));
    }
}
