package top.egon.cola.archetype.source.serviceopen.application.course.executor.query;

import top.egon.cola.archetype.source.serviceopen.application.course.pojo.convertor.CourseApplicationConverter;
import top.egon.cola.archetype.source.serviceopen.application.course.pojo.query.GetCourseQuery;
import top.egon.cola.archetype.source.serviceopen.application.course.pojo.result.CourseResult;
import top.egon.cola.archetype.source.serviceopen.application.course.validators.CourseApplicationValidator;
import top.egon.cola.archetype.source.serviceopen.common.enums.ApplicationErrorCode;
import top.egon.cola.archetype.source.serviceopen.common.exception.ApplicationException;
import top.egon.cola.archetype.source.serviceopen.domain.course.service.CourseDomainService;
import top.egon.cola.archetype.source.serviceopen.domain.course.vos.CourseId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service("getCourseQueryExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GetCourseQueryExe {

    @Qualifier("courseDomainService")
    private final CourseDomainService courseDomainService;

    @Qualifier("courseApplicationConverterImpl")
    private final CourseApplicationConverter converter;

    @Qualifier("courseApplicationValidator")
    private final CourseApplicationValidator validator;

    public CourseResult execute(GetCourseQuery query) {
        validator.require(query != null, "get course query is required");
        return courseDomainService.findById(new CourseId(query.courseId()))
                .map(converter::toResult)
                .orElseThrow(() -> new ApplicationException(
                        ApplicationErrorCode.COURSE_NOT_FOUND, "course not found"));
    }
}
