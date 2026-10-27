package top.egon.cola.archetype.source.agent.infrastructure.knowledge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.agent.common.knowledge.KnowledgeIngestChannel;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeDocumentBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.service.KnowledgeIngestPublisher;
import top.egon.cola.component.outbox.api.OutboxMessage;
import top.egon.cola.component.outbox.api.TransactionalOutbox;

import java.util.LinkedHashMap;
import java.util.Map;

@Service("knowledgeIngestPublisher")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KnowledgeIngestOutboxPublisher implements KnowledgeIngestPublisher {

    @Qualifier("transactionalOutbox")
    private final TransactionalOutbox transactionalOutbox;

    @Override
    public void enqueue(KnowledgeDocumentBO document, String traceId, Map<String, String> auditHeaders) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put(KnowledgeIngestChannel.SCHEMA_VERSION_FIELD, KnowledgeIngestChannel.SCHEMA_VERSION);
        payload.put(KnowledgeIngestChannel.DOCUMENT_ID_FIELD, document.documentId());
        payload.put(KnowledgeIngestChannel.TENANT_ID_FIELD, document.tenantId());
        transactionalOutbox.enqueue(OutboxMessage.builder()
                .channel(KnowledgeIngestChannel.NAME)
                .destination(String.valueOf(document.documentId()))
                .schemaVersion(KnowledgeIngestChannel.SCHEMA_VERSION)
                .payload(Map.copyOf(payload))
                .headers(auditHeaders)
                .traceId(traceId)
                .build());
    }
}
