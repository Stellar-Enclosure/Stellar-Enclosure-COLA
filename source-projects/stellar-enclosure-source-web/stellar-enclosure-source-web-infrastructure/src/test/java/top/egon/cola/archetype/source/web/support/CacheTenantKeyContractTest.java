package top.egon.cola.archetype.source.web.support;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import top.egon.cola.archetype.source.web.infrastructure.user.repo.UserRepository;
import top.egon.cola.archetype.source.web.infrastructure.user.po.UserPO;
import top.egon.cola.archetype.source.web.infrastructure.user.dao.UserDAO;
import top.egon.cola.component.common.mybatis.business.EgonColaTenantIdProvider;
import top.egon.cola.component.common.mybatis.cache.EgonColaRepositoryKeyGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CacheTenantKeyContractTest {

    @Test
    void readsAndWritesUseTheConfiguredTenantContext() throws Exception {
        var read = UserRepository.class.getMethod("findCachedById", Long.class);
        var update = UserRepository.class.getMethod("updateCachedById", UserPO.class);
        assertEquals("egonColaRepositoryKeyGenerator", read.getAnnotation(Cacheable.class).keyGenerator());
        assertEquals("egonColaRepositoryKeyGenerator", update.getAnnotation(CacheEvict.class).keyGenerator());
        var generator = new EgonColaRepositoryKeyGenerator();
        try {
            EgonColaTenantIdProvider.initialize("customTenant");
            MDC.put("customTenant", "42");
            MDC.put("tenantId", "999");
            var entity = new UserPO();
            entity.setId(17L);
            entity.setTenantId(42L);
            assertEquals("42:17", generator.generate(null, read, 17L));
            assertEquals("42:17", generator.generate(null, update, entity));
            entity.setTenantId(999L);
            assertThrows(IllegalArgumentException.class, () -> generator.generate(null, update, entity));
        } finally {
            MDC.remove("customTenant");
            MDC.remove("tenantId");
            EgonColaTenantIdProvider.initialize(EgonColaTenantIdProvider.DEFAULT_MDC_KEY);
        }
    }
    @Test
    @SuppressWarnings("unchecked")
    void cachedWritesUseConfiguredMutationStrategyAndFallback() {
        var provider = (org.springframework.beans.factory.ObjectProvider<top.egon.cola.component.common.cache.consistency.EgonColaCacheMutationExecutor>)
                org.mockito.Mockito.mock(org.springframework.beans.factory.ObjectProvider.class);
        var mutations = org.mockito.Mockito.mock(top.egon.cola.component.common.cache.consistency.EgonColaCacheMutationExecutor.class);
        var dao = org.mockito.Mockito.mock(UserDAO.class);
        var target = new UserRepository(dao, new top.egon.cola.component.common.mybatis.autoconfigure.EgonColaMybatisPlusProperties(), provider);
        var manager = new org.springframework.cache.concurrent.ConcurrentMapCacheManager("UserPO");
        var beans = new org.springframework.beans.factory.support.DefaultListableBeanFactory();
        beans.registerSingleton("egonColaRepositoryKeyGenerator", new EgonColaRepositoryKeyGenerator());
        var interceptor = new org.springframework.cache.interceptor.CacheInterceptor();
        interceptor.setCacheManager(manager);
        interceptor.setCacheOperationSources(new org.springframework.cache.annotation.AnnotationCacheOperationSource());
        interceptor.setBeanFactory(beans);
        interceptor.afterPropertiesSet();
        interceptor.afterSingletonsInstantiated();
        var proxies = new org.springframework.aop.framework.ProxyFactory(target);
        proxies.setProxyTargetClass(true);
        proxies.addAdvice(interceptor);
        var repository = (UserRepository) proxies.getProxy();
        try (var validators = jakarta.validation.Validation.buildDefaultValidatorFactory();
             var binding = top.egon.cola.component.common.mybatis.model.EgonColaModelValidationUtils.initialize(
                     new top.egon.cola.component.common.core.validation.ValidationUtils(validators.getValidator()), new Object())) {
            EgonColaTenantIdProvider.initialize("customTenant");
            MDC.put("customTenant", "42");
            var entity = new UserPO();
            entity.setId(17L);
            entity.setTenantId(42L);
            entity.setVersion(1L);
            var cache = manager.getCache("UserPO");
            cache.put("42:17", "old");
            org.mockito.Mockito.when(provider.getIfAvailable()).thenReturn(mutations);
            org.mockito.Mockito.when(mutations.execute(org.mockito.ArgumentMatchers.eq("UserPO"),
                    org.mockito.ArgumentMatchers.eq(java.util.List.of("42:17")), org.mockito.ArgumentMatchers.any())).thenAnswer(call -> ((java.util.function.Supplier<Boolean>) call.getArgument(2)).get())
                    .thenReturn(true, true);
            org.mockito.Mockito.when(dao.updateById(entity)).thenReturn(1);
            org.junit.jupiter.api.Assertions.assertTrue(repository.updateCachedById(entity));
            org.junit.jupiter.api.Assertions.assertTrue(repository.saveCached(entity));
            org.junit.jupiter.api.Assertions.assertTrue(repository.removeCachedById(entity));
            org.mockito.Mockito.verify(mutations, org.mockito.Mockito.times(3)).execute(org.mockito.ArgumentMatchers.eq("UserPO"),
                    org.mockito.ArgumentMatchers.eq(java.util.List.of("42:17")), org.mockito.ArgumentMatchers.any());
            org.junit.jupiter.api.Assertions.assertNotNull(cache.get("42:17"), "fallback eviction must not override the configured strategy");
            org.mockito.Mockito.when(provider.getIfAvailable()).thenReturn(null);
            org.junit.jupiter.api.Assertions.assertTrue(repository.updateCachedById(entity));
            org.mockito.Mockito.verify(dao, org.mockito.Mockito.times(2)).updateById(entity);
            org.junit.jupiter.api.Assertions.assertNull(cache.get("42:17"), "ordinary Spring caching is the fallback");
            entity.setTenantId(999L);
            assertThrows(IllegalArgumentException.class, () -> repository.updateCachedById(entity));
            org.mockito.Mockito.verify(dao, org.mockito.Mockito.times(2)).updateById(entity);
        } finally {
            MDC.remove("customTenant");
            EgonColaTenantIdProvider.initialize(EgonColaTenantIdProvider.DEFAULT_MDC_KEY);
        }
    }

}
