package top.egon.cola.archetype.source.agent.starter.config;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.flyway.autoconfigure.FlywayDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import top.egon.cola.component.outbox.autoconfigure.OutboxMpStorageProperties;
import top.egon.cola.component.outbox.migration.OutboxManagedDdlInitializer;

import javax.sql.DataSource;
import java.sql.SQLException;

/** Prevents the managed Outbox from silently leaving pending legacy messages behind. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "egon.cola.component.transactional-outbox", name = "enabled", havingValue = "true")
public class AgentOutboxMigrationConfiguration {

    @Bean(name = "agentFlywayDataSource", destroyMethod = "")
    @FlywayDataSource
    @DependsOn("dataSource")
    @ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", havingValue = "true", matchIfMissing = true)
    DataSource agentFlywayDataSource(@Qualifier("outboxManagedDdlInitializer") OutboxManagedDdlInitializer initializer,
            @Qualifier("egonColaShardingRouteFingerprint") String fingerprint) {
        return initializer.physicalMetadataDataSource(fingerprint);
    }

    @Bean
    SmartInitializingSingleton agentOutboxLegacyMigrationGuard(
            @Qualifier("outboxManagedDdlInitializer") OutboxManagedDdlInitializer initializer,
            @Qualifier("egonColaShardingRouteFingerprint") String fingerprint,
            @Qualifier("outboxMpStorageProperties") OutboxMpStorageProperties properties,
            @Value("${AGENT_OUTBOX_LEGACY_VERIFIED:false}") boolean verified) {
        return () -> {
            if (!properties.isMigrationMode() && !verified) {
                requireNoLegacyMessages(initializer.physicalMetadataDataSource(fingerprint));
            }
        };
    }

    static void requireNoLegacyMessages(DataSource primary) {
        try (var connection = primary.getConnection();
             var statement = connection.createStatement();
             var table = statement.executeQuery("SELECT to_regclass('public.egon_cola_outbox_message')")) {
            if (!table.next()) {
                throw new IllegalStateException("AGENT_OUTBOX_LEGACY_CHECK_FAILED");
            }
            if (table.getString(1) == null) {
                return;
            }
            try (var rows = statement.executeQuery("SELECT 1 FROM public.egon_cola_outbox_message LIMIT 1")) {
                if (rows.next()) {
                    throw new IllegalStateException("AGENT_OUTBOX_LEGACY_MIGRATION_REQUIRED");
                }
            }
        } catch (SQLException failure) {
            throw new IllegalStateException("AGENT_OUTBOX_LEGACY_CHECK_FAILED", failure);
        }
    }
}
