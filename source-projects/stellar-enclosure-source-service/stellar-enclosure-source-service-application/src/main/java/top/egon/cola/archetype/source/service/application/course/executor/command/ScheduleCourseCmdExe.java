package top.egon.cola.archetype.source.service.application.course.executor.command;

import top.egon.cola.archetype.source.service.application.course.pojo.command.ScheduleCourseCommand;
import top.egon.cola.archetype.source.service.application.course.pojo.convertor.CourseApplicationConverter;
import top.egon.cola.archetype.source.service.application.course.pojo.result.CourseScheduleResult;
import top.egon.cola.archetype.source.service.application.course.validators.CourseApplicationValidator;
import top.egon.cola.archetype.source.service.common.enums.ApplicationErrorCode;
import top.egon.cola.archetype.source.service.common.exception.ApplicationException;
import top.egon.cola.archetype.source.service.domain.course.entities.Course;
import top.egon.cola.archetype.source.service.domain.course.entities.CourseSchedule;
import top.egon.cola.archetype.source.service.domain.course.service.CourseEventService;
import top.egon.cola.archetype.source.service.domain.course.service.CourseDomainService;
import top.egon.cola.archetype.source.service.domain.course.vos.CourseId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service("scheduleCourseCmdExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ScheduleCourseCmdExe {

    @Qualifier("courseEventService")
    private final CourseEventService courseEventService;

    @Qualifier("courseDomainService")
    private final CourseDomainService courseDomainService;

    @Qualifier("courseApplicationConverterImpl")
    private final CourseApplicationConverter converter;

    @Qualifier("courseApplicationValidator")
    private final CourseApplicationValidator validator;

    @Transactional
    public CourseScheduleResult execute(ScheduleCourseCommand command) {
        validator.require(command != null, "schedule course command is required");
        CourseId courseId = new CourseId(command.courseId());
        Course course = courseDomainService.findById(courseId)
                .orElseThrow(() -> new ApplicationException(
                        ApplicationErrorCode.COURSE_NOT_FOUND, "course not found"));
        CourseSchedule schedule = courseDomainService.scheduleCourse(
                course, command.classId(), command.startsAt(), command.endsAt(),
                courseDomainService.findOverlapping(
                        courseId, command.classId(), command.startsAt(), command.endsAt()));
        CourseSchedule saved = courseDomainService.saveSchedule(schedule);
        courseEventService.courseScheduled(saved);
        return converter.toResult(saved);
    }
}
