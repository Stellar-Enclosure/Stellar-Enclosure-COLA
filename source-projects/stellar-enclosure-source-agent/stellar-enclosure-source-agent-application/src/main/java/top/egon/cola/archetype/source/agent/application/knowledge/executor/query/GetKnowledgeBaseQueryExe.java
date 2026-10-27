package top.egon.cola.archetype.source.agent.application.knowledge.executor.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.agent.common.exception.KnowledgeApplicationException;
import top.egon.cola.archetype.source.agent.common.error.KnowledgeErrorCodeEnum;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeBaseBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeBaseRepository;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.query.GetKnowledgeBaseQuery;

@Service("getKnowledgeBaseQueryExe")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GetKnowledgeBaseQueryExe {

    @Qualifier("knowledgeBaseRepository")
    private final KnowledgeBaseRepository knowledgeBaseRepository;

    public KnowledgeBaseBO execute(GetKnowledgeBaseQuery query) {
        Long knowledgeBaseId = query.getKnowledgeBaseId();
        return required(knowledgeBaseId, null);
    }

    private KnowledgeBaseBO required(Long knowledgeBaseId, String traceId) {
        return knowledgeBaseRepository.findById(knowledgeBaseId)
                .orElseThrow(() -> new KnowledgeApplicationException(
                        KnowledgeErrorCodeEnum.KNOWLEDGE_BASE_NOT_FOUND, traceId));
    }
}
