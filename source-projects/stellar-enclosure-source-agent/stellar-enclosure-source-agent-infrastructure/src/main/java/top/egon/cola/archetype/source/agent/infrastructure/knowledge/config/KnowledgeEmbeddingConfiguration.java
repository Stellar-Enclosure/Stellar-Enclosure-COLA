package top.egon.cola.archetype.source.agent.infrastructure.knowledge.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publishes the embedding model the RAG component resolves by name.
 *
 * <p>The model is built here rather than auto-configured because the host owns the endpoint, the key
 * and the model name; the component is never given any of them. Metadata is deliberately not
 * embedded ({@link MetadataMode#NONE}): the chunk metadata is bookkeeping — collection, document,
 * index, logical model, tenant — and folding it into the vector would make similarity depend on it.
 *
 * <p>Gated on the component being enabled: a host that runs without RAG never needs an embedding
 * endpoint, and a deployment without one fails at start-up instead of at the first ingest.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KnowledgeEmbeddingProperties.class)
@ConditionalOnProperty(prefix = "egon.cola.component.rag", name = "enabled", havingValue = "true")
public class KnowledgeEmbeddingConfiguration {

    @Bean("knowledgeEmbeddingModel")
    @ConditionalOnMissingBean(name = "knowledgeEmbeddingModel")
    public EmbeddingModel knowledgeEmbeddingModel(KnowledgeEmbeddingProperties properties) {
        // Spring AI 1 appended /v1; the SDK now expects it in the base URL.
        return OpenAiEmbeddingModel.builder()
                .metadataMode(MetadataMode.NONE)
                .options(OpenAiEmbeddingOptions.builder()
                        .baseUrl(properties.baseUrl().replaceAll("/+$", "") + "/v1")
                        .apiKey(properties.apiKey())
                        .model(properties.modelName())
                        .build())
                .build();
    }
}
