package top.egon.cola.archetype.source.agent.application.knowledge.manage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.command.AskKnowledgeBaseCommand;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.command.RetrieveKnowledgeCommand;
import top.egon.cola.archetype.source.agent.application.knowledge.manage.KnowledgeQaManage;
import top.egon.cola.archetype.source.agent.application.knowledge.service.KnowledgeQaCapacityService;
import top.egon.cola.archetype.source.agent.application.knowledge.service.KnowledgeRetrievalService;
import top.egon.cola.archetype.source.agent.domain.knowledge.service.KnowledgeAnswerService;
import top.egon.cola.archetype.source.agent.domain.knowledge.service.KnowledgeVectorService;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeRetrievalBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeBaseRepository;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeDocumentRepository;
import top.egon.cola.archetype.source.agent.domain.knowledge.service.KnowledgeAnswerRunService;
import top.egon.cola.archetype.source.agent.domain.knowledge.service.KnowledgeQaEventObserverService;
import top.egon.cola.component.common.core.validation.ValidationUtils;
import java.time.Clock;
import top.egon.cola.archetype.source.agent.application.knowledge.executor.command.RetrieveKnowledgeCmdExe;
import top.egon.cola.archetype.source.agent.application.knowledge.executor.command.AskKnowledgeBaseCmdExe;

/**
 * Retrieval debugging and the retrieval-backed answer stream.
 *
 * <p>Both use cases read the collection, the logical model and the tenant scope from the base and
 * the running thread, so a caller can neither ask another base's chunks nor answer from another
 * model. The retrieval is a read-only call that never reaches a chat model, which is what keeps the
 * debug path free of generation cost.
 *
 * <p>A question holds a capacity permit for as long as its generation runs, so the permit is taken
 * before the first dependency call — the retrieval embeds the question too — and released exactly
 * once: by the terminal event, by {@code cancel()} when the caller disconnects, or by the failure
 * that replaces both. Every path out of this class either releases the permit or hands it to the
 * returned handle.
 */
@Service("knowledgeQaManage")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KnowledgeQaManageImpl implements KnowledgeQaManage {

    private final @Qualifier("retrieveKnowledgeCmdExe") RetrieveKnowledgeCmdExe retrieveKnowledgeCmdExe;

    private final @Qualifier("askKnowledgeBaseCmdExe") AskKnowledgeBaseCmdExe askKnowledgeBaseCmdExe;

    public KnowledgeQaManageImpl(KnowledgeBaseRepository knowledgeBaseRepository,
            KnowledgeDocumentRepository documentRepository,
            KnowledgeVectorService vectorService,
            KnowledgeAnswerService answerService,
            KnowledgeQaCapacityService capacityService,
            ValidationUtils validationUtils,
            Clock clock) {
        this(new RetrieveKnowledgeCmdExe(new KnowledgeRetrievalService(knowledgeBaseRepository, documentRepository, vectorService, validationUtils)),
                new AskKnowledgeBaseCmdExe(new KnowledgeRetrievalService(knowledgeBaseRepository, documentRepository, vectorService, validationUtils), answerService, capacityService, clock));
    }

    @Override
    public KnowledgeRetrievalBO retrieve(RetrieveKnowledgeCommand command) {
        return retrieveKnowledgeCmdExe.execute(command);
    }

    @Override
    public KnowledgeAnswerRunService ask(AskKnowledgeBaseCommand command, KnowledgeQaEventObserverService observer) {
        return askKnowledgeBaseCmdExe.execute(command, observer);
    }

}
