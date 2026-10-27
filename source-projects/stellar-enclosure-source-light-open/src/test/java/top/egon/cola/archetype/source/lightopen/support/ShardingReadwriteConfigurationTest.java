package top.egon.cola.archetype.source.lightopen.support;

import jakarta.validation.Validation;
import org.apache.shardingsphere.driver.yaml.YamlJDBCConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import top.egon.cola.component.common.core.validation.ValidationUtils;
import top.egon.cola.component.common.mybatis.autoconfigure.EgonColaMybatisPlusProperties;
import top.egon.cola.component.common.mybatis.routing.EgonColaTwoLevelRouteStrategy;
import top.egon.cola.component.common.mybatis.routing.EgonColaRoutingProfileBO;
import top.egon.cola.component.common.mybatis.sharding.EgonColaShardingProperties;
import top.egon.cola.component.common.mybatis.sharding.bootstrap.EgonColaShardingTopologyValidator;
import top.egon.cola.component.common.mybatis.sharding.bootstrap.EgonColaShardingYamlLoader;
import top.egon.cola.component.common.mybatis.sharding.strategy.EgonColaComplexTenantThenBusinessShardingStrategy;
import top.egon.cola.component.common.mybatis.sharding.strategy.EgonColaShardingStrategyFactory;
import top.egon.cola.component.common.mybatis.sharding.strategy.EgonColaSingleTableShardingStrategy;
import top.egon.cola.component.common.mybatis.sharding.strategy.EgonColaStandardTenantIdShardingStrategy;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ShardingReadwriteConfigurationTest {

    @Test
    void profileBindsEachPhysicalGroupAndEmitsReadwriteRules() throws Exception {
        try (var validators = Validation.buildDefaultValidatorFactory()) {
            var validation = new ValidationUtils(validators.getValidator());
            for (String profile : new String[]{"dev", "test", "prod"}) {
                try (var context = new AnnotationConfigApplicationContext()) {
                    TestPropertyValues.of(
                            "spring.profiles.active=" + profile + ",readwrite",
                            "APP_DATASOURCE_MODE=SHARDING",
                            "LIGHT_SHARDING_USERNAME=test",
                            "LIGHT_SHARDING_PASSWORD=test",
                            "LIGHT_SHARDING_MASTER_DATA_URL=jdbc:postgresql://localhost/master",
                            "LIGHT_SHARDING_MASTER_DATA_REPLICA_URL=jdbc:postgresql://localhost/master_replica",
                            "LIGHT_SHARDING_SHARD_0_URL=jdbc:postgresql://localhost/shard_0",
                            "LIGHT_SHARDING_SHARD_0_REPLICA_URL=jdbc:postgresql://localhost/shard_0_replica",
                            "LIGHT_SHARDING_SHARD_1_URL=jdbc:postgresql://localhost/shard_1",
                            "LIGHT_SHARDING_SHARD_1_REPLICA_URL=jdbc:postgresql://localhost/shard_1_replica"
                    ).applyTo(context);
                    new ConfigDataApplicationContextInitializer().initialize(context);
                    var binder = Binder.get(context.getEnvironment());
                    var sharding = binder.bind(EgonColaShardingProperties.PREFIX, Bindable.of(EgonColaShardingProperties.class)).get();
                    assertThat(sharding.getMode()).as(profile).isEqualTo(EgonColaShardingProperties.ModeEnum.SHARDING_READWRITE);
                    assertThat(sharding.getDataSources()).as(profile).hasSize(6);
                    var route = new EgonColaTwoLevelRouteStrategy(validation);
                    var yamlLoader = new EgonColaShardingYamlLoader(new EgonColaShardingStrategyFactory(validation, Map.of(
                            "SINGLE", new EgonColaSingleTableShardingStrategy(),
                            "STANDARD_TENANT_ID", new EgonColaStandardTenantIdShardingStrategy(),
                            "COMPLEX_TENANT_THEN_BUSINESS", new EgonColaComplexTenantThenBusinessShardingStrategy(route))),
                            new DefaultResourceLoader(), context.getEnvironment());
                    var topology = new EgonColaShardingTopologyValidator(validation).validate(sharding, yamlLoader.load(sharding));
                    String migration = new ClassPathResource("db/egon-mp/V20261027_001__align_readwrite_shard_tables.sql")
                            .getContentAsString(StandardCharsets.UTF_8);
                    var sharded = topology.profiles().values().stream()
                            .filter(table -> table.kind() == EgonColaRoutingProfileBO.TableKindEnum.TENANT_LEGACY).toList();
                    assertThat(sharded).as(profile).isNotEmpty();
                    for (var table : sharded) {
                        assertThat(migration.indexOf("LOCK TABLE " + table.logicalTable() + "_0, "
                                + table.logicalTable() + "_1 IN ACCESS EXCLUSIVE MODE"))
                                .isBetween(0, migration.indexOf("IF EXISTS (SELECT 1 FROM " + table.logicalTable() + "_0"));
                        var nodes = table.actualNodes().values().stream().flatMap(java.util.List::stream).toList();
                        assertThat(nodes).as(profile).hasSize(2);
                        for (int index = 0; index < 2; index++) {
                            assertThat(nodes.get(index).group()).isEqualTo("shard_" + index);
                            assertThat(nodes.get(index).table()).isEqualTo(table.logicalTable() + "_t" + index);
                            assertThat(migration).contains("ALTER TABLE " + table.logicalTable() + "_" + index
                                    + " RENAME TO " + table.logicalTable() + "_t" + index);
                            assertThat(migration).contains("EXISTS (SELECT 1 FROM " + table.logicalTable() + "_" + index + " LIMIT 1)");
                        }
                    }
                    var configuration = YamlEngine.unmarshal(new String(topology.yaml(), StandardCharsets.UTF_8), YamlJDBCConfiguration.class);
                    var rules = configuration.getRules().stream().filter(YamlReadwriteSplittingRuleConfiguration.class::isInstance)
                            .map(YamlReadwriteSplittingRuleConfiguration.class::cast).findFirst().orElseThrow();
                    for (String group : new String[]{"master_data", "shard_0", "shard_1"}) {
                        assertThat(rules.getDataSourceGroups().get(group).getWriteDataSourceName()).as(profile).isEqualTo(group);
                        assertThat(rules.getDataSourceGroups().get(group).getReadDataSourceNames()).as(profile).containsExactly(group + "_replica");
                        assertThat(rules.getDataSourceGroups().get(group).getTransactionalReadQueryStrategy()).as(profile).isEqualTo("DYNAMIC");
                    }
                    var ddl = binder.bind(EgonColaMybatisPlusProperties.PREFIX, Bindable.of(EgonColaMybatisPlusProperties.class)).get().getDdl();
                    assertThat(ddl.isAutoMigrate()).as(profile).isFalse();
                    assertThat(ddl.getTargets()).as(profile).hasSize(3);
                    TestPropertyValues.of("EGON_MP_AUTO_MIGRATE=true").applyTo(context);
                    var optedIn = Binder.get(context.getEnvironment()).bind(EgonColaMybatisPlusProperties.PREFIX,
                            Bindable.of(EgonColaMybatisPlusProperties.class)).get().getDdl();
                    assertThat(optedIn.isAutoMigrate()).as(profile).isTrue();
                }
            }
        }
    }
}
