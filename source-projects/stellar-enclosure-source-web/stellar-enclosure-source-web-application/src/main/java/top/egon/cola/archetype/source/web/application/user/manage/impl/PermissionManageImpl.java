package top.egon.cola.archetype.source.web.application.user.manage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.archetype.source.web.application.user.manage.PermissionManage;
import top.egon.cola.archetype.source.web.application.user.pojo.command.GrantPermissionCommand;
import top.egon.cola.archetype.source.web.application.user.pojo.convertor.PermissionConverter;
import top.egon.cola.archetype.source.web.application.user.pojo.query.PermissionTreeQuery;
import top.egon.cola.archetype.source.web.application.user.pojo.result.PermissionTreeResult;
import top.egon.cola.archetype.source.web.application.user.validators.UserApplicationValidator;
import top.egon.cola.archetype.source.web.domain.service.CommandIdempotencyService;
import top.egon.cola.archetype.source.web.domain.service.OrganizationEventService;
import top.egon.cola.archetype.source.web.domain.user.entities.Permission;
import top.egon.cola.archetype.source.web.domain.user.service.PermissionDomainService;
import top.egon.cola.archetype.source.web.domain.user.service.UserDomainService;
import top.egon.cola.archetype.source.web.application.user.executor.command.GrantPermissionCmdExe;
import top.egon.cola.archetype.source.web.application.user.executor.query.PermissionTreeQueryExe;

/** Permission grant use case; the granted-code projection is delegated to the named converter. */
@Service("permissionManage")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PermissionManageImpl implements PermissionManage {

    @Qualifier("grantPermissionCmdExe")
    private final GrantPermissionCmdExe grantPermissionCmdExe;

    @Qualifier("permissionTreeQueryExe")
    private final PermissionTreeQueryExe permissionTreeQueryExe;

    public PermissionManageImpl(UserDomainService userDomainService,
            PermissionDomainService permissionDomainService,
            UserApplicationValidator validator,
            PermissionConverter converter,
            CommandIdempotencyService idempotency,
            OrganizationEventService eventService) {
        this(new GrantPermissionCmdExe(userDomainService, permissionDomainService, idempotency, eventService),
                new PermissionTreeQueryExe(permissionDomainService, converter));
    }

    @Override
    @Transactional
    public void grantPermission(GrantPermissionCommand command) {
        grantPermissionCmdExe.execute(command);
    }

    @Override
    public PermissionTreeResult getPermissionTree(PermissionTreeQuery query) {
        return permissionTreeQueryExe.execute(query);
    }

}
