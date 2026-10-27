package top.egon.cola.archetype.source.web.application.user.executor.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.archetype.source.web.application.support.IdempotentCommand;
import top.egon.cola.archetype.source.web.application.support.OrganizationTransactionHooks;
import top.egon.cola.archetype.source.web.application.user.pojo.command.CreateUserCommand;
import top.egon.cola.archetype.source.web.application.user.pojo.convertor.UserConverter;
import top.egon.cola.archetype.source.web.application.user.pojo.result.UserDetailResult;
import top.egon.cola.archetype.source.web.application.user.validators.UserApplicationValidator;
import top.egon.cola.archetype.source.web.common.enums.OrganizationFailureType;
import top.egon.cola.archetype.source.web.common.exception.OrganizationApplicationException;
import top.egon.cola.archetype.source.web.domain.service.CommandIdempotencyService;
import top.egon.cola.archetype.source.web.domain.service.OrganizationEventService;
import top.egon.cola.archetype.source.web.domain.user.entities.User;
import top.egon.cola.archetype.source.web.domain.user.events.UserChangedEvent;
import top.egon.cola.archetype.source.web.domain.user.service.UserDomainService;
import top.egon.cola.archetype.source.web.domain.user.vos.UserId;
import top.egon.cola.component.common.id.snowflake.SnowflakeIdGenerator;
import java.time.Instant;

@Service("createUserCmdExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CreateUserCmdExe {

    @Qualifier("userDomainService")
    private final UserDomainService userDomainService;

    @Qualifier("userApplicationValidator")
    private final UserApplicationValidator validator;

    @Qualifier("userConverterImpl")
    private final UserConverter converter;

    @Qualifier("commandIdempotencyService")
    private final CommandIdempotencyService idempotency;

    @Qualifier("organizationEventService")
    private final OrganizationEventService eventService;

    @Transactional
    public UserDetailResult execute(CreateUserCommand command) {
        return IdempotentCommand.execute(idempotency, "create-user", command.requestId(), () -> {
            String normalizedEmail = validator.normalizedEmail(command.email());
            if (userDomainService.existsByEmail(normalizedEmail)) {
                throw conflict("user email already exists");
            }
            User user = userDomainService.save(userDomainService.create(
                    new UserId(SnowflakeIdGenerator.nextLongId()), command.name(), normalizedEmail));
            OrganizationTransactionHooks.afterCommit(() -> eventService.publish(
                    new UserChangedEvent(
                            Long.toString(SnowflakeIdGenerator.nextLongId()),
                            user.id().value(), Instant.now(), "CREATED")));
            return converter.toResult(user);
        });
    }

    private static OrganizationApplicationException conflict(String message) {
        return new OrganizationApplicationException(
                OrganizationFailureType.CONFLICT, "ORG_CONFLICT", message);
    }
}
