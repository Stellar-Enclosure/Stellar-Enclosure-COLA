package top.egon.cola.archetype.source.light.application.teaching.executor.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.egon.cola.archetype.source.light.application.teaching.pojo.command.CreateSchoolClassCommand;
import top.egon.cola.archetype.source.light.application.teaching.pojo.convertor.TeachingApplicationConvertor;
import top.egon.cola.archetype.source.light.application.teaching.pojo.result.SchoolClassResult;
import top.egon.cola.archetype.source.light.application.teaching.validators.TeachingApplicationValidator;
import top.egon.cola.archetype.source.light.common.exception.TeachingDomainException;
import top.egon.cola.archetype.source.light.common.exception.TeachingUseCaseException;
import top.egon.cola.archetype.source.light.domain.teaching.entities.SchoolClass;
import top.egon.cola.archetype.source.light.domain.teaching.service.CourseIdempotencyService;
import top.egon.cola.archetype.source.light.domain.teaching.service.SchoolClassDomainService;
import top.egon.cola.archetype.source.light.domain.teaching.service.TeachingEventService;
import top.egon.cola.archetype.source.light.domain.teaching.vos.Semester;
import top.egon.cola.archetype.source.light.domain.teaching.vos.TeachingEvent;

@Service("createSchoolClassCmdExe")
@Lazy
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CreateSchoolClassCmdExe {

    @Qualifier("schoolClassDomainService")
    private final SchoolClassDomainService schoolClassDomainService;

    @Qualifier("teachingEventService")
    private final TeachingEventService teachingEventService;

    @Qualifier("courseIdempotencyService")
    private final CourseIdempotencyService courseIdempotencyService;

    @Qualifier("teachingApplicationValidator")
    private final TeachingApplicationValidator applicationValidator;

    @Qualifier("teachingApplicationConvertorImpl")
    private final TeachingApplicationConvertor convertor;

    @Transactional
    public SchoolClassResult execute(CreateSchoolClassCommand command) {
        applicationValidator.validate(command);
        claim(command.idempotencyKey());
        try {
            SchoolClass saved = schoolClassDomainService.save(
                    schoolClassDomainService.createSchoolClass(command.name(), new Semester(command.semester())));
            teachingEventService.publish(TeachingEvent.classCreated(saved.id().value()));
            return convertor.toSchoolClassResult(saved);
        } catch (TeachingDomainException exception) {
            throw translate(exception);
        }
    }

    private void claim(String idempotencyKey) {
        if (!courseIdempotencyService.claim(idempotencyKey)) {
            throw new TeachingUseCaseException("DUPLICATE_REQUEST", "request was already processed");
        }
    }

    private TeachingUseCaseException translate(TeachingDomainException exception) {
        return new TeachingUseCaseException(exception.getStatus(), exception.getMessage(), exception);
    }
}
