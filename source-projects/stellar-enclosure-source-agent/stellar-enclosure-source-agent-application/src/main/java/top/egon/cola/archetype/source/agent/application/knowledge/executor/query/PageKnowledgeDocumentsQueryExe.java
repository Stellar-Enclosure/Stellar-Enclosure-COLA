package top.egon.cola.archetype.source.agent.application.knowledge.executor.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.agent.common.exception.KnowledgeApplicationException;
import top.egon.cola.archetype.source.agent.common.error.KnowledgeErrorCodeEnum;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.DocumentIngestStatusEnum;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeBaseBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeDocumentBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeBaseRepository;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeDocumentRepository;
import top.egon.cola.component.common.core.pojo.PageResultRecord;
import java.util.Objects;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.query.PageKnowledgeDocumentsQuery;

@Service("pageKnowledgeDocumentsQueryExe")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PageKnowledgeDocumentsQueryExe {

    private static final int MAX_PAGE_SIZE = 100;

    @Qualifier("knowledgeBaseRepository")
    private final KnowledgeBaseRepository knowledgeBaseRepository;

    @Qualifier("knowledgeDocumentRepository")
    private final KnowledgeDocumentRepository documentRepository;

    public PageResultRecord<KnowledgeDocumentBO> execute(PageKnowledgeDocumentsQuery query) {
        Long knowledgeBaseId = query.getKnowledgeBaseId();
        int page = query.getPage();
        int size = query.getSize();
        DocumentIngestStatusEnum status = query.getStatus();
        String keyword = query.getKeyword();
        requirePage(page, size);
        requiredBase(knowledgeBaseId, null);
        long total = documentRepository.count(knowledgeBaseId, status, keyword);
        return PageResultRecord.success(
                documentRepository.page(knowledgeBaseId, (page - 1) * size, size, status, keyword),
                total, page, size);
    }

    private KnowledgeBaseBO requiredBase(Long knowledgeBaseId, String traceId) {
        Objects.requireNonNull(knowledgeBaseId, "knowledgeBaseId must not be null");
        return knowledgeBaseRepository.findById(knowledgeBaseId)
                .orElseThrow(() -> new KnowledgeApplicationException(
                        KnowledgeErrorCodeEnum.KNOWLEDGE_BASE_NOT_FOUND, traceId));
    }

    private void requirePage(int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new KnowledgeApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_VALIDATION_ERROR, null);
        }
    }
}
