package top.egon.cola.archetype.source.lightopen.application.teaching.manage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.egon.cola.archetype.source.lightopen.application.teaching.manage.CourseManage;
import top.egon.cola.archetype.source.lightopen.application.teaching.pojo.command.CreateCourseCommand;
import top.egon.cola.archetype.source.lightopen.application.teaching.pojo.convertor.TeachingApplicationConvertor;
import top.egon.cola.archetype.source.lightopen.application.teaching.pojo.query.GetCourseQuery;
import top.egon.cola.archetype.source.lightopen.application.teaching.pojo.result.CourseResult;
import top.egon.cola.archetype.source.lightopen.application.teaching.validators.TeachingApplicationValidator;
import top.egon.cola.archetype.source.lightopen.domain.teaching.service.CourseDomainService;
import top.egon.cola.archetype.source.lightopen.domain.teaching.service.CourseIdempotencyService;
import top.egon.cola.archetype.source.lightopen.domain.teaching.service.TeachingEventService;
import top.egon.cola.archetype.source.lightopen.domain.teaching.service.TeachingQueryService;
import top.egon.cola.archetype.source.lightopen.application.teaching.executor.command.CreateCourseCmdExe;
import top.egon.cola.archetype.source.lightopen.application.teaching.executor.query.GetCourseQueryExe;

@Service("courseManageImpl")
@Lazy
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CourseManageImpl implements CourseManage {
    @Qualifier("createCourseCmdExe")
    private final CreateCourseCmdExe createCourseCmdExe;

    @Qualifier("getCourseQueryExe")
    private final GetCourseQueryExe getCourseQueryExe;

    public CourseManageImpl(CourseDomainService courseDomainService,
            TeachingQueryService teachingQueryService,
            TeachingEventService teachingEventService,
            CourseIdempotencyService courseIdempotencyService,
            TeachingApplicationValidator applicationValidator,
            TeachingApplicationConvertor convertor) {
        this(new CreateCourseCmdExe(courseDomainService, teachingQueryService, teachingEventService, courseIdempotencyService, applicationValidator, convertor),
                new GetCourseQueryExe(courseDomainService, convertor));
    }

    @Override
    @Transactional
    public CourseResult create(CreateCourseCommand command) {
        return createCourseCmdExe.execute(command);
    }

    @Override
    public CourseResult get(GetCourseQuery query) {
        return getCourseQueryExe.execute(query);
    }

}
