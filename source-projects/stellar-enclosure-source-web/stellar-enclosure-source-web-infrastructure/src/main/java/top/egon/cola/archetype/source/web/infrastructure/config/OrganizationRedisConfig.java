package top.egon.cola.archetype.source.web.infrastructure.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.BaseConfig;
import org.redisson.config.ReadMode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import top.egon.cola.component.common.cache.autoconfigure.EgonColaCacheProperties;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 缓存连接装配：二级缓存是必需能力，缺省即开启，只在这里提供宿主侧的 Redisson 客户端。
 * 受管 profile 使用显式 Redis Cluster 坐标和 ACL，隔离测试可使用本机单节点。
 */
@Configuration(value = "organizationRedisConfig", proxyBeanMethods = false)
@EnableCaching
public class OrganizationRedisConfig {

    @Bean("organizationRedisTemplate")
    @ConditionalOnProperty(prefix = "organization.integrations.redis", name = "enabled", havingValue = "true")
    RedisTemplate<String, Object> organizationRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean("managedMachineIdConfigurationGuard")
    static BeanFactoryPostProcessor managedMachineIdConfigurationGuard(@Qualifier("environment") Environment environment) {
        return factory -> {
            if (!environment.matchesProfiles("test & !dev & !prod")
                    && Binder.get(environment).bind("egon.cola.component.id.machine-id", String.class).isBound()) {
                throw new IllegalStateException("Managed profiles require a Redis machine ID lease");
            }
        };
    }

    @Bean(name = "redissonClient", destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = EgonColaCacheProperties.PREFIX, name = "enabled",
            havingValue = "true", matchIfMissing = true)
    RedissonClient redissonClient(
            @Qualifier("environment") Environment environment,
            @Value("${spring.data.redis.host:}") String host,
            @Value("${spring.data.redis.port:6379}") int port,
            @Value("${spring.data.redis.database:0}") int database,
            @Value("${spring.data.redis.username:}") String username,
            @Value("${spring.data.redis.password:}") String password,
            @Value("${spring.data.redis.ssl.enabled:false}") boolean ssl) {
        var settings = Binder.get(environment);
        Duration connectTimeout = settings.bind("spring.data.redis.connect-timeout", Duration.class).orElse(Duration.ofSeconds(3));
        Duration timeout = settings.bind("spring.data.redis.timeout", Duration.class).orElse(Duration.ofSeconds(3));
        var config = new Config();
        String scheme = ssl ? "rediss" : "redis";
        BaseConfig<?> server;
        if (environment.matchesProfiles("test & !dev & !prod")) {
            server = config.useSingleServer().setAddress(redisNode(scheme, host + ':' + port)).setDatabase(database);
        } else {
            List<String> nodes = settings.bind("spring.data.redis.cluster.nodes", Bindable.listOf(String.class))
                    .orElse(List.of());
            if (nodes.isEmpty() || database != 0 || username.isBlank() || password.isBlank()) {
                throw new IllegalStateException("Managed Redis requires Cluster nodes, ACL credentials and database zero");
            }
            String[] addresses = nodes.stream().map(node -> redisNode(scheme, node)).toArray(String[]::new);
            server = config.useClusterServers().addNodeAddress(addresses).setReadMode(ReadMode.MASTER);
        }
        server.setConnectTimeout(Math.toIntExact(connectTimeout.toMillis())).setTimeout(Math.toIntExact(timeout.toMillis()));
        if (!username.isBlank()) {
            server.setUsername(username);
        }
        if (!password.isBlank()) {
            server.setPassword(password);
        }
        return Redisson.create(config);
    }

    private static String redisNode(String scheme, String address) {
        URI node;
        try {
            node = URI.create(scheme + "://" + address);
        } catch (IllegalArgumentException malformed) {
            throw new IllegalStateException("Invalid Redis node address");
        }
        if (node.getHost() == null || node.getPort() < 1 || node.getPort() > 65535 || !node.getRawPath().isEmpty()
                || node.getRawUserInfo() != null || node.getRawQuery() != null || node.getRawFragment() != null) {
            throw new IllegalStateException("Invalid Redis node address");
        }
        return node.toASCIIString();
    }

    /**
     * 组件关闭时（如无 Redis 的 test 剖面）Spring Cache 仍要有确定归属的管理器，
     * 否则 classpath 上任意 JSR-107 provider 会抢占 Boot 的默认缓存装配。
     */
    @Bean("cacheManager")
    @ConditionalOnProperty(prefix = EgonColaCacheProperties.PREFIX, name = "enabled", havingValue = "false")
    CacheManager inMemoryCacheManager() {
        return new ConcurrentMapCacheManager();
    }
}
