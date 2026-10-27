package top.egon.cola.archetype.source.webopen.application.user.executor.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.archetype.source.webopen.application.support.IdempotentCommand;
import top.egon.cola.archetype.source.webopen.application.support.OrganizationTransactionHooks;
import top.egon.cola.archetype.source.webopen.application.user.pojo.command.GrantPermissionCommand;
import top.egon.cola.archetype.source.webopen.application.user.validators.UserApplicationValidator;
import top.egon.cola.archetype.source.webopen.common.enums.OrganizationFailureType;
import top.egon.cola.archetype.source.webopen.common.exception.OrganizationApplicationException;
import top.egon.cola.archetype.source.webopen.domain.service.CommandIdempotencyService;
import top.egon.cola.archetype.source.webopen.domain.service.OrganizationEventService;
import top.egon.cola.archetype.source.webopen.domain.user.aggregates.RolePermissionAggregate;
import top.egon.cola.archetype.source.webopen.domain.user.entities.Permission;
import top.egon.cola.archetype.source.webopen.domain.user.entities.Role;
import top.egon.cola.archetype.source.webopen.domain.user.events.PermissionGrantedEvent;
import top.egon.cola.archetype.source.webopen.domain.user.service.PermissionDomainService;
import top.egon.cola.archetype.source.webopen.domain.user.service.UserDomainService;
import top.egon.cola.archetype.source.webopen.domain.user.vos.PermissionCode;
import top.egon.cola.archetype.source.webopen.domain.user.vos.RoleCode;
import top.egon.cola.component.common.id.snowflake.SnowflakeIdGenerator;
import java.time.Instant;

@Service("grantPermissionCmdExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GrantPermissionCmdExe {

    @Qualifier("userDomainService")
    private final UserDomainService userDomainService;

    @Qualifier("permissionDomainService")
    private final PermissionDomainService permissionDomainService;

    @Qualifier("userApplicationValidator")
    private final UserApplicationValidator validator;

    @Qualifier("commandIdempotencyService")
    private final CommandIdempotencyService idempotency;

    @Qualifier("organizationEventService")
    private final OrganizationEventService eventService;

    @Transactional
    public void execute(GrantPermissionCommand command) {
        IdempotentCommand.execute(idempotency, "grant-permission", command.requestId(), () -> {
            validator.requireOrganizationAdmin();
            Role role = userDomainService.findRoleByCode(new RoleCode(command.roleCode()))
                    .orElseThrow(() -> notFound("role not found"));
            Permission permission = permissionDomainService.findByCode(new PermissionCode(command.permissionCode()))
                    .orElseThrow(() -> notFound("permission not found"));
            RolePermissionAggregate aggregate = new RolePermissionAggregate(role, role.permissionCodes());
            aggregate.grant(permission);
            userDomainService.saveRole(aggregate.role());
            OrganizationTransactionHooks.afterCommit(() -> eventService.publish(
                    new PermissionGrantedEvent(
                            Long.toString(SnowflakeIdGenerator.nextLongId()),
                            role.id(), Instant.now(), role.code().value(), permission.code().value())));
        });
    }

    private static OrganizationApplicationException notFound(String message) {
        return new OrganizationApplicationException(
                OrganizationFailureType.NOT_FOUND, "ORG_NOT_FOUND", message);
    }
}
