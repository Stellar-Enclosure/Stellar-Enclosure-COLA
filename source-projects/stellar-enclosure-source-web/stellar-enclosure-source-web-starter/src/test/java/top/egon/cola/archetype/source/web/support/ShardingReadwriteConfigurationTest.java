package top.egon.cola.archetype.source.web.support;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Validation;
import org.apache.shardingsphere.driver.yaml.YamlJDBCConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
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

import top.egon.cola.component.tianshu.configdata.DdcConfigDataLoader;
import top.egon.cola.component.tianshu.format.DdcConfigCanonicalJson;
import top.egon.cola.component.tianshu.model.config.DdcConfigBaselineVO;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ShardingReadwriteConfigurationTest {

    @Test
    void profileBindsEachPhysicalGroupAndEmitsReadwriteRules(@TempDir Path temporary) throws Exception {
        Path baseline = writeBaseline(temporary);
        try (var validators = Validation.buildDefaultValidatorFactory()) {
            var validation = new ValidationUtils(validators.getValidator());
            for (String profile : new String[]{"dev", "test", "prod"}) {
                try (var context = new AnnotationConfigApplicationContext()) {
                    TestPropertyValues.of(
                            "spring.profiles.active=" + profile + ",readwrite",
                            "APP_DATASOURCE_MODE=SHARDING",
                            "ORGANIZATION_SHARDING_USERNAME=test",
                            "ORGANIZATION_SHARDING_PASSWORD=test",
                            "ORGANIZATION_SHARDING_MASTER_DATA_URL=jdbc:postgresql://localhost/master",
                            "ORGANIZATION_SHARDING_MASTER_DATA_REPLICA_URL=jdbc:postgresql://localhost/master_replica",
                            "ORGANIZATION_SHARDING_SHARD_0_URL=jdbc:postgresql://localhost/shard_0",
                            "ORGANIZATION_SHARDING_SHARD_0_REPLICA_URL=jdbc:postgresql://localhost/shard_0_replica",
                            "ORGANIZATION_SHARDING_SHARD_1_URL=jdbc:postgresql://localhost/shard_1",
                            "ORGANIZATION_SHARDING_SHARD_1_REPLICA_URL=jdbc:postgresql://localhost/shard_1_replica"
                    ).applyTo(context);
                    if ("prod".equals(profile)) {
                        TestPropertyValues.of(
                                "TIANSHU_CONFIG_ENABLED=false",
                                "TIANSHU_CONFIG_BASELINE_FILE=" + baseline,
                                "TIANSHU_TENANT_ID=1001",
                                "TIANSHU_BIZ_CODE=topology",
                                "TIANSHU_APP_CODE=topology",
                                "APP_ENV=prod",
                                "TIANSHU_CONFIG_TARGET_BINDING_ID=topology-prod",
                                "TIANSHU_CONFIG_TARGET_BINDING_REVISION=3",
                                "CLUSTER_ID=test-cluster",
                                "POD_NAMESPACE=test-namespace",
                                "WORKLOAD_UID=test-workload",
                                "CONTAINER_NAME=application"
                        ).applyTo(context);
                    }
                    new ConfigDataApplicationContextInitializer().initialize(context);
                    if ("prod".equals(profile)) {
                        var loaded = DdcConfigDataLoader.baselineMetadataOrNull(context.getEnvironment());
                        assertThat(loaded).isNotNull();
                        assertThat(loaded.targetBinding().getBindingId()).isEqualTo("topology-prod");
                    }
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

    private static Path writeBaseline(Path temporary) throws Exception {
        var body = (ObjectNode) DdcConfigCanonicalJson.parse("""
                {"schemaVersion":"1",
                 "scope":{"tenantId":"1001","bizCode":"topology","env":"prod",
                          "appCode":"topology","resourceName":"application.yml"},
                 "source":{"configId":"42","revision":"43"},
                 "enabled":true,"contentSha256":"sha256:%s","restartDigest":"sha256:%s",
                 "rules":[],"restartValues":{},"hotSeed":{},"startupDependencies":{},"secretReferences":{},
                 "targetBindings":[{"bindingId":"topology-prod","bindingRevision":"3","clusterId":"test-cluster",
                      "kubernetesNamespace":"test-namespace","workloadKind":"Deployment","workloadName":"topology",
                      "workloadUid":"test-workload","containerName":"application"}]}
                """.formatted("0".repeat(64), "0".repeat(64)).getBytes(StandardCharsets.UTF_8));
        var mapper = Jackson2ObjectMapperBuilder.json().build();
        var draft = mapper.treeToValue(body, DdcConfigBaselineVO.class);
        body.put("restartDigest", DdcConfigCanonicalJson.restartDigest(draft));
        body.put("contentSha256", DdcConfigCanonicalJson.contentSha256(mapper.treeToValue(body, DdcConfigBaselineVO.class)));
        return Files.writeString(temporary.resolve("baseline.json"), body.toString());
    }
}
