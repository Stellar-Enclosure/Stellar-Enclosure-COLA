package top.egon.cola.archetype.source.web.application.teaching.executor.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.archetype.source.web.application.support.IdempotentCommand;
import top.egon.cola.archetype.source.web.application.support.OrganizationTransactionHooks;
import top.egon.cola.archetype.source.web.application.teaching.pojo.command.AssignUserToClassCommand;
import top.egon.cola.archetype.source.web.common.enums.OrganizationFailureType;
import top.egon.cola.archetype.source.web.common.exception.OrganizationApplicationException;
import top.egon.cola.archetype.source.web.common.exception.OrganizationDomainException;
import top.egon.cola.archetype.source.web.domain.service.CommandIdempotencyService;
import top.egon.cola.archetype.source.web.domain.service.OrganizationEventService;
import top.egon.cola.archetype.source.web.domain.teaching.aggregates.SchoolClassAggregate;
import top.egon.cola.archetype.source.web.domain.teaching.entities.SchoolClass;
import top.egon.cola.archetype.source.web.domain.teaching.events.SchoolClassMembershipChangedEvent;
import top.egon.cola.archetype.source.web.domain.teaching.service.SchoolClassDomainService;
import top.egon.cola.archetype.source.web.domain.teaching.vos.SchoolClassId;
import top.egon.cola.archetype.source.web.domain.user.entities.User;
import top.egon.cola.archetype.source.web.domain.user.service.UserDomainService;
import top.egon.cola.archetype.source.web.domain.user.vos.UserId;
import top.egon.cola.component.common.id.snowflake.SnowflakeIdGenerator;
import java.time.Instant;

@Service("assignUserToClassCmdExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AssignUserToClassCmdExe {

    @Qualifier("schoolClassDomainService")
    private final SchoolClassDomainService schoolClassDomainService;

    @Qualifier("userDomainService")
    private final UserDomainService userDomainService;

    @Qualifier("commandIdempotencyService")
    private final CommandIdempotencyService idempotency;

    @Qualifier("organizationEventService")
    private final OrganizationEventService eventService;

    @Transactional
    public void execute(AssignUserToClassCommand command) {
        IdempotentCommand.execute(idempotency, "assign-user-to-school-class", command.requestId(), () -> {
            SchoolClassId classId = new SchoolClassId(command.schoolClassId());
            UserId memberId = new UserId(command.userId());
            User user = userDomainService.findById(memberId).orElseThrow(() -> notFound("user not found"));
            SchoolClass schoolClass = schoolClassDomainService.findByGradeIdAndId(command.gradeId(), classId)
                    .orElseThrow(() -> notFound("school class not found"));
            try {
                new SchoolClassAggregate(schoolClass).validateAssignment(user);
            } catch (OrganizationDomainException failure) {
                throw new OrganizationApplicationException(
                        OrganizationFailureType.DOMAIN_REJECTED,
                        "ORG_DOMAIN_REJECTED",
                        failure.getMessage());
            }
            if (schoolClassDomainService.hasUser(command.gradeId(), classId, memberId)) {
                throw conflict("user already assigned to school class");
            }
            schoolClassDomainService.addUser(command.gradeId(), classId, memberId);
            OrganizationTransactionHooks.afterCommit(() -> eventService.publish(
                    new SchoolClassMembershipChangedEvent(
                            Long.toString(SnowflakeIdGenerator.nextLongId()),
                            classId.value(), Instant.now(), memberId.value(), "ASSIGNED")));
        });
    }

    private static OrganizationApplicationException conflict(String message) {
        return new OrganizationApplicationException(
                OrganizationFailureType.CONFLICT, "ORG_CONFLICT", message);
    }

    private static OrganizationApplicationException notFound(String message) {
        return new OrganizationApplicationException(
                OrganizationFailureType.NOT_FOUND, "ORG_NOT_FOUND", message);
    }
}
