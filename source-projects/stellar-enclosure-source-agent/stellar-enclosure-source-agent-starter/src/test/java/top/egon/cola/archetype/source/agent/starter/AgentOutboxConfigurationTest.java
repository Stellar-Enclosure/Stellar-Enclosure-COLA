package top.egon.cola.archetype.source.agent.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import top.egon.cola.component.common.core.validation.ValidationUtils;
import top.egon.cola.component.common.mybatis.autoconfigure.EgonColaMybatisPlusProperties;
import top.egon.cola.component.common.mybatis.ddl.EgonColaPostgreDdlRunner;
import top.egon.cola.component.common.mybatis.sharding.EgonColaShardingProperties;
import top.egon.cola.component.common.mybatis.sharding.bootstrap.EgonColaShardingTopologyValidator;
import top.egon.cola.component.common.mybatis.sharding.bootstrap.EgonColaShardingYamlLoader;
import top.egon.cola.component.common.mybatis.sharding.strategy.EgonColaShardingStrategyFactory;
import top.egon.cola.component.common.mybatis.sharding.strategy.EgonColaSingleTableShardingStrategy;
import top.egon.cola.component.outbox.autoconfigure.OutboxMpStorageProperties;
import top.egon.cola.component.outbox.migration.OutboxManagedDdlInitializer;

import javax.sql.DataSource;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AgentOutboxConfigurationTest {

    @Test
    void everyProfileBindsTheManagedOutboxRoutesAndMapper() throws Exception {
        try (var validators = Validation.buildDefaultValidatorFactory()) {
            var validation = new ValidationUtils(validators.getValidator());
            for (String profile : new String[]{"application.yml", "application-dev.yml", "application-test.yml", "application-prod.yml"}) {
                var environment = new StandardEnvironment();
                var loader = new YamlPropertySourceLoader();
                loader.load(profile, new ClassPathResource(profile)).forEach(environment.getPropertySources()::addFirst);
                loader.load("sharding", new ClassPathResource("egon-mybatis-plus-sharding.yml"))
                        .forEach(environment.getPropertySources()::addFirst);
                environment.getPropertySources().addFirst(new MapPropertySource("test-credentials", Map.of(
                        "AGENT_DB_URL", "jdbc:postgresql://localhost/agent",
                        "AGENT_DB_USERNAME", "agent", "AGENT_DB_PASSWORD", "test")));
                var binder = Binder.get(environment);
                var sharding = binder.bind(EgonColaShardingProperties.PREFIX, Bindable.of(EgonColaShardingProperties.class)).get();
                var mybatis = binder.bind(EgonColaMybatisPlusProperties.PREFIX, Bindable.of(EgonColaMybatisPlusProperties.class)).get();
                var topologyValidator = new EgonColaShardingTopologyValidator(validation);
                var yamlLoader = new EgonColaShardingYamlLoader(new EgonColaShardingStrategyFactory(validation,
                        Map.of("SINGLE", new EgonColaSingleTableShardingStrategy())), new DefaultResourceLoader(), environment);
                byte[] yaml = yamlLoader.load(sharding);
                var topology = topologyValidator.validate(sharding, yaml);
                var physical = mock(DataSource.class);
                var outbox = new OutboxManagedDdlInitializer(mock(EgonColaPostgreDdlRunner.class), topologyValidator,
                        validation, sharding, new OutboxMpStorageProperties().setAutoInitializeDdl(false), new ObjectMapper());
                outbox.initialize(Map.of("primary", physical), yaml);
                assertThat(outbox.isReadyFor(physical, topology.fingerprint())).as(profile).isTrue();
                assertThat(mybatis.getTenantId().ignores("egon_cola_outbox_message")).as(profile).isTrue();
                assertThat(environment.getProperty("mybatis-plus.mapper-locations"))
                        .as(profile).contains("classpath*:mapper/outbox/**/*.xml");
            }
        }
    }
}
