package top.egon.cola.archetype.source.agent.application.knowledge.executor.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.agent.application.knowledge.config.KnowledgeRuntimeProperties;
import top.egon.cola.archetype.source.agent.common.exception.KnowledgeApplicationException;
import top.egon.cola.archetype.source.agent.application.knowledge.service.KnowledgeRemovalService;
import top.egon.cola.archetype.source.agent.common.error.KnowledgeErrorCodeEnum;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.DocumentIngestStatusEnum;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeBaseBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeDocumentBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeBaseRepository;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeDocumentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.command.DeleteKnowledgeBaseCommand;

@Service("deleteKnowledgeBaseCmdExe")
@Slf4j(topic = "top.egon.cola.archetype.source.agent.application.knowledge.manage.impl.KnowledgeBaseManageImpl")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DeleteKnowledgeBaseCmdExe {

    @Qualifier("knowledgeBaseRepository")
    private final KnowledgeBaseRepository knowledgeBaseRepository;

    @Qualifier("knowledgeDocumentRepository")
    private final KnowledgeDocumentRepository documentRepository;

    @Qualifier("knowledgeRemovalService")
    private final KnowledgeRemovalService removalService;

    @Qualifier("knowledgeRuntimeProperties")
    private final KnowledgeRuntimeProperties properties;

    public void execute(DeleteKnowledgeBaseCommand command) {
        Long knowledgeBaseId = command.getKnowledgeBaseId();
        Objects.requireNonNull(knowledgeBaseId, "knowledgeBaseId must not be null");
        required(knowledgeBaseId, null);
        long inFlight = countInFlight(knowledgeBaseId);
        if (inFlight > 0) {
            // A delivery that is already running cannot be recalled, so the base waits for it
            // rather than letting the cascade race a chunk write.
            log.info("knowledge base delete knowledgeBaseId={} outcome=REJECTED reason=documents_in_progress count={}",
                    knowledgeBaseId, inFlight);
            throw new KnowledgeApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_BASE_BUSY, null);
        }
        List<KnowledgeDocumentBO> documents = allDocuments(knowledgeBaseId);
        // Files first, outside the transaction below: deleting them inside one would hold a database
        // transaction open across slow file I/O. The deletion is idempotent by contract, so a
        // transaction that still fails leaves the files gone and a retry finds nothing to delete.
        documents.forEach(removalService::deleteStoredFile);
        try {
            removalService.removeBase(String.valueOf(knowledgeBaseId), knowledgeBaseId,
                    documents.stream().map(KnowledgeDocumentBO::documentId).toList());
        } catch (RuntimeException failure) {
            // The transaction rolled both the chunks and the rows back, so the base is unchanged and
            // a retry is safe; the caller is told the cascade failed rather than that it succeeded.
            log.warn("knowledge base delete knowledgeBaseId={} documentCount={} outcome=FAILED code={}",
                    knowledgeBaseId, documents.size(), KnowledgeErrorCodeEnum.KNOWLEDGE_INTERNAL_ERROR.code());
            throw new KnowledgeApplicationException(
                    KnowledgeErrorCodeEnum.KNOWLEDGE_INTERNAL_ERROR, null, failure);
        }
        log.info("knowledge base deleted knowledgeBaseId={} documentCount={} outcome=DELETED",
                knowledgeBaseId, documents.size());
    }

    private KnowledgeBaseBO required(Long knowledgeBaseId, String traceId) {
        return knowledgeBaseRepository.findById(knowledgeBaseId)
                .orElseThrow(() -> new KnowledgeApplicationException(
                        KnowledgeErrorCodeEnum.KNOWLEDGE_BASE_NOT_FOUND, traceId));
    }

    /** Non-terminal documents, counted per status so the state machine stays the single source. */
    private long countInFlight(Long knowledgeBaseId) {
        long inFlight = 0L;
        for (DocumentIngestStatusEnum status : DocumentIngestStatusEnum.values()) {
            if (!status.isTerminal()) {
                inFlight += documentRepository.count(knowledgeBaseId, status, null);
            }
        }
        return inFlight;
    }

    /**
     * Every document of the base, not just the first page: the per-base cap bounds how many there
     * can be, but a lowered cap must not leave the surplus behind on a delete.
     */
    private List<KnowledgeDocumentBO> allDocuments(Long knowledgeBaseId) {
        int pageSize = properties.runtime().maxDocumentsPerBase();
        List<KnowledgeDocumentBO> documents = new ArrayList<>();
        List<KnowledgeDocumentBO> batch;
        do {
            batch = documentRepository.page(knowledgeBaseId, documents.size(), pageSize, null, null);
            documents.addAll(batch);
        } while (batch.size() == pageSize);
        return documents;
    }
}
