package top.egon.cola.archetype.source.serviceopen.application.course.executor.command;

import top.egon.cola.archetype.source.serviceopen.application.course.pojo.command.CreateCourseCommand;
import top.egon.cola.archetype.source.serviceopen.application.course.pojo.convertor.CourseApplicationConverter;
import top.egon.cola.archetype.source.serviceopen.application.course.pojo.result.CourseResult;
import top.egon.cola.archetype.source.serviceopen.application.course.validators.CourseApplicationValidator;
import top.egon.cola.archetype.source.serviceopen.common.enums.ApplicationErrorCode;
import top.egon.cola.archetype.source.serviceopen.common.exception.ApplicationException;
import top.egon.cola.archetype.source.serviceopen.domain.course.entities.Course;
import top.egon.cola.archetype.source.serviceopen.domain.course.service.CourseDomainService;
import top.egon.cola.archetype.source.serviceopen.domain.course.vos.CourseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service("createCourseCmdExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CreateCourseCmdExe {

    @Qualifier("courseDomainService")
    private final CourseDomainService courseDomainService;

    @Qualifier("courseApplicationConverterImpl")
    private final CourseApplicationConverter converter;

    @Qualifier("courseApplicationValidator")
    private final CourseApplicationValidator validator;

    @Transactional
    public CourseResult execute(CreateCourseCommand command) {
        validator.require(command != null, "create course command is required");
        CourseCode code = new CourseCode(command.code());
        if (courseDomainService.existsByCode(code)) {
            throw new ApplicationException(
                    ApplicationErrorCode.COURSE_CODE_DUPLICATED, "course code already exists");
        }
        Course course = courseDomainService.createCourse(code, command.name(), command.credit());
        return converter.toResult(courseDomainService.save(course));
    }
}
