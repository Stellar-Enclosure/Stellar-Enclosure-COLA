package top.egon.cola.archetype.source.webopen.support;

import org.apache.shardingsphere.driver.yaml.YamlJDBCConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import top.egon.cola.component.common.mybatis.routing.EgonColaRouteQuery;
import top.egon.cola.component.common.mybatis.routing.EgonColaTwoLevelRouteStrategy;
import top.egon.cola.component.common.mybatis.sharding.resolver.EgonColaShardingWriteTargetResolver;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class TwoLevelRoutingExampleTest {

    @Test
    void exampleUsesReplicaReadsAndCoLocatedBusinessRoots() throws Exception {
        String text = new ClassPathResource("sharding/two-level-readwrite.yml")
                .getContentAsString(StandardCharsets.UTF_8);
        var yaml = YamlEngine.unmarshal(text, YamlJDBCConfiguration.class);
        var readwrite = yaml.getRules().stream().filter(YamlReadwriteSplittingRuleConfiguration.class::isInstance)
                .map(YamlReadwriteSplittingRuleConfiguration.class::cast).findFirst().orElseThrow();
        for (String group : List.of("master_data", "shard_0", "shard_1")) {
            assertThat(readwrite.getDataSourceGroups().get(group).getTransactionalReadQueryStrategy()).isEqualTo("DYNAMIC");
            assertThat(readwrite.getDataSourceGroups().get(group).getReadDataSourceNames()).containsExactly(group + "_replica_0");
        }
        var sharding = yaml.getRules().stream().filter(YamlShardingRuleConfiguration.class::isInstance)
                .map(YamlShardingRuleConfiguration.class::cast).findFirst().orElseThrow();
        var algorithms = sharding.getShardingAlgorithms();
        for (var algorithm : algorithms.values()) {
            Properties props = algorithm.getProps();
            Object instance = Class.forName(props.getProperty("algorithmClassName")).getConstructor().newInstance();
            instance.getClass().getMethod("init", Properties.class).invoke(instance, props);
        }
        var order = EgonColaShardingWriteTargetResolver.profile(algorithms.get("two_level_order_table").getProps());
        var item = EgonColaShardingWriteTargetResolver.profile(algorithms.get("two_level_item_table").getProps());
        for (long tenant : List.of(1L, 2L, 42L)) {
            for (long root : List.of(1L, 7L, 100L)) {
                var parent = EgonColaTwoLevelRouteStrategy.routeCandidates(order, new EgonColaRouteQuery(
                        "routing_order", tenant, List.of(root), EgonColaRouteQuery.OperationEnum.ROUTE_CANDIDATES, false))
                        .targets().getFirst();
                var child = EgonColaTwoLevelRouteStrategy.routeCandidates(item, new EgonColaRouteQuery(
                        "routing_order_item", tenant, List.of(root), EgonColaRouteQuery.OperationEnum.ROUTE_CANDIDATES, false))
                        .targets().getFirst();
                assertThat(child.group()).isEqualTo(parent.group());
                assertThat(child.table().substring("routing_order_item".length()))
                        .isEqualTo(parent.table().substring("routing_order".length()));
            }
        }
    }
}
