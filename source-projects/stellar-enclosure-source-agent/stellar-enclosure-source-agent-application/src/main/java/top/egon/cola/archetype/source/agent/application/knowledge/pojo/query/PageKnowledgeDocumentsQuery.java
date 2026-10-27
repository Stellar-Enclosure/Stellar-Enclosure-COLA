package top.egon.cola.archetype.source.agent.application.knowledge.pojo.query;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import top.egon.cola.component.common.core.pojo.BasePojo;
import lombok.experimental.Accessors;
import lombok.Builder;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.DocumentIngestStatusEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class PageKnowledgeDocumentsQuery implements BasePojo {
    private Long knowledgeBaseId;
    private int page;
    private int size;
    private DocumentIngestStatusEnum status;
    private String keyword;
}
