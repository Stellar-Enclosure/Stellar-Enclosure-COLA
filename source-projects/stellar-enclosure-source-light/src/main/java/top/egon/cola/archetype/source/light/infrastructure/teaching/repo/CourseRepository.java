package top.egon.cola.archetype.source.light.infrastructure.teaching.repo;

import top.egon.cola.archetype.source.light.infrastructure.teaching.dao.CourseDAO;
import top.egon.cola.archetype.source.light.infrastructure.teaching.po.CoursePO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.component.common.mybatis.autoconfigure.EgonColaMybatisPlusProperties;
import top.egon.cola.component.common.mybatis.extension.EgonColaRepository;
import top.egon.cola.component.common.mybatis.business.EgonColaTenantIdProvider;
import top.egon.cola.component.common.cache.consistency.EgonColaCacheMutationExecutor;
import java.util.Collection;
import java.util.List;

/** Logical persistence boundary; query implementations remain explicit Mapper SQL. */
@Slf4j
@Validated
@Repository("courseRepository")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@CacheConfig(cacheNames = "CoursePO")
public class CourseRepository extends EgonColaRepository<CourseDAO, CoursePO> {
    @Getter
    @Qualifier("courseDAO")
    private final CourseDAO baseMapper;
    @Getter(AccessLevel.PROTECTED)
    @Qualifier("egon.cola.component.mybatis-plus-top.egon.cola.component.common.mybatis.autoconfigure.EgonColaMybatisPlusProperties")
    private final EgonColaMybatisPlusProperties properties;

    @Qualifier("egonColaCacheMutationExecutor")
    private final ObjectProvider<EgonColaCacheMutationExecutor> cacheMutations;

    public List<CoursePO> selectByCourseCode(@NotBlank String courseCode) {
        return getBaseMapper().selectByCourseCode(courseCode);
    }

    /**
     * 通过 Spring 代理调用；sync 模式缓存短 TTL 空值，不与 unless 混用。
     */
    @Cacheable(keyGenerator = "egonColaRepositoryKeyGenerator", sync = true)
    public CoursePO findCachedById(@NotNull @Positive Long id) {
        return getById(id);
    }

    /**
     * 缓存写入口必须与读入口使用相同区域和 Key；普通 CRUD 不再隐式失效缓存。
     */
    @CacheEvict(keyGenerator = "egonColaRepositoryKeyGenerator", condition = "#result && !#root.target.componentCacheMutationEnabled()")
    public boolean updateCachedById(@NotNull CoursePO entity) {
        return mutateCache(entity, () -> updateById(entity));
    }

    @CacheEvict(keyGenerator = "egonColaRepositoryKeyGenerator", condition = "#result && !#root.target.componentCacheMutationEnabled()")
    public boolean saveCached(@NotNull CoursePO entity) {
        return mutateCache(entity, () -> save(entity));
    }

    @CacheEvict(keyGenerator = "egonColaRepositoryKeyGenerator", condition = "#result && !#root.target.componentCacheMutationEnabled()")
    public boolean removeCachedById(@NotNull CoursePO entity) {
        return mutateCache(entity, () -> removeById(entity));
    }

    public boolean componentCacheMutationEnabled() {
        return cacheMutations.getIfAvailable() != null;
    }

    private boolean mutateCache(CoursePO entity, java.util.function.Supplier<Boolean> write) {
        long tenant = EgonColaTenantIdProvider.currentTenantId();
        if (entity == null || entity.getId() == null || entity.getId() <= 0
                || (entity.getTenantId() != null && entity.getTenantId() != tenant)) {
            throw new IllegalArgumentException("CACHE_KEY_TENANT_MISMATCH");
        }
        EgonColaCacheMutationExecutor executor = cacheMutations.getIfAvailable();
        return executor == null ? write.get() : executor.execute("CoursePO", List.of(tenant + ":" + entity.getId()), write);
    }
}
