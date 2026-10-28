package top.egon.cola.archetype.source.lightopen.support;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import top.egon.cola.archetype.source.lightopen.infrastructure.user.repo.UserRepository;
import top.egon.cola.archetype.source.lightopen.infrastructure.user.po.UserPO;
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
}
