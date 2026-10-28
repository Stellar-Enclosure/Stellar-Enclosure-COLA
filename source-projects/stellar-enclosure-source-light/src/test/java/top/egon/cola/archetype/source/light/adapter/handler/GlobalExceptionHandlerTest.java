package top.egon.cola.archetype.source.light.adapter.handler;

import top.egon.cola.archetype.source.light.common.exception.UserUseCaseException;
import top.egon.cola.archetype.source.light.common.exception.GeneratedCrudErrorMapper;
import org.junit.jupiter.api.Test;
import top.egon.cola.component.common.core.pojo.ResultRecord;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    @Test
    void generated_crud_rejections_preserve_not_found_and_conflict_statuses() {
        var handler = new GlobalExceptionHandler();
        var missing = handler.handleGeneratedCrudFailure(GeneratedCrudErrorMapper.missing(12L));
        var conflict = handler.handleGeneratedCrudFailure(GeneratedCrudErrorMapper.zeroRows("update", 12L, 1L));
        assertThat(missing.getStatusCode().value()).isEqualTo(404);
        assertThat(conflict.getStatusCode().value()).isEqualTo(409);
        assertThat(conflict.getBody().status()).isEqualTo("GENERATED_WRITE_CONFLICT");
    }

    @Test
    void exposes_application_code_without_internal_details() {
        ResultRecord<Void> response = new GlobalExceptionHandler().handleUserFailure(
                new UserUseCaseException("USER_EXISTS", "User exists", new IllegalStateException("secret")));
        assertThat(response.code()).isEqualTo(600000);
        assertThat(response.status()).isEqualTo("USER_EXISTS");
        assertThat(response.success()).isFalse();
        assertThat(response.message()).isEqualTo("User exists");
        assertThat(response.toString()).doesNotContain("secret");
    }
}
