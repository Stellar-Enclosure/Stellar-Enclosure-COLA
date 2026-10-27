package top.egon.cola.archetype.source.agent.application.knowledge.manage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.command.CreateKnowledgeBaseCommand;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.command.UpdateKnowledgeBaseCommand;
import top.egon.cola.archetype.source.agent.application.knowledge.config.KnowledgeRuntimeProperties;
import top.egon.cola.archetype.source.agent.application.knowledge.manage.KnowledgeBaseManage;
import top.egon.cola.archetype.source.agent.application.knowledge.service.KnowledgeRemovalService;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeBaseBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeBaseRepository;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeDocumentRepository;
import top.egon.cola.component.common.core.pojo.PageResultRecord;
import top.egon.cola.component.common.core.validation.ValidationUtils;
import top.egon.cola.archetype.source.agent.application.knowledge.executor.command.CreateKnowledgeBaseCmdExe;
import top.egon.cola.archetype.source.agent.application.knowledge.executor.query.GetKnowledgeBaseQueryExe;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.query.GetKnowledgeBaseQuery;
import top.egon.cola.archetype.source.agent.application.knowledge.executor.query.PageKnowledgeBasesQueryExe;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.query.PageKnowledgeBasesQuery;
import top.egon.cola.archetype.source.agent.application.knowledge.executor.command.UpdateKnowledgeBaseCmdExe;
import top.egon.cola.archetype.source.agent.application.knowledge.executor.query.CountKnowledgeBaseDocumentsQueryExe;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.query.CountKnowledgeBaseDocumentsQuery;
import top.egon.cola.archetype.source.agent.application.knowledge.executor.command.DeleteKnowledgeBaseCmdExe;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.command.DeleteKnowledgeBaseCommand;

/**
 * Knowledge base lifecycle: create, read, edit and the cascading delete.
 *
 * <p>The indexing configuration is frozen at creation: no later use case accepts it, and the update
 * path rejects a request that carries one instead of dropping it, so a caller cannot believe a
 * change took effect.
 *
 * <p>The delete is the one use case that spans storage, the vector store and the database. Stored
 * files go first, outside any transaction, because deleting them inside one would hold a database
 * transaction open across slow file I/O; the database part — chunk removal, document and base
 * soft deletion — then commits as one unit.
 */
@Service("knowledgeBaseManage")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KnowledgeBaseManageImpl implements KnowledgeBaseManage {

    private final @Qualifier("createKnowledgeBaseCmdExe") CreateKnowledgeBaseCmdExe createKnowledgeBaseCmdExe;

    private final @Qualifier("getKnowledgeBaseQueryExe") GetKnowledgeBaseQueryExe getKnowledgeBaseQueryExe;

    private final @Qualifier("pageKnowledgeBasesQueryExe") PageKnowledgeBasesQueryExe pageKnowledgeBasesQueryExe;

    private final @Qualifier("updateKnowledgeBaseCmdExe") UpdateKnowledgeBaseCmdExe updateKnowledgeBaseCmdExe;

    private final @Qualifier("countKnowledgeBaseDocumentsQueryExe") CountKnowledgeBaseDocumentsQueryExe countKnowledgeBaseDocumentsQueryExe;

    private final @Qualifier("deleteKnowledgeBaseCmdExe") DeleteKnowledgeBaseCmdExe deleteKnowledgeBaseCmdExe;

    public KnowledgeBaseManageImpl(KnowledgeBaseRepository knowledgeBaseRepository,
            KnowledgeDocumentRepository documentRepository,
            KnowledgeRemovalService removalService,
            KnowledgeRuntimeProperties properties,
            ValidationUtils validationUtils) {
        this(new CreateKnowledgeBaseCmdExe(knowledgeBaseRepository, validationUtils),
                new GetKnowledgeBaseQueryExe(knowledgeBaseRepository),
                new PageKnowledgeBasesQueryExe(knowledgeBaseRepository),
                new UpdateKnowledgeBaseCmdExe(knowledgeBaseRepository, validationUtils),
                new CountKnowledgeBaseDocumentsQueryExe(knowledgeBaseRepository, documentRepository),
                new DeleteKnowledgeBaseCmdExe(knowledgeBaseRepository, documentRepository, removalService, properties));
    }

    @Override
    public KnowledgeBaseBO create(CreateKnowledgeBaseCommand command) {
        return createKnowledgeBaseCmdExe.execute(command);
    }

    @Override
    public KnowledgeBaseBO get(Long knowledgeBaseId) {
        return getKnowledgeBaseQueryExe.execute(new GetKnowledgeBaseQuery(knowledgeBaseId));
    }

    @Override
    public PageResultRecord<KnowledgeBaseBO> page(int page, int size, String keyword, String embeddingModel) {
        return pageKnowledgeBasesQueryExe.execute(new PageKnowledgeBasesQuery(page, size, keyword, embeddingModel));
    }

    @Override
    public KnowledgeBaseBO update(UpdateKnowledgeBaseCommand command) {
        return updateKnowledgeBaseCmdExe.execute(command);
    }

    @Override
    public long documentCount(Long knowledgeBaseId) {
        return countKnowledgeBaseDocumentsQueryExe.execute(new CountKnowledgeBaseDocumentsQuery(knowledgeBaseId));
    }

    @Override
    public void delete(Long knowledgeBaseId) {
        deleteKnowledgeBaseCmdExe.execute(new DeleteKnowledgeBaseCommand(knowledgeBaseId));
    }

}
