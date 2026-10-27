package top.egon.cola.archetype.source.agent.starter.config;

import org.junit.jupiter.api.Test;
import top.egon.cola.component.outbox.autoconfigure.OutboxMpStorageProperties;
import top.egon.cola.component.outbox.migration.OutboxManagedDdlInitializer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentOutboxMigrationConfigurationTest {
    @Test
    void flywayUsesTheExistingPhysicalPrimary() throws Exception {
        var initializer = mock(OutboxManagedDdlInitializer.class);
        var primary = mock(DataSource.class);
        when(initializer.physicalMetadataDataSource("fingerprint")).thenReturn(primary);
        var configuration = new AgentOutboxMigrationConfiguration();
        org.assertj.core.api.Assertions.assertThat(configuration.agentFlywayDataSource(initializer, "fingerprint"))
                .isSameAs(primary);
        var method = AgentOutboxMigrationConfiguration.class.getDeclaredMethod("agentFlywayDataSource",
                OutboxManagedDdlInitializer.class, String.class);
        org.assertj.core.api.Assertions.assertThat(method.getAnnotation(org.springframework.boot.flyway.autoconfigure.FlywayDataSource.class))
                .isNotNull();
        org.assertj.core.api.Assertions.assertThat(method.getAnnotation(org.springframework.context.annotation.Bean.class).destroyMethod())
                .isEmpty();
    }

    @Test
    void flywayBindingKeepsTheLogicalBusinessDatasourcePrimary() {
        var initializer = mock(OutboxManagedDdlInitializer.class);
        var primary = mock(DataSource.class);
        var logical = mock(DataSource.class);
        when(initializer.physicalMetadataDataSource("fingerprint")).thenReturn(primary);
        new org.springframework.boot.test.context.runner.ApplicationContextRunner()
                .withUserConfiguration(AgentOutboxMigrationConfiguration.class)
                .withBean("dataSource", DataSource.class, () -> logical, definition -> definition.setPrimary(true))
                .withBean("outboxManagedDdlInitializer", OutboxManagedDdlInitializer.class, () -> initializer)
                .withBean("egonColaShardingRouteFingerprint", String.class, () -> "fingerprint")
                .withBean("outboxMpStorageProperties", OutboxMpStorageProperties.class,
                        () -> new OutboxMpStorageProperties().setMigrationMode(true))
                .withPropertyValues("egon.cola.component.transactional-outbox.enabled=true", "spring.flyway.enabled=true")
                .run(context -> {
                    org.assertj.core.api.Assertions.assertThat(context).hasNotFailed();
                    org.assertj.core.api.Assertions.assertThat(context.getBean(DataSource.class)).isSameAs(logical);
                    org.assertj.core.api.Assertions.assertThat(context.getBean("agentFlywayDataSource")).isSameAs(primary);
                    org.assertj.core.api.Assertions.assertThat(context.getBeanFactory().findAnnotationOnBean("agentFlywayDataSource",
                            org.springframework.boot.flyway.autoconfigure.FlywayDataSource.class)).isNotNull();
                });
    }

    @Test
    void blocksExistingMessagesUntilMaintenanceIsExplicitlyVerified() throws Exception {
        var primary = mock(DataSource.class);
        var connection = mock(Connection.class);
        var statement = mock(Statement.class);
        var table = mock(ResultSet.class);
        var rows = mock(ResultSet.class);
        when(primary.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT to_regclass('public.egon_cola_outbox_message')")).thenReturn(table);
        when(table.next()).thenReturn(true);
        when(table.getString(1)).thenReturn("egon_cola_outbox_message");
        when(statement.executeQuery("SELECT 1 FROM public.egon_cola_outbox_message LIMIT 1")).thenReturn(rows);
        when(rows.next()).thenReturn(true, false);

        assertThatThrownBy(() -> AgentOutboxMigrationConfiguration.requireNoLegacyMessages(primary))
                .hasMessage("AGENT_OUTBOX_LEGACY_MIGRATION_REQUIRED");
        assertThatCode(() -> AgentOutboxMigrationConfiguration.requireNoLegacyMessages(primary)).doesNotThrowAnyException();
    }

    @Test
    void acceptsAnInstallationWithoutALegacyTable() throws Exception {
        var primary = mock(DataSource.class);
        var connection = mock(Connection.class);
        var statement = mock(Statement.class);
        var table = mock(ResultSet.class);
        when(primary.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT to_regclass('public.egon_cola_outbox_message')")).thenReturn(table);
        when(table.next()).thenReturn(true);
        assertThatCode(() -> AgentOutboxMigrationConfiguration.requireNoLegacyMessages(primary)).doesNotThrowAnyException();
        verify(statement, never()).executeQuery("SELECT 1 FROM public.egon_cola_outbox_message LIMIT 1");
    }

    @Test
    void failsClosedWhenTheLegacyTableCannotBeChecked() throws Exception {
        var primary = mock(DataSource.class);
        when(primary.getConnection()).thenThrow(new SQLException("unavailable"));
        assertThatThrownBy(() -> AgentOutboxMigrationConfiguration.requireNoLegacyMessages(primary))
                .hasMessage("AGENT_OUTBOX_LEGACY_CHECK_FAILED").hasCauseInstanceOf(SQLException.class);
    }

    @Test
    void maintenanceModeAndOperatorVerificationDoNotReadTheLegacyTable() {
        var initializer = mock(OutboxManagedDdlInitializer.class);
        var configuration = new AgentOutboxMigrationConfiguration();
        var properties = new OutboxMpStorageProperties().setMigrationMode(true);
        configuration.agentOutboxLegacyMigrationGuard(initializer, "fingerprint", properties, false).afterSingletonsInstantiated();
        properties.setMigrationMode(false);
        configuration.agentOutboxLegacyMigrationGuard(initializer, "fingerprint", properties, true).afterSingletonsInstantiated();
        verify(initializer, never()).physicalMetadataDataSource("fingerprint");
    }
}
