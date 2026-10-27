package top.egon.cola.archetype.source.agent.application.knowledge.executor.command;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.command.CreateKnowledgeBaseCommand;
import top.egon.cola.archetype.source.agent.common.exception.KnowledgeApplicationException;
import top.egon.cola.archetype.source.agent.common.error.KnowledgeErrorCodeEnum;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeBaseBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeChunkConfigBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.repository.KnowledgeBaseRepository;
import top.egon.cola.component.common.core.validation.ValidationUtils;
import top.egon.cola.component.common.mybatis.business.EgonColaTenantIdProvider;
import java.util.List;
import java.util.Map;

@Service("createKnowledgeBaseCmdExe")
@Slf4j(topic = "top.egon.cola.archetype.source.agent.application.knowledge.manage.impl.KnowledgeBaseManageImpl")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CreateKnowledgeBaseCmdExe {

    @Qualifier("knowledgeBaseRepository")
    private final KnowledgeBaseRepository knowledgeBaseRepository;

    @Qualifier("agentValidationUtils")
    private final ValidationUtils validationUtils;

    public KnowledgeBaseBO execute(CreateKnowledgeBaseCommand command) {
        validate(command, command == null ? null : command.traceId());
        KnowledgeBaseBO stored = knowledgeBaseRepository.insert(assemble(command));
        log.info("knowledge base created knowledgeBaseId={} code={} logicalModelName={} chunkStrategy={}",
                stored.knowledgeBaseId(), stored.code(), stored.embeddingModel(), stored.chunkStrategy());
        return stored;
    }

    /** Turns the request into the domain value, refusing a combination the domain does not allow. */
    private KnowledgeBaseBO assemble(CreateKnowledgeBaseCommand command) {
        try {
            return KnowledgeBaseBO.create(command.code(), command.name(), command.description(),
                    command.embeddingModel(), command.chunkStrategy(), chunkConfig(command), currentTenantId());
        } catch (IllegalArgumentException invalid) {
            throw new KnowledgeApplicationException(
                    KnowledgeErrorCodeEnum.KNOWLEDGE_VALIDATION_ERROR, command.traceId(), invalid);
        }
    }

    private static KnowledgeChunkConfigBO chunkConfig(CreateKnowledgeBaseCommand command) {
        CreateKnowledgeBaseCommand.ChunkingConfigCommand config = command.chunkConfig();
        try {
            return new KnowledgeChunkConfigBO(config.maxTokensPerChunk(), config.overlapTokens(),
                    config.minChunkChars(), config.headingLevels());
        } catch (IllegalArgumentException invalid) {
            throw new KnowledgeApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_VALIDATION_ERROR,
                    command.traceId(), Map.of("chunkConfig", List.of(invalid.getMessage())), invalid);
        }
    }

    private Long currentTenantId() {
        Long tenantId = EgonColaTenantIdProvider.currentTenantId();
        if (tenantId == null) {
            throw new KnowledgeApplicationException(
                    KnowledgeErrorCodeEnum.KNOWLEDGE_INTERNAL_ERROR, null);
        }
        return tenantId;
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
