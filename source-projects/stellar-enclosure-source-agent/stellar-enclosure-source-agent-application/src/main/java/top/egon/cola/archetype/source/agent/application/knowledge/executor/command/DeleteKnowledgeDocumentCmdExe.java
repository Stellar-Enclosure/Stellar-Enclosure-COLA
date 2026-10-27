package top.egon.cola.archetype.source.agent.application.knowledge.executor.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.agent.common.exception.KnowledgeApplicationException;
import top.egon.cola.archetype.source.agent.application.knowledge.service.KnowledgeRemovalService;
import top.egon.cola.archetype.source.agent.common.error.KnowledgeErrorCodeEnum;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeDocumentBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeDocumentRepository;
import java.util.Objects;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.command.DeleteKnowledgeDocumentCommand;

@Service("deleteKnowledgeDocumentCmdExe")
@Slf4j(topic = "top.egon.cola.archetype.source.agent.application.knowledge.manage.impl.KnowledgeDocumentManageImpl")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DeleteKnowledgeDocumentCmdExe {

    @Qualifier("knowledgeDocumentRepository")
    private final KnowledgeDocumentRepository documentRepository;

    @Qualifier("knowledgeRemovalService")
    private final KnowledgeRemovalService removalService;

    public void execute(DeleteKnowledgeDocumentCommand command) {
        Long documentId = command.getDocumentId();
        KnowledgeDocumentBO document = required(documentId, null);
        removalService.deleteStoredFile(document);
        try {
            removalService.removeDocument(String.valueOf(document.knowledgeBaseId()), document.documentId());
        } catch (RuntimeException failure) {
            // Chunks and row went back together, so the document is unchanged and still retrievable.
            log.warn("knowledge document delete documentId={} knowledgeBaseId={} outcome=FAILED code={}",
                    document.documentId(), document.knowledgeBaseId(),
                    KnowledgeErrorCodeEnum.KNOWLEDGE_INTERNAL_ERROR.code());
            throw new KnowledgeApplicationException(
                    KnowledgeErrorCodeEnum.KNOWLEDGE_INTERNAL_ERROR, null, failure);
        }
        log.info("knowledge document deleted documentId={} knowledgeBaseId={}",
                document.documentId(), document.knowledgeBaseId());
    }

    private KnowledgeDocumentBO required(Long documentId, String traceId) {
        Objects.requireNonNull(documentId, "documentId must not be null");
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new KnowledgeApplicationException(
                        KnowledgeErrorCodeEnum.KNOWLEDGE_DOCUMENT_NOT_FOUND, traceId));
    }
}
