package top.egon.cola.archetype.source.agent.application.knowledge.executor.command;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.command.UpdateKnowledgeBaseCommand;
import top.egon.cola.archetype.source.agent.common.exception.KnowledgeApplicationException;
import top.egon.cola.archetype.source.agent.common.error.KnowledgeErrorCodeEnum;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeBaseBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeBaseRepository;
import top.egon.cola.component.common.core.validation.ValidationUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("updateKnowledgeBaseCmdExe")
@Slf4j(topic = "top.egon.cola.archetype.source.agent.application.knowledge.manage.impl.KnowledgeBaseManageImpl")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UpdateKnowledgeBaseCmdExe {

    /** The message the immutable-field rejection attaches to every field it names. */
    private static final String IMMUTABLE_FIELD_MESSAGE = "immutable after creation";

    @Qualifier("knowledgeBaseRepository")
    private final KnowledgeBaseRepository knowledgeBaseRepository;

    @Qualifier("agentValidationUtils")
    private final ValidationUtils validationUtils;

    public KnowledgeBaseBO execute(UpdateKnowledgeBaseCommand command) {
        validate(command, command == null ? null : command.traceId());
        String traceId = command.traceId();
        List<String> immutable = command.immutableFields();
        if (!immutable.isEmpty()) {
            // Probing before the write is what makes the rejection truthful: a silently dropped
            // field would let the caller believe the new value was stored.
            throw new KnowledgeApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_IMMUTABLE_FIELD,
                    traceId, immutableFieldErrors(immutable), null);
        }
        required(command.knowledgeBaseId(), traceId);
        knowledgeBaseRepository.updateNameAndDescription(
                command.knowledgeBaseId(), command.name(), command.description());
        KnowledgeBaseBO updated = required(command.knowledgeBaseId(), traceId);
        log.info("knowledge base updated knowledgeBaseId={} outcome=UPDATED", updated.knowledgeBaseId());
        return updated;
    }

    private KnowledgeBaseBO required(Long knowledgeBaseId, String traceId) {
        return knowledgeBaseRepository.findById(knowledgeBaseId)
                .orElseThrow(() -> new KnowledgeApplicationException(
                        KnowledgeErrorCodeEnum.KNOWLEDGE_BASE_NOT_FOUND, traceId));
    }

    private static Map<String, List<String>> immutableFieldErrors(List<String> fields) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        fields.forEach(field -> errors.put(field, List.of(IMMUTABLE_FIELD_MESSAGE)));
        return Map.copyOf(errors);
    }

    private void validate(Object command, String traceId) {
        try {
            validationUtils.validate(command);
        } catch (ConstraintViolationException | IllegalArgumentException invalid) {
            throw new KnowledgeApplicationException(
                    KnowledgeErrorCodeEnum.KNOWLEDGE_VALIDATION_ERROR, traceId, invalid);
        }
    }
}
