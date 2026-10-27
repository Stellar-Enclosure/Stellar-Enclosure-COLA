package top.egon.cola.archetype.source.light.application.teaching.executor.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.egon.cola.archetype.source.light.application.teaching.pojo.command.ScheduleCourseCommand;
import top.egon.cola.archetype.source.light.application.teaching.pojo.convertor.TeachingApplicationConvertor;
import top.egon.cola.archetype.source.light.application.teaching.pojo.result.SchoolClassResult;
import top.egon.cola.archetype.source.light.application.teaching.validators.TeachingApplicationValidator;
import top.egon.cola.archetype.source.light.common.exception.TeachingDomainException;
import top.egon.cola.archetype.source.light.common.exception.TeachingUseCaseException;
import top.egon.cola.archetype.source.light.domain.teaching.aggregates.SchoolClassAggregate;
import top.egon.cola.archetype.source.light.domain.teaching.entities.Course;
import top.egon.cola.archetype.source.light.domain.teaching.service.CourseDomainService;
import top.egon.cola.archetype.source.light.domain.teaching.service.CourseIdempotencyService;
import top.egon.cola.archetype.source.light.domain.teaching.service.SchoolClassDomainService;
import top.egon.cola.archetype.source.light.domain.teaching.service.TeachingEventService;
import top.egon.cola.archetype.source.light.domain.teaching.vos.CourseScheduleBO;
import top.egon.cola.archetype.source.light.domain.teaching.vos.SchoolClassId;
import top.egon.cola.archetype.source.light.domain.teaching.vos.TeachingEvent;

@Service("scheduleCourseCmdExe")
@Lazy
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ScheduleCourseCmdExe {

    @Qualifier("schoolClassDomainService")
    private final SchoolClassDomainService schoolClassDomainService;

    @Qualifier("courseDomainService")
    private final CourseDomainService courseDomainService;

    @Qualifier("teachingEventService")
    private final TeachingEventService teachingEventService;

    @Qualifier("courseIdempotencyService")
    private final CourseIdempotencyService courseIdempotencyService;

    @Qualifier("teachingApplicationValidator")
    private final TeachingApplicationValidator applicationValidator;

    @Qualifier("teachingApplicationConvertorImpl")
    private final TeachingApplicationConvertor convertor;

    @Transactional
    public SchoolClassResult execute(ScheduleCourseCommand command) {
        applicationValidator.validate(command);
        claim(command.idempotencyKey());
        SchoolClassAggregate aggregate = schoolClassDomainService
                .findAggregateById(new SchoolClassId(command.schoolClassId()))
                .orElseThrow(() -> new TeachingUseCaseException("CLASS_NOT_FOUND", "class not found"));
        Course course = courseDomainService.findById(command.courseId())
                .orElseThrow(() -> new TeachingUseCaseException("COURSE_NOT_FOUND", "course not found"));
        CourseScheduleBO schedule = convertor.toSchedule(command, course);
        try {
            SchoolClassAggregate scheduled = schoolClassDomainService.schedule(aggregate, course, schedule);
            schoolClassDomainService.saveAggregate(scheduled);
            teachingEventService.publish(TeachingEvent.courseScheduled(command.schoolClassId()));
            return convertor.toSchoolClassResult(scheduled);
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
