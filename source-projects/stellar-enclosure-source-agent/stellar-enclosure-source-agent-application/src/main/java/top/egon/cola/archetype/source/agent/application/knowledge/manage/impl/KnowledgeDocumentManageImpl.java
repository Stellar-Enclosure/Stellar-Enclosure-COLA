package top.egon.cola.archetype.source.agent.application.knowledge.manage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.command.UploadKnowledgeDocumentCommand;
import top.egon.cola.archetype.source.agent.application.knowledge.config.KnowledgeRuntimeProperties;
import top.egon.cola.archetype.source.agent.application.knowledge.manage.KnowledgeDocumentManage;
import top.egon.cola.archetype.source.agent.application.knowledge.service.KnowledgeIngestQueueService;
import top.egon.cola.archetype.source.agent.application.knowledge.service.KnowledgeRemovalService;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.DocumentIngestStatusEnum;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeDocumentBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeBaseRepository;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeDocumentRepository;
import top.egon.cola.component.common.core.pojo.PageResultRecord;
import top.egon.cola.component.common.core.validation.ValidationUtils;
import top.egon.cola.component.rag.api.RagExtractionService;
import top.egon.cola.component.rag.extract.RagDocumentExtractorRegistry;
import top.egon.cola.component.rag.storage.RagDocumentStorage;
import top.egon.cola.archetype.source.agent.application.knowledge.executor.command.UploadKnowledgeDocumentCmdExe;
import top.egon.cola.archetype.source.agent.application.knowledge.executor.query.GetKnowledgeDocumentQueryExe;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.query.GetKnowledgeDocumentQuery;
import top.egon.cola.archetype.source.agent.application.knowledge.executor.query.PageKnowledgeDocumentsQueryExe;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.query.PageKnowledgeDocumentsQuery;
import top.egon.cola.archetype.source.agent.application.knowledge.executor.command.ReingestKnowledgeDocumentCmdExe;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.command.ReingestKnowledgeDocumentCommand;
import top.egon.cola.archetype.source.agent.application.knowledge.executor.command.DeleteKnowledgeDocumentCmdExe;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.command.DeleteKnowledgeDocumentCommand;

/**
 * Document lifecycle of one knowledge base: upload, read, reprocess and delete.
 *
 * <p>The upload is the only use case with a hand-written transaction boundary, and it is deliberate
 * that the boundary is narrow. The format is routed, the original stored and the text parsed first,
 * outside any transaction: a database transaction held open across file I/O and document parsing is
 * the one way this path could exhaust the pool. The row and its queue message are then written
 * together, and only that step is atomic — the file is compensated if it fails.
 *
 * <p>The parse result is what gets stored: the text is persisted on the row, so a retried ingest
 * re-chunks what is already there instead of re-reading the original, and a reprocess never needs
 * the file at all.
 */
@Service("knowledgeDocumentManage")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KnowledgeDocumentManageImpl implements KnowledgeDocumentManage {

    private final @Qualifier("uploadKnowledgeDocumentCmdExe") UploadKnowledgeDocumentCmdExe uploadKnowledgeDocumentCmdExe;

    private final @Qualifier("getKnowledgeDocumentQueryExe") GetKnowledgeDocumentQueryExe getKnowledgeDocumentQueryExe;

    private final @Qualifier("pageKnowledgeDocumentsQueryExe") PageKnowledgeDocumentsQueryExe pageKnowledgeDocumentsQueryExe;

    private final @Qualifier("reingestKnowledgeDocumentCmdExe") ReingestKnowledgeDocumentCmdExe reingestKnowledgeDocumentCmdExe;

    private final @Qualifier("deleteKnowledgeDocumentCmdExe") DeleteKnowledgeDocumentCmdExe deleteKnowledgeDocumentCmdExe;

    public KnowledgeDocumentManageImpl(KnowledgeBaseRepository knowledgeBaseRepository,
            KnowledgeDocumentRepository documentRepository,
            KnowledgeIngestQueueService ingestQueueService,
            KnowledgeRemovalService removalService,
            RagDocumentStorage documentStorage,
            RagExtractionService ragExtractionService,
            RagDocumentExtractorRegistry extractorRegistry,
            KnowledgeRuntimeProperties properties,
            ValidationUtils validationUtils) {
        this(new UploadKnowledgeDocumentCmdExe(knowledgeBaseRepository, documentRepository, ingestQueueService, removalService, documentStorage, ragExtractionService, extractorRegistry, properties, validationUtils),
                new GetKnowledgeDocumentQueryExe(documentRepository),
                new PageKnowledgeDocumentsQueryExe(knowledgeBaseRepository, documentRepository),
                new ReingestKnowledgeDocumentCmdExe(documentRepository, ingestQueueService),
                new DeleteKnowledgeDocumentCmdExe(documentRepository, removalService));
    }

    @Override
    public KnowledgeDocumentBO upload(UploadKnowledgeDocumentCommand command) {
        return uploadKnowledgeDocumentCmdExe.execute(command);
    }

    @Override
    public KnowledgeDocumentBO get(Long documentId) {
        return getKnowledgeDocumentQueryExe.execute(new GetKnowledgeDocumentQuery(documentId));
    }

    @Override
    public PageResultRecord<KnowledgeDocumentBO> page(Long knowledgeBaseId, int page, int size,
                                                      DocumentIngestStatusEnum status, String keyword) {
        return pageKnowledgeDocumentsQueryExe.execute(new PageKnowledgeDocumentsQuery(knowledgeBaseId, page, size, status, keyword));
    }

    @Override
    public KnowledgeDocumentBO reingest(Long documentId) {
        return reingestKnowledgeDocumentCmdExe.execute(new ReingestKnowledgeDocumentCommand(documentId));
    }

    @Override
    public void delete(Long documentId) {
        deleteKnowledgeDocumentCmdExe.execute(new DeleteKnowledgeDocumentCommand(documentId));
    }

}
