package top.egon.cola.archetype.source.agent.application.knowledge.pojo.query;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import top.egon.cola.component.common.core.pojo.BasePojo;
import lombok.experimental.Accessors;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class PageKnowledgeBasesQuery implements BasePojo {
    private int page;
    private int size;
    private String keyword;
    private String embeddingModel;
}
