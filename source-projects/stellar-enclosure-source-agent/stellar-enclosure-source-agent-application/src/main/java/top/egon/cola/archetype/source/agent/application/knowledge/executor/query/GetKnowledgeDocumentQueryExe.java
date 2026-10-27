package top.egon.cola.archetype.source.agent.application.knowledge.executor.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.agent.common.exception.KnowledgeApplicationException;
import top.egon.cola.archetype.source.agent.common.error.KnowledgeErrorCodeEnum;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeDocumentBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeDocumentRepository;
import java.util.Objects;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.query.GetKnowledgeDocumentQuery;

@Service("getKnowledgeDocumentQueryExe")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GetKnowledgeDocumentQueryExe {

    @Qualifier("knowledgeDocumentRepository")
    private final KnowledgeDocumentRepository documentRepository;

    public KnowledgeDocumentBO execute(GetKnowledgeDocumentQuery query) {
        Long documentId = query.getDocumentId();
        return required(documentId, null);
    }

    private KnowledgeDocumentBO required(Long documentId, String traceId) {
        Objects.requireNonNull(documentId, "documentId must not be null");
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new KnowledgeApplicationException(
                        KnowledgeErrorCodeEnum.KNOWLEDGE_DOCUMENT_NOT_FOUND, traceId));
    }
}
