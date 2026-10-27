package top.egon.cola.archetype.source.service.application.course.manage.impl;

import top.egon.cola.archetype.source.service.application.course.pojo.command.CreateCourseCommand;
import top.egon.cola.archetype.source.service.application.course.pojo.command.ScheduleCourseCommand;
import top.egon.cola.archetype.source.service.application.course.pojo.convertor.CourseApplicationConverter;
import top.egon.cola.archetype.source.service.application.course.manage.CourseManage;
import top.egon.cola.archetype.source.service.application.course.pojo.query.GetCourseQuery;
import top.egon.cola.archetype.source.service.application.course.pojo.query.PageCourseQuery;
import top.egon.cola.archetype.source.service.application.course.pojo.result.CourseResult;
import top.egon.cola.archetype.source.service.application.course.pojo.result.CourseScheduleResult;
import top.egon.cola.archetype.source.service.application.course.validators.CourseApplicationValidator;
import top.egon.cola.archetype.source.service.application.pojo.result.PageResult;
import top.egon.cola.archetype.source.service.domain.course.service.CourseEventService;
import top.egon.cola.archetype.source.service.domain.course.service.CourseDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.archetype.source.service.application.course.executor.command.CreateCourseCmdExe;
import top.egon.cola.archetype.source.service.application.course.executor.command.ScheduleCourseCmdExe;
import top.egon.cola.archetype.source.service.application.course.executor.query.GetCourseQueryExe;
import top.egon.cola.archetype.source.service.application.course.executor.query.PageCourseQueryExe;

@Service("courseManage")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CourseManageImpl implements CourseManage {

    @Qualifier("createCourseCmdExe")
    private final CreateCourseCmdExe createCourseCmdExe;

    @Qualifier("scheduleCourseCmdExe")
    private final ScheduleCourseCmdExe scheduleCourseCmdExe;

    @Qualifier("getCourseQueryExe")
    private final GetCourseQueryExe getCourseQueryExe;

    @Qualifier("pageCourseQueryExe")
    private final PageCourseQueryExe pageCourseQueryExe;

    public CourseManageImpl(CourseEventService courseEventService,
            CourseDomainService courseDomainService,
            CourseApplicationConverter converter,
            CourseApplicationValidator validator) {
        this(new CreateCourseCmdExe(courseDomainService, converter, validator),
                new ScheduleCourseCmdExe(courseEventService, courseDomainService, converter, validator),
                new GetCourseQueryExe(courseDomainService, converter, validator),
                new PageCourseQueryExe(courseDomainService, converter, validator));
    }

    @Override
    @Transactional
    public CourseResult create(CreateCourseCommand command) {
        return createCourseCmdExe.execute(command);
    }

    @Override
    @Transactional
    public CourseScheduleResult schedule(ScheduleCourseCommand command) {
        return scheduleCourseCmdExe.execute(command);
    }

    @Override
    public CourseResult get(GetCourseQuery query) {
        return getCourseQueryExe.execute(query);
    }

    @Override
    public PageResult<CourseResult> page(PageCourseQuery query) {
        return pageCourseQueryExe.execute(query);
    }
}
