package top.egon.cola.archetype.source.serviceopen.application.course.executor.query;

import top.egon.cola.archetype.source.serviceopen.application.course.pojo.convertor.CourseApplicationConverter;
import top.egon.cola.archetype.source.serviceopen.application.course.pojo.query.PageCourseQuery;
import top.egon.cola.archetype.source.serviceopen.application.course.pojo.result.CourseResult;
import top.egon.cola.archetype.source.serviceopen.application.course.validators.CourseApplicationValidator;
import top.egon.cola.archetype.source.serviceopen.application.pojo.result.PageResult;
import top.egon.cola.archetype.source.serviceopen.domain.common.Page;
import top.egon.cola.archetype.source.serviceopen.domain.course.entities.Course;
import top.egon.cola.archetype.source.serviceopen.domain.course.service.CourseDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service("pageCourseQueryExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PageCourseQueryExe {

    @Qualifier("courseDomainService")
    private final CourseDomainService courseDomainService;

    @Qualifier("courseApplicationConverterImpl")
    private final CourseApplicationConverter converter;

    @Qualifier("courseApplicationValidator")
    private final CourseApplicationValidator validator;

    public PageResult<CourseResult> execute(PageCourseQuery query) {
        validator.require(
                query != null && query.currentPage() > 0 && query.pageSize() > 0,
                "positive page parameters are required");
        Page<Course> page = courseDomainService.findPage(query.currentPage(), query.pageSize());
        return PageResult.of(
                page.records().stream().map(converter::toResult).toList(),
                page.currentPage(), page.totalPages(), page.pageSize(), page.totalCount());
    }
}
