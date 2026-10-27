package top.egon.cola.archetype.source.agent.domain.knowledge.service;

import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeDocumentBO;

import java.util.Map;

/** Announces an ingest in the caller's transaction. */
public interface KnowledgeIngestPublisher {

    void enqueue(KnowledgeDocumentBO document, String traceId, Map<String, String> auditHeaders);
}
