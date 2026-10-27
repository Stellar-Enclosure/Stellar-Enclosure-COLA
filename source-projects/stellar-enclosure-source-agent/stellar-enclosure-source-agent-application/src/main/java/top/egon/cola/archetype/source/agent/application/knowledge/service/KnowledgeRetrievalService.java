package top.egon.cola.archetype.source.agent.application.knowledge.service;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.agent.common.exception.KnowledgeApplicationException;
import top.egon.cola.archetype.source.agent.common.error.KnowledgeErrorCodeEnum;
import top.egon.cola.archetype.source.agent.domain.knowledge.service.KnowledgeVectorService;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeBaseBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeChunkBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeDocumentBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeRetrievedChunkBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeBaseRepository;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeDocumentRepository;
import top.egon.cola.component.common.core.validation.ValidationUtils;
import top.egon.cola.component.rag.common.exception.RagException;
import top.egon.cola.component.rag.common.exception.RagModelNotRegisteredException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service("knowledgeRetrievalService")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KnowledgeRetrievalService {

    private static final int COMPONENT_DEFAULT_TOP_K = 0;
    private static final double ACCEPT_EVERY_SCORE = 0.0d;

    @Qualifier("knowledgeBaseRepository")
    private final KnowledgeBaseRepository knowledgeBaseRepository;

    @Qualifier("knowledgeDocumentRepository")
    private final KnowledgeDocumentRepository documentRepository;

    @Qualifier("knowledgeVectorService")
    private final KnowledgeVectorService vectorService;

    @Qualifier("agentValidationUtils")
    private final ValidationUtils validationUtils;

    /**
     * Resolves the display names and applies the score floor of one retrieval.
     *
     * <p>The names are resolved in one batch rather than per chunk, which is what keeps a retrieval
     * response from forcing its caller into a second lookup per document.
     */
    public List<KnowledgeRetrievedChunkBO> search(KnowledgeBaseBO base, String query, Integer topK,
                                                   double threshold, String traceId) {
        List<KnowledgeChunkBO> chunks;
        try {
            chunks = vectorService.retrieve(String.valueOf(base.knowledgeBaseId()), base.embeddingModel(), query,
                    topK == null ? COMPONENT_DEFAULT_TOP_K : topK, Map.of());
        } catch (RagException failure) {
            throw new KnowledgeApplicationException(failureCode(failure), traceId, failure);
        } catch (RuntimeException failure) {
            throw new KnowledgeApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_INTERNAL_ERROR, traceId, failure);
        }
        List<KnowledgeChunkBO> accepted = chunks == null ? List.of() : chunks.stream()
                .filter(chunk -> matchesThreshold(chunk.score(), threshold))
                .toList();
        if (accepted.isEmpty()) {
            return List.of();
        }
        Map<Long, String> displayNames = displayNames(accepted);
        return accepted.stream().map(chunk -> new KnowledgeRetrievedChunkBO(chunk.documentId(), chunk.chunkIndex(),
                displayNames.get(chunk.documentId()), chunk.score(), chunk.content())).toList();
    }

    /**
     * An unknown score passes only when every score is accepted: a store that reports no score
     * cannot be compared against a floor, and dropping such a chunk would hide a match the caller
     * asked to see.
     */
    private static boolean matchesThreshold(Double score, double threshold) {
        return score == null ? threshold <= ACCEPT_EVERY_SCORE : score >= threshold;
    }

    private Map<Long, String> displayNames(List<KnowledgeChunkBO> chunks) {
        List<Long> documentIds = chunks.stream().map(KnowledgeChunkBO::documentId).distinct().toList();
        Map<Long, String> displayNames = new LinkedHashMap<>();
        for (KnowledgeDocumentBO document : documentRepository.findByIds(documentIds)) {
            displayNames.put(document.documentId(), document.displayName());
        }
        return displayNames;
    }

    public KnowledgeBaseBO requiredBase(Long knowledgeBaseId, String traceId) {
        Objects.requireNonNull(knowledgeBaseId, "knowledgeBaseId must not be null");
        return knowledgeBaseRepository.findById(knowledgeBaseId)
                .orElseThrow(() -> new KnowledgeApplicationException(
                        KnowledgeErrorCodeEnum.KNOWLEDGE_BASE_NOT_FOUND, traceId));
    }

    /** The stable project code for a component failure; an unregistered model is the caller's fault. */
    private static KnowledgeErrorCodeEnum failureCode(RagException failure) {
        return failure instanceof RagModelNotRegisteredException
                ? KnowledgeErrorCodeEnum.KNOWLEDGE_MODEL_NOT_REGISTERED
                : KnowledgeErrorCodeEnum.KNOWLEDGE_DEPENDENCY_UNAVAILABLE;
    }

    public void validate(Object command, String traceId) {
        try {
            validationUtils.validate(command);
        } catch (ConstraintViolationException | IllegalArgumentException invalid) {
            throw new KnowledgeApplicationException(
                    KnowledgeErrorCodeEnum.KNOWLEDGE_VALIDATION_ERROR, traceId, invalid);
        }
    }
}
