package top.egon.cola.archetype.source.webopen.application.teaching.executor.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.archetype.source.webopen.application.support.IdempotentCommand;
import top.egon.cola.archetype.source.webopen.application.support.OrganizationTransactionHooks;
import top.egon.cola.archetype.source.webopen.application.teaching.pojo.command.CreateSchoolClassCommand;
import top.egon.cola.archetype.source.webopen.application.teaching.pojo.convertor.SchoolClassConverter;
import top.egon.cola.archetype.source.webopen.application.teaching.pojo.result.SchoolClassDetailResult;
import top.egon.cola.archetype.source.webopen.application.teaching.validators.TeachingApplicationValidator;
import top.egon.cola.archetype.source.webopen.common.enums.OrganizationFailureType;
import top.egon.cola.archetype.source.webopen.common.exception.OrganizationApplicationException;
import top.egon.cola.archetype.source.webopen.domain.service.CommandIdempotencyService;
import top.egon.cola.archetype.source.webopen.domain.service.OrganizationEventService;
import top.egon.cola.archetype.source.webopen.domain.teaching.entities.Grade;
import top.egon.cola.archetype.source.webopen.domain.teaching.entities.SchoolClass;
import top.egon.cola.archetype.source.webopen.domain.teaching.events.SchoolClassChangedEvent;
import top.egon.cola.archetype.source.webopen.domain.teaching.service.SchoolClassDomainService;
import top.egon.cola.archetype.source.webopen.domain.teaching.vos.GradeCode;
import top.egon.cola.archetype.source.webopen.domain.teaching.vos.SchoolClassId;
import top.egon.cola.component.common.id.snowflake.SnowflakeIdGenerator;
import java.time.Instant;

@Service("createSchoolClassCmdExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CreateSchoolClassCmdExe {

    @Qualifier("schoolClassDomainService")
    private final SchoolClassDomainService schoolClassDomainService;

    @Qualifier("teachingApplicationValidator")
    private final TeachingApplicationValidator validator;

    @Qualifier("commandIdempotencyService")
    private final CommandIdempotencyService idempotency;

    @Qualifier("organizationEventService")
    private final OrganizationEventService eventService;

    @Qualifier("schoolClassConverterImpl")
    private final SchoolClassConverter converter;

    @Transactional
    public SchoolClassDetailResult execute(CreateSchoolClassCommand command) {
        return IdempotentCommand.execute(idempotency, "create-school-class", command.requestId(), () -> {
            validator.requireTeachingAdmin();
            Grade grade = schoolClassDomainService.findGradeByCode(GradeCode.create(command.gradeCode()))
                    .orElseThrow(() -> notFound("grade not found"));
            if (schoolClassDomainService.existsByGradeIdAndNameIgnoreCase(grade.id(), command.name().trim())) {
                throw conflict("school class name already exists in grade");
            }
            SchoolClass schoolClass = schoolClassDomainService.save(schoolClassDomainService.create(
                    new SchoolClassId(SnowflakeIdGenerator.nextLongId()), command.name(), grade));
            OrganizationTransactionHooks.afterCommit(() -> eventService.publish(
                    new SchoolClassChangedEvent(
                            Long.toString(SnowflakeIdGenerator.nextLongId()),
                            schoolClass.id().value(), Instant.now(), schoolClass.gradeId(), "CREATED")));
            return converter.toResult(schoolClass);
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
