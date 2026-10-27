package top.egon.cola.archetype.source.webopen.application.teaching.manage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.archetype.source.webopen.application.teaching.manage.GradeManage;
import top.egon.cola.archetype.source.webopen.application.teaching.pojo.command.CreateGradeCommand;
import top.egon.cola.archetype.source.webopen.application.teaching.pojo.convertor.GradeConverter;
import top.egon.cola.archetype.source.webopen.application.teaching.pojo.query.GradeDetailQuery;
import top.egon.cola.archetype.source.webopen.application.teaching.pojo.result.GradeDetailResult;
import top.egon.cola.archetype.source.webopen.application.teaching.validators.TeachingApplicationValidator;
import top.egon.cola.archetype.source.webopen.domain.service.CommandIdempotencyService;
import top.egon.cola.archetype.source.webopen.domain.service.OrganizationEventService;
import top.egon.cola.archetype.source.webopen.domain.teaching.entities.Grade;
import top.egon.cola.archetype.source.webopen.domain.teaching.service.GradeDomainService;
import top.egon.cola.archetype.source.webopen.application.teaching.executor.command.CreateGradeCmdExe;
import top.egon.cola.archetype.source.webopen.application.teaching.executor.query.GradeDetailQueryExe;

/** Grade use cases; caching and transport stay inside the domain service implementations. */
@Service("gradeManage")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GradeManageImpl implements GradeManage {

    @Qualifier("createGradeCmdExe")
    private final CreateGradeCmdExe createGradeCmdExe;

    @Qualifier("gradeDetailQueryExe")
    private final GradeDetailQueryExe gradeDetailQueryExe;

    public GradeManageImpl(GradeDomainService gradeDomainService,
            TeachingApplicationValidator validator,
            CommandIdempotencyService idempotency,
            OrganizationEventService eventService,
            GradeConverter converter) {
        this(new CreateGradeCmdExe(gradeDomainService, validator, idempotency, eventService, converter),
                new GradeDetailQueryExe(gradeDomainService, converter));
    }

    @Override
    @Transactional
    public GradeDetailResult createGrade(CreateGradeCommand command) {
        return createGradeCmdExe.execute(command);
    }

    @Override
    public GradeDetailResult getGrade(GradeDetailQuery query) {
        return gradeDetailQueryExe.execute(query);
    }

}
