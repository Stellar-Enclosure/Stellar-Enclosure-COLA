package top.egon.cola.archetype.source.light.application.user.executor.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.light.application.user.pojo.query.GetUserPermissionsQuery;
import top.egon.cola.archetype.source.light.application.user.pojo.result.PermissionDetailResult;
import top.egon.cola.archetype.source.light.domain.user.service.PermissionDomainService;
import top.egon.cola.archetype.source.light.domain.user.vos.UserId;
import java.util.List;

@Service("getUserPermissionsQueryExe")
@Lazy
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GetUserPermissionsQueryExe {

    @Qualifier("permissionDomainService")
    private final PermissionDomainService permissionDomainService;

    public List<PermissionDetailResult> execute(GetUserPermissionsQuery query) {
        return permissionDomainService.findByUserId(new UserId(query.userId())).stream()
                .map(permission -> new PermissionDetailResult(
                        permission.code().value(), permission.name()))
                .toList();
    }
}
