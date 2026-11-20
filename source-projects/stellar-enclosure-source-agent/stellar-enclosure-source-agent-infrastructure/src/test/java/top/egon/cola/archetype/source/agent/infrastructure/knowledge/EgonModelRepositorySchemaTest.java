package top.egon.cola.archetype.source.agent.infrastructure.knowledge;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

class EgonModelRepositorySchemaTest {
    @Test
    void correctionIsEmptyOnlyAndRetainsFlywayAndExternalTableOwnership() throws Exception {
        String sql = Files.readString(Path.of("src/main/resources/db/migration/V20260913_001__egon_model_repository.sql"));
        assertThat(sql.indexOf("REBUILD_REQUIRED")).isLessThan(sql.indexOf("ALTER TABLE"));
        assertThat(sql).contains("ADD COLUMN deleted_at timestamp(6) without time zone", "version bigint NOT NULL DEFAULT 0", "DROP IDENTITY IF EXISTS", "DROP COLUMN is_deleted RESTRICT", "WHERE deleted_at IS NULL")
                .doesNotContain("UPDATE knowledge_", "1970-01-01", "egon_cola_outbox_message", "DROP TABLE");
        try (var files = Files.list(Path.of("src/main/resources/db/migration"))) {
            assertThat(files.map(path -> path.getFileName().toString()).filter(name -> name.endsWith(".sql")).sorted())
                    .containsExactly(
                            "V20260910_001__create_knowledge_schema.sql",
                            "V20260910_002__create_transactional_outbox_schema.sql",
                            "V20260913_001__egon_model_repository.sql",
                            "V20261028_001__add_ingest_generation.sql");
        }
    }
}
