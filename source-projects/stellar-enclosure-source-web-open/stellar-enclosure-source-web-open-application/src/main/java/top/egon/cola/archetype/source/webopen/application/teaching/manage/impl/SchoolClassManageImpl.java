package top.egon.cola.archetype.source.webopen.application.teaching.manage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.archetype.source.webopen.application.teaching.manage.SchoolClassManage;
import top.egon.cola.archetype.source.webopen.application.teaching.pojo.command.AssignUserToClassCommand;
import top.egon.cola.archetype.source.webopen.application.teaching.pojo.command.CreateSchoolClassCommand;
import top.egon.cola.archetype.source.webopen.application.teaching.pojo.convertor.SchoolClassConverter;
import top.egon.cola.archetype.source.webopen.application.teaching.pojo.query.SchoolClassDetailQuery;
import top.egon.cola.archetype.source.webopen.application.teaching.pojo.result.SchoolClassDetailResult;
import top.egon.cola.archetype.source.webopen.application.teaching.validators.TeachingApplicationValidator;
import top.egon.cola.archetype.source.webopen.domain.service.CommandIdempotencyService;
import top.egon.cola.archetype.source.webopen.domain.service.OrganizationEventService;
import top.egon.cola.archetype.source.webopen.domain.teaching.service.SchoolClassDomainService;
import top.egon.cola.archetype.source.webopen.domain.user.service.UserDomainService;
import top.egon.cola.archetype.source.webopen.application.teaching.executor.command.CreateSchoolClassCmdExe;
import top.egon.cola.archetype.source.webopen.application.teaching.executor.query.SchoolClassDetailQueryExe;
import top.egon.cola.archetype.source.webopen.application.teaching.executor.command.AssignUserToClassCmdExe;

/** School class and membership use cases; the compound lookup stays inside the domain service. */
@Service("schoolClassManage")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SchoolClassManageImpl implements SchoolClassManage {

    @Qualifier("createSchoolClassCmdExe")
    private final CreateSchoolClassCmdExe createSchoolClassCmdExe;

    @Qualifier("schoolClassDetailQueryExe")
    private final SchoolClassDetailQueryExe schoolClassDetailQueryExe;

    @Qualifier("assignUserToClassCmdExe")
    private final AssignUserToClassCmdExe assignUserToClassCmdExe;

    public SchoolClassManageImpl(SchoolClassDomainService schoolClassDomainService,
            UserDomainService userDomainService,
            TeachingApplicationValidator validator,
            CommandIdempotencyService idempotency,
            OrganizationEventService eventService,
            SchoolClassConverter converter) {
        this(new CreateSchoolClassCmdExe(schoolClassDomainService, validator, idempotency, eventService, converter),
                new SchoolClassDetailQueryExe(schoolClassDomainService, converter),
                new AssignUserToClassCmdExe(schoolClassDomainService, userDomainService, validator, idempotency, eventService));
    }

    @Override
    @Transactional
    public SchoolClassDetailResult createSchoolClass(CreateSchoolClassCommand command) {
        return createSchoolClassCmdExe.execute(command);
    }

    @Override
    public SchoolClassDetailResult getSchoolClass(SchoolClassDetailQuery query) {
        return schoolClassDetailQueryExe.execute(query);
    }

    @Override
    @Transactional
    public void assignUser(AssignUserToClassCommand command) {
        assignUserToClassCmdExe.execute(command);
    }

}
