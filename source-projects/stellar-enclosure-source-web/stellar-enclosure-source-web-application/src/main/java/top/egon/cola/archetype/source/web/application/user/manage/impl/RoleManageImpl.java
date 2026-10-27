package top.egon.cola.archetype.source.web.application.user.manage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.archetype.source.web.application.user.manage.RoleManage;
import top.egon.cola.archetype.source.web.application.user.pojo.command.AssignRoleCommand;
import top.egon.cola.archetype.source.web.application.user.validators.UserApplicationValidator;
import top.egon.cola.archetype.source.web.domain.service.CommandIdempotencyService;
import top.egon.cola.archetype.source.web.domain.service.OrganizationEventService;
import top.egon.cola.archetype.source.web.domain.user.entities.Role;
import top.egon.cola.archetype.source.web.domain.user.service.UserDomainService;
import top.egon.cola.archetype.source.web.application.user.executor.command.AssignRoleCmdExe;

/** Role assignment use case; the aggregate keeps the invariant that the cache and MQ cannot express. */
@Service("roleManage")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RoleManageImpl implements RoleManage {

    @Qualifier("assignRoleCmdExe")
    private final AssignRoleCmdExe assignRoleCmdExe;

    public RoleManageImpl(UserDomainService userDomainService,
            UserApplicationValidator validator,
            CommandIdempotencyService idempotency,
            OrganizationEventService eventService) {
        this(new AssignRoleCmdExe(userDomainService, idempotency, eventService));
    }

    @Override
    @Transactional
    public void assignRole(AssignRoleCommand command) {
        assignRoleCmdExe.execute(command);
    }

}
