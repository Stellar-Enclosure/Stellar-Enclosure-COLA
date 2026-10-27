package top.egon.cola.archetype.source.light.application.teaching.executor.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.light.application.teaching.pojo.convertor.TeachingApplicationConvertor;
import top.egon.cola.archetype.source.light.application.teaching.pojo.query.GetCourseQuery;
import top.egon.cola.archetype.source.light.application.teaching.pojo.result.CourseResult;
import top.egon.cola.archetype.source.light.common.exception.TeachingUseCaseException;
import top.egon.cola.archetype.source.light.domain.teaching.service.CourseDomainService;

@Service("getCourseQueryExe")
@Lazy
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GetCourseQueryExe {

    @Qualifier("courseDomainService")
    private final CourseDomainService courseDomainService;

    @Qualifier("teachingApplicationConvertorImpl")
    private final TeachingApplicationConvertor convertor;

    public CourseResult execute(GetCourseQuery query) {
        return courseDomainService.findById(query.courseId())
                .map(convertor::toTarget)
                .orElseThrow(() -> new TeachingUseCaseException("COURSE_NOT_FOUND", "course not found"));
    }
}
