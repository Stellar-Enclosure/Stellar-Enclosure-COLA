package top.egon.cola.archetype.source.agent.application.knowledge.executor.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.agent.common.exception.KnowledgeApplicationException;
import top.egon.cola.archetype.source.agent.application.knowledge.service.KnowledgeIngestQueueService;
import top.egon.cola.archetype.source.agent.common.error.KnowledgeErrorCodeEnum;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeDocumentBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeDocumentRepository;
import java.util.Objects;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.command.ReingestKnowledgeDocumentCommand;

@Service("reingestKnowledgeDocumentCmdExe")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReingestKnowledgeDocumentCmdExe {

    @Qualifier("knowledgeDocumentRepository")
    private final KnowledgeDocumentRepository documentRepository;

    @Qualifier("knowledgeIngestQueueService")
    private final KnowledgeIngestQueueService ingestQueueService;

    public KnowledgeDocumentBO execute(ReingestKnowledgeDocumentCommand command) {
        Long documentId = command.getDocumentId();
        KnowledgeDocumentBO document = required(documentId, null);
        if (!document.status().isTerminal()) {
            throw new KnowledgeApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_DOCUMENT_BUSY, null);
        }
        if (document.content() == null || document.content().isBlank()) {
            // Nothing to embed: the only way out is a fresh upload, which the code tells the caller.
            throw new KnowledgeApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_CONTENT_MISSING, null);
        }
        if (!ingestQueueService.resetAndEnqueue(document, null)) {
            throw new KnowledgeApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_DOCUMENT_BUSY, null);
        }
        return required(documentId, null);
    }

    private KnowledgeDocumentBO required(Long documentId, String traceId) {
        Objects.requireNonNull(documentId, "documentId must not be null");
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new KnowledgeApplicationException(
                        KnowledgeErrorCodeEnum.KNOWLEDGE_DOCUMENT_NOT_FOUND, traceId));
    }
}
