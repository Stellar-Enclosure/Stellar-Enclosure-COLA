package top.egon.cola.archetype.source.light.start.config;

import io.grpc.Status;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.core.io.ClassPathResource;
import top.egon.cola.archetype.source.light.adapter.pojo.convertor.LightFacadeConverter;
import top.egon.cola.archetype.source.light.infrastructure.config.RedisConfig;
import top.egon.cola.component.common.core.validation.ValidationUtils;
import top.egon.cola.component.rpc.provider.server.RpcProviderExceptionMapper;

import java.time.format.DateTimeParseException;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class NativeRpcConfigurationTest {
    @Test
    void named_rpc_validation_reuses_the_existing_component_validator() {
        try (var validators = Validation.buildDefaultValidatorFactory()) {
            var validation = new ValidationUtils(validators.getValidator());
            new ApplicationContextRunner().withUserConfiguration(NativeRpcConfiguration.class)
                    .withBean("egonColaValidationUtils", ValidationUtils.class, () -> validation)
                    .run(context -> {
                        assertThat(context).hasNotFailed();
                        assertThat(context.getBean("nativeRpcValidation")).isSameAs(validation);
                        assertThat(context.getBean(ValidationUtils.class)).isSameAs(validation);
                        assertThat(context.getBean("lightFacadeConverter")).isInstanceOf(LightFacadeConverter.class);
                        var mapper = context.getBean("nativeRpcValidationExceptionMapper", RpcProviderExceptionMapper.class);
                        assertThat(mapper.map(new ConstraintViolationException(Set.of())).orElseThrow()
                                .getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
                        assertThat(mapper.map(new DateTimeParseException("invalid", "bad", 0)).orElseThrow()
                                .getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
                        assertThat(mapper.map(new IllegalStateException("server failure"))).isEmpty();
                    });
        }
    }

    @Test
    void all_profiles_define_the_same_native_keys_and_test_disables_external_lifecycles() {
        var base = properties("application.yml");
        assertThat(base.getProperty("spring.application.name")).isEqualTo("${APP_NAME:stellar-enclosure-source-light}");
        for (String profile : new String[]{"dev", "test", "prod"}) {
            assertThat(nativeKeys(properties("application-" + profile + ".yml")))
                    .as(profile).isEqualTo(nativeKeys(base));
        }
        var test = properties("application-test.yml");
        for (String suffix : new String[]{"rpc.enabled", "rpc.provider.enabled", "rpc.consumer.enabled",
                "tianshu.enabled", "tianshu.delivery.enabled", "yuheng.openapi.enabled"}) {
            assertThat(test.getProperty("egon.cola.component." + suffix)).as(suffix).isEqualTo("false");
        }
        assertThat(test.getProperty("egon.cola.platform.tianquan.shoubing.enabled")).isEqualTo("false");
        var prod = properties("application-prod.yml");
        assertThat(prod.getProperty("egon.cola.component.rpc.transport")).isEqualTo("h2c");
        assertThat(prod.getProperty("spring.config.import[0]")).startsWith("tianshu-baseline:");
        assertThat(prod.getProperty("server.ssl.enabled")).isEqualTo("false");
        assertThat(prod.getProperty("egon.cola.platform.tianquan.shoubing.register-filter")).isEqualTo("false");
    }

    @Test
    void native_properties_bind_to_the_actual_component_prefixes() {
        var environment = new org.springframework.core.env.StandardEnvironment();
        var values = new java.util.LinkedHashMap<String, Object>();
        properties("application.yml").forEach((key, value) -> values.put(key.toString(), value));
        properties("application-test.yml").forEach((key, value) -> values.put(key.toString(), value));
        values.put("server.port", "8181");
        environment.getPropertySources().addFirst(new org.springframework.core.env.MapPropertySource("native", values));
        var binder = org.springframework.boot.context.properties.bind.Binder.get(environment);
        var rpc = binder.bind("egon.cola.component.rpc", top.egon.cola.component.rpc.config.EgonRpcProperties.class).get();
        assertThat(rpc.isEnabled()).isFalse();
        assertThat(rpc.getProvider().getPort()).isEqualTo(50051);
        assertThat(rpc.getConsumer().getDefaultTimeoutMs()).isEqualTo(3000);
        var ddc = binder.bind("egon.cola.component.tianshu", top.egon.cola.component.tianshu.autoconfigure.properties.DdcProperties.class).get();
        assertThat(rpc.getTransport()).isEqualTo("h2c");
        assertThat(rpc.getConsumer().getBindings()).isEmpty();
        assertThat(ddc.isEnabled()).isFalse();
        assertThat(ddc.getDelivery().isEnabled()).isFalse();
        assertThat(ddc.getDelivery().getWorkloadTokenFile()).isEqualTo(java.nio.file.Path.of("/var/run/tianshu/identity/workload-token"));
        assertThat(binder.bind("egon.cola.component.tianshu.baseline.scope.app-code", String.class).get())
                .isEqualTo("stellar-enclosure-source-light");
        assertThat(binder.bind("egon.cola.component.tianshu.registry.http.enabled", Boolean.class).isBound()).isFalse();
        var idp = binder.bind("egon.cola.platform.tianquan.shoubing", top.egon.cola.platform.tianquan.shoubing.starter.autoconfigure.IdpStarterProperties.class).get();
        assertThat(idp.isEnabled()).isFalse();
        assertThat(idp.isRegisterFilter()).isFalse();
        assertThat(idp.getServiceClient().getRegistrationId()).isEqualTo("tianshuregistration");
    }

    @Test
    void managed_redis_uses_the_named_cluster_client_and_primary_reads() {
        try (var redisson = mockStatic(Redisson.class)) {
            var client = mock(RedissonClient.class);
            redisson.when(() -> Redisson.create(any(Config.class))).thenAnswer(call -> {
                Config config = call.getArgument(0);
                assertThat(config.isClusterConfig()).isTrue();
                assertThat(config.isSingleConfig()).isFalse();
                var cluster = config.useClusterServers();
                assertThat(cluster.getNodeAddresses()).containsExactly("rediss://redis-a.fixture:6379", "rediss://redis-b.fixture:6379");
                assertThat(cluster.getReadMode()).isEqualTo(ReadMode.MASTER);
                assertThat(cluster.getUsername()).isEqualTo("fixture-lease");
                return client;
            });
            new ApplicationContextRunner().withUserConfiguration(RedisConfig.class)
                    .withBean(CacheManager.class, ConcurrentMapCacheManager::new)
                    .withInitializer(context -> context.getEnvironment().setActiveProfiles("prod"))
                    .withPropertyValues("egon.cola.component.cache.enabled=true", "spring.data.redis.cluster.nodes[0]=redis-a.fixture:6379",
                            "spring.data.redis.cluster.nodes[1]=redis-b.fixture:6379", "spring.data.redis.ssl.enabled=true",
                            "spring.data.redis.username=fixture-lease", "spring.data.redis.password=fixture-only")
                    .run(context -> {
                        assertThat(context).hasNotFailed();
                        assertThat(context.getBean("redissonClient", RedissonClient.class)).isSameAs(client);
                    });
            redisson.verify(() -> Redisson.create(any(Config.class)));
        }
    }

    @Test
    void managed_redis_rejects_static_id_overrides_and_unmanaged_destinations_before_connecting() {
        try (var redisson = mockStatic(Redisson.class)) {
            var application = new ApplicationContextRunner().withUserConfiguration(RedisConfig.class)
                    .withInitializer(context -> context.getEnvironment().setActiveProfiles("prod", "test"));
            application.withPropertyValues("egon.cola.component.cache.enabled=false", "egon.cola.component.id.machine-id=7")
                    .run(context -> assertThat(context).hasFailed());
            application.withPropertyValues("egon.cola.component.cache.enabled=true", "spring.data.redis.host=redis-a.fixture")
                    .run(context -> assertThat(context).hasFailed());
            application.withPropertyValues("egon.cola.component.cache.enabled=true", "spring.data.redis.cluster.nodes[0]=redis-a.fixture:6379/0",
                            "spring.data.redis.username=fixture-lease", "spring.data.redis.password=fixture-only")
                    .run(context -> assertThat(context).hasFailed());
            redisson.verifyNoInteractions();
        }
    }

    private Properties properties(String path) {
        var factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource(path));
        var values = new Properties();
        factory.getObject().forEach((key, value) -> values.setProperty(key.toString(), value.toString()));
        return values;
    }

    private Set<String> nativeKeys(Properties values) {
        return values.stringPropertyNames().stream().filter(key -> key.startsWith("egon.cola.component.rpc.")
                || key.startsWith("egon.cola.component.tianshu.")
                || key.startsWith("egon.cola.component.yuheng.openapi.")
                || key.startsWith("egon.cola.platform.tianquan.shoubing.")).collect(Collectors.toSet());
    }
}
