package top.egon.cola.archetype.source.lightopen.application.user.pojo.event;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import top.egon.cola.component.common.core.pojo.BasePojo;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class UserImportedEvent implements BasePojo {
    @NotBlank
    private String externalId;
    @NotBlank
    private String name;
    @NotBlank
    private String email;
    @NotBlank
    private String operatorId;
    @NotBlank
    private String requestId;
}
