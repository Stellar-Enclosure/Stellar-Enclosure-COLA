package top.egon.cola.archetype.source.lightopen.application.user.manage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.egon.cola.archetype.source.lightopen.application.user.manage.RoleManage;
import top.egon.cola.archetype.source.lightopen.application.user.pojo.command.AssignRoleCommand;
import top.egon.cola.archetype.source.lightopen.application.user.pojo.convertor.UserApplicationConvertor;
import top.egon.cola.archetype.source.lightopen.application.user.pojo.result.UserResult;
import top.egon.cola.archetype.source.lightopen.application.user.validators.UserApplicationValidator;
import top.egon.cola.archetype.source.lightopen.domain.user.service.RoleDomainService;
import top.egon.cola.archetype.source.lightopen.domain.user.service.UserDomainService;
import top.egon.cola.archetype.source.lightopen.domain.user.service.UserEventService;
import top.egon.cola.archetype.source.lightopen.domain.user.service.UserIdempotencyService;
import top.egon.cola.archetype.source.lightopen.application.user.executor.command.AssignRoleCmdExe;

@Service("roleManageImpl")
@Lazy
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RoleManageImpl implements RoleManage {
    @Qualifier("assignRoleCmdExe")
    private final AssignRoleCmdExe assignRoleCmdExe;

    public RoleManageImpl(RoleDomainService roleDomainService,
            UserDomainService userDomainService,
            UserEventService userEventService,
            UserIdempotencyService userIdempotencyService,
            UserApplicationValidator applicationValidator,
            UserApplicationConvertor convertor) {
        this(new AssignRoleCmdExe(roleDomainService, userDomainService, userEventService, userIdempotencyService, applicationValidator, convertor));
    }

    @Override
    @Transactional
    public UserResult assignRole(AssignRoleCommand command) {
        return assignRoleCmdExe.execute(command);
    }
}
