package top.egon.cola.archetype.source.service.starter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import top.egon.cola.component.rpc.config.EgonRpcProperties;
import top.egon.cola.component.tianshu.autoconfigure.properties.DdcProperties;
import top.egon.cola.component.common.core.validation.ValidationUtils;
import top.egon.cola.archetype.source.service.adapter.pojo.convertor.EvaluationFacadeConverter;
import top.egon.cola.archetype.source.service.facade.course.CourseFacade;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = EvaluationServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class NativeServiceConfigurationTest extends top.egon.cola.archetype.source.service.support.PersistenceTestSupport {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private Environment environment;

    @Test
    void assembles_named_native_providers_and_converters_without_external_lifecycles() {
        assertThat(context.getBean(CourseFacade.class)).isNotNull();
        assertThat(context.getBean(EvaluationFacadeConverter.class)).isNotNull();
        assertThat(context.getBean("nativeRpcValidation", ValidationUtils.class))
                .isSameAs(context.getBean("egonColaValidationUtils"));
        assertThat(context.containsBean("rpcProviderLifecycle")).isFalse();
        assertThat(context.containsBean("ddcRuntimeCoordinator")).isFalse();
        assertThat(context.containsBean("ddcHttpRegistrationRuntime")).isFalse();
        assertThat(context.getBeansOfType(org.redisson.api.RedissonClient.class)).isEmpty();
        for (String suffix : new String[]{"rpc.enabled", "rpc.provider.enabled", "rpc.consumer.enabled",
                "tianshu.enabled", "tianshu.delivery.enabled"}) {
            assertThat(environment.getProperty("egon.cola.component." + suffix)).as(suffix).isEqualTo("false");
        }
        assertThat(environment.getProperty("egon.cola.platform.tianquan.shoubing.enabled")).isEqualTo("false");
    }

    @Test
    void every_profile_uses_the_same_native_and_target_keys() {
        var expected = keys(properties("application.yml"));
        for (String profile : new String[]{"dev", "test", "prod"}) {
            assertThat(keys(properties("application-" + profile + ".yml"))).as(profile).isEqualTo(expected);
        }
        var prod = properties("application-prod.yml");
        assertThat(prod.getProperty("egon.cola.component.rpc.transport")).isEqualTo("h2c");
        assertThat(prod.getProperty("server.ssl.enabled")).isEqualTo("false");
        assertThat(prod.getProperty("spring.config.import[0]")).startsWith("tianshu-baseline:");
        assertThat(properties("application-test.yml").getProperty("app.integrations.organization.version"))
                .endsWith(":1.0.0}");
    }

    @Test
    void binds_exact_peer_methods_and_the_native_delivery_prefix() {
        var binder = Binder.get(environment);
        var rpc = binder.bind("egon.cola.component.rpc", EgonRpcProperties.class).get();
        var ddc = binder.bind("egon.cola.component.tianshu", DdcProperties.class).get();
        assertThat(rpc.isEnabled()).isFalse();
        assertThat(rpc.getTransport()).isEqualTo("h2c");
        assertThat(rpc.getProvider().getPort()).isEqualTo(50051);
        assertThat(rpc.getConsumer().getBindings()).containsOnlyKeys("organization-user-read", "organization-class-read");
        rpc.getConsumer().getBindings().forEach((id, binding) -> {
            assertThat(binding.getNamespace()).isEqualTo("fixture");
            assertThat(binding.getService()).isEqualTo("organization");
            assertThat(binding.getMethods()).containsExactly(environment.getRequiredProperty(
                    "egon.cola.platform.tianquan.shoubing.rpc.consumer.bindings." + id + ".full-method-name"));
        });
        assertThat(ddc.isEnabled()).isFalse();
        assertThat(ddc.getDelivery().isEnabled()).isFalse();
        assertThat(ddc.getDelivery().getServiceOrigin()).isEqualTo(java.net.URI.create("http://tianshu-config.platform-system.svc.cluster.local"));
        assertThat(environment.getProperty("egon.cola.component.tianshu.baseline.scope.app-code")).isEqualTo("stellar-enclosure-source-service");
        assertThat(environment.containsProperty("egon.cola.component.tianshu.registry.http.enabled")).isFalse();
    }

    private Properties properties(String path) {
        var factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource(path));
        var values = new Properties();
        factory.getObject().forEach((key, value) -> values.setProperty(key.toString(), value.toString()));
        return values;
    }

    private Set<String> keys(Properties properties) {
        return properties.stringPropertyNames().stream().filter(key -> key.startsWith("egon.cola.component.rpc.")
                || key.startsWith("egon.cola.component.tianshu.") || key.startsWith("egon.cola.component.yuheng.openapi.")
                || key.startsWith("egon.cola.platform.tianquan.shoubing.") || key.startsWith("app.integrations.organization."))
                .filter(key -> !key.contains("tenant-registration-ids"))
                .collect(Collectors.toSet());
    }
}
