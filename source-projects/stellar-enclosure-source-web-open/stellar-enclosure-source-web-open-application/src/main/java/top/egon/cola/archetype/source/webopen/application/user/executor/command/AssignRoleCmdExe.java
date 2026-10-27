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
import top.egon.cola.archetype.source.webopen.application.user.pojo.command.AssignRoleCommand;
import top.egon.cola.archetype.source.webopen.application.user.validators.UserApplicationValidator;
import top.egon.cola.archetype.source.webopen.common.enums.OrganizationFailureType;
import top.egon.cola.archetype.source.webopen.common.exception.OrganizationApplicationException;
import top.egon.cola.archetype.source.webopen.domain.service.CommandIdempotencyService;
import top.egon.cola.archetype.source.webopen.domain.service.OrganizationEventService;
import top.egon.cola.archetype.source.webopen.domain.user.aggregates.UserAggregate;
import top.egon.cola.archetype.source.webopen.domain.user.entities.Role;
import top.egon.cola.archetype.source.webopen.domain.user.entities.User;
import top.egon.cola.archetype.source.webopen.domain.user.events.RoleAssignedEvent;
import top.egon.cola.archetype.source.webopen.domain.user.service.UserDomainService;
import top.egon.cola.archetype.source.webopen.domain.user.vos.RoleCode;
import top.egon.cola.archetype.source.webopen.domain.user.vos.UserId;
import top.egon.cola.component.common.id.snowflake.SnowflakeIdGenerator;
import java.time.Instant;

@Service("assignRoleCmdExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AssignRoleCmdExe {

    @Qualifier("userDomainService")
    private final UserDomainService userDomainService;

    @Qualifier("userApplicationValidator")
    private final UserApplicationValidator validator;

    @Qualifier("commandIdempotencyService")
    private final CommandIdempotencyService idempotency;

    @Qualifier("organizationEventService")
    private final OrganizationEventService eventService;

    @Transactional
    public void execute(AssignRoleCommand command) {
        IdempotentCommand.execute(idempotency, "assign-role", command.requestId(), () -> {
            validator.requireOrganizationAdmin();
            User user = userDomainService.findById(new UserId(command.userId()))
                    .orElseThrow(() -> notFound("user not found"));
            Role role = userDomainService.findRoleByCode(new RoleCode(command.roleCode()))
                    .orElseThrow(() -> notFound("role not found"));
            UserAggregate aggregate = new UserAggregate(user);
            aggregate.assignRole(role);
            userDomainService.save(aggregate.user());
            OrganizationTransactionHooks.afterCommit(() -> eventService.publish(
                    new RoleAssignedEvent(
                            Long.toString(SnowflakeIdGenerator.nextLongId()),
                            user.id().value(), Instant.now(), role.code().value())));
        });
    }

    private static OrganizationApplicationException notFound(String message) {
        return new OrganizationApplicationException(
                OrganizationFailureType.NOT_FOUND, "ORG_NOT_FOUND", message);
    }
}
