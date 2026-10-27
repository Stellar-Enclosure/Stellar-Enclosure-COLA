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
import top.egon.cola.component.common.core.pojo.PageResultRecord;
import java.util.List;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.query.PageKnowledgeBasesQuery;

@Service("pageKnowledgeBasesQueryExe")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PageKnowledgeBasesQueryExe {

    @Qualifier("knowledgeBaseRepository")
    private final KnowledgeBaseRepository knowledgeBaseRepository;

    public PageResultRecord<KnowledgeBaseBO> execute(PageKnowledgeBasesQuery query) {
        int page = query.getPage();
        int size = query.getSize();
        String keyword = query.getKeyword();
        String embeddingModel = query.getEmbeddingModel();
        requirePage(page, size, null);
        long total = knowledgeBaseRepository.count(keyword, embeddingModel);
        List<KnowledgeBaseBO> records = knowledgeBaseRepository.page(offset(page, size), size, keyword, embeddingModel);
        return PageResultRecord.success(records, total, page, size);
    }

    private static int offset(int page, int size) {
        return (page - 1) * size;
    }

    private void requirePage(int page, int size, String traceId) {
        if (page < 1 || size < 1 || size > 100) {
            throw new KnowledgeApplicationException(
                    KnowledgeErrorCodeEnum.KNOWLEDGE_VALIDATION_ERROR, traceId);
        }
    }
}
