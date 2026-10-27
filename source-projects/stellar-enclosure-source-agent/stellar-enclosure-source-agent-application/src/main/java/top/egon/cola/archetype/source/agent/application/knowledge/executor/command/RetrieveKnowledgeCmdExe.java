package top.egon.cola.archetype.source.agent.application.knowledge.executor.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.command.RetrieveKnowledgeCommand;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeBaseBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeRetrievalBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeRetrievedChunkBO;
import java.util.List;
import top.egon.cola.archetype.source.agent.application.knowledge.service.KnowledgeRetrievalService;

@Service("retrieveKnowledgeCmdExe")
@Slf4j(topic = "top.egon.cola.archetype.source.agent.application.knowledge.manage.impl.KnowledgeQaManageImpl")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RetrieveKnowledgeCmdExe {

    @Qualifier("knowledgeRetrievalService")
    private final KnowledgeRetrievalService retrievalService;

    public KnowledgeRetrievalBO execute(RetrieveKnowledgeCommand command) {
        retrievalService.validate(command, command == null ? null : command.traceId());
        String traceId = command.traceId();
        KnowledgeBaseBO base = retrievalService.requiredBase(command.knowledgeBaseId(), traceId);
        List<KnowledgeRetrievedChunkBO> matched = retrievalService.search(base, command.query(), command.topK(),
                command.effectiveSimilarityThreshold(), traceId);
        log.info("knowledge retrieval knowledgeBaseId={} logicalModelName={} topK={} threshold={} matched={}"
                        + " outcome=OK", base.knowledgeBaseId(), base.embeddingModel(), command.topK(),
                command.effectiveSimilarityThreshold(), matched.size());
        return new KnowledgeRetrievalBO(base.embeddingModel(), matched);
    }

}
