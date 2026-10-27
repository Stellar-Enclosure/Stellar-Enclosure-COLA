package top.egon.cola.archetype.source.light.application.user.manage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.egon.cola.archetype.source.light.application.user.manage.PermissionManage;
import top.egon.cola.archetype.source.light.application.user.pojo.command.GrantPermissionCommand;
import top.egon.cola.archetype.source.light.application.user.pojo.convertor.UserApplicationConvertor;
import top.egon.cola.archetype.source.light.application.user.pojo.query.GetUserPermissionsQuery;
import top.egon.cola.archetype.source.light.application.user.pojo.result.PermissionDetailResult;
import top.egon.cola.archetype.source.light.application.user.pojo.result.PermissionResult;
import top.egon.cola.archetype.source.light.application.user.validators.UserApplicationValidator;
import top.egon.cola.archetype.source.light.domain.user.service.PermissionDomainService;
import top.egon.cola.archetype.source.light.domain.user.service.RoleDomainService;
import top.egon.cola.archetype.source.light.domain.user.service.UserEventService;
import top.egon.cola.archetype.source.light.domain.user.service.UserIdempotencyService;
import java.util.List;
import top.egon.cola.archetype.source.light.application.user.executor.command.GrantPermissionCmdExe;
import top.egon.cola.archetype.source.light.application.user.executor.query.GetUserPermissionsQueryExe;

@Service("permissionManageImpl")
@Lazy
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PermissionManageImpl implements PermissionManage {
    @Qualifier("grantPermissionCmdExe")
    private final GrantPermissionCmdExe grantPermissionCmdExe;

    @Qualifier("getUserPermissionsQueryExe")
    private final GetUserPermissionsQueryExe getUserPermissionsQueryExe;

    public PermissionManageImpl(PermissionDomainService permissionDomainService,
            RoleDomainService roleDomainService,
            UserEventService userEventService,
            UserIdempotencyService userIdempotencyService,
            UserApplicationValidator applicationValidator,
            UserApplicationConvertor convertor) {
        this(new GrantPermissionCmdExe(permissionDomainService, roleDomainService, userEventService, userIdempotencyService, applicationValidator, convertor),
                new GetUserPermissionsQueryExe(permissionDomainService));
    }

    @Override
    @Transactional
    public PermissionResult grantPermission(GrantPermissionCommand command) {
        return grantPermissionCmdExe.execute(command);
    }

    @Override
    public List<PermissionDetailResult> getByUser(GetUserPermissionsQuery query) {
        return getUserPermissionsQueryExe.execute(query);
    }
}
