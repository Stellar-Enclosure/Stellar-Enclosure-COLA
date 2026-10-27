package top.egon.cola.archetype.source.lightopen.application.teaching.manage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.egon.cola.archetype.source.lightopen.application.teaching.manage.SchoolClassManage;
import top.egon.cola.archetype.source.lightopen.application.teaching.pojo.command.CreateSchoolClassCommand;
import top.egon.cola.archetype.source.lightopen.application.teaching.pojo.command.ScheduleCourseCommand;
import top.egon.cola.archetype.source.lightopen.application.teaching.pojo.convertor.TeachingApplicationConvertor;
import top.egon.cola.archetype.source.lightopen.application.teaching.pojo.query.GetSchoolClassQuery;
import top.egon.cola.archetype.source.lightopen.application.teaching.pojo.result.SchoolClassResult;
import top.egon.cola.archetype.source.lightopen.application.teaching.validators.TeachingApplicationValidator;
import top.egon.cola.archetype.source.lightopen.domain.teaching.service.CourseDomainService;
import top.egon.cola.archetype.source.lightopen.domain.teaching.service.CourseIdempotencyService;
import top.egon.cola.archetype.source.lightopen.domain.teaching.service.SchoolClassDomainService;
import top.egon.cola.archetype.source.lightopen.domain.teaching.service.TeachingEventService;
import top.egon.cola.archetype.source.lightopen.application.teaching.executor.command.CreateSchoolClassCmdExe;
import top.egon.cola.archetype.source.lightopen.application.teaching.executor.command.ScheduleCourseCmdExe;
import top.egon.cola.archetype.source.lightopen.application.teaching.executor.query.GetSchoolClassQueryExe;

@Service("schoolClassManageImpl")
@Lazy
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SchoolClassManageImpl implements SchoolClassManage {
    @Qualifier("createSchoolClassCmdExe")
    private final CreateSchoolClassCmdExe createSchoolClassCmdExe;

    @Qualifier("scheduleCourseCmdExe")
    private final ScheduleCourseCmdExe scheduleCourseCmdExe;

    @Qualifier("getSchoolClassQueryExe")
    private final GetSchoolClassQueryExe getSchoolClassQueryExe;

    public SchoolClassManageImpl(SchoolClassDomainService schoolClassDomainService,
            CourseDomainService courseDomainService,
            TeachingEventService teachingEventService,
            CourseIdempotencyService courseIdempotencyService,
            TeachingApplicationValidator applicationValidator,
            TeachingApplicationConvertor convertor) {
        this(new CreateSchoolClassCmdExe(schoolClassDomainService, teachingEventService, courseIdempotencyService, applicationValidator, convertor),
                new ScheduleCourseCmdExe(schoolClassDomainService, courseDomainService, teachingEventService, courseIdempotencyService, applicationValidator, convertor),
                new GetSchoolClassQueryExe(schoolClassDomainService, convertor));
    }

    @Override
    @Transactional
    public SchoolClassResult create(CreateSchoolClassCommand command) {
        return createSchoolClassCmdExe.execute(command);
    }

    @Override
    @Transactional
    public SchoolClassResult schedule(ScheduleCourseCommand command) {
        return scheduleCourseCmdExe.execute(command);
    }

    @Override
    public SchoolClassResult get(GetSchoolClassQuery query) {
        return getSchoolClassQueryExe.execute(query);
    }

}
