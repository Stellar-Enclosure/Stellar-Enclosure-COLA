package top.egon.cola.archetype.source.service.application.exam.manage.impl;

import top.egon.cola.archetype.source.service.application.exam.pojo.command.AttachExamPaperCommand;
import top.egon.cola.archetype.source.service.application.exam.pojo.command.CreateExamCommand;
import top.egon.cola.archetype.source.service.application.exam.pojo.command.PublishExamCommand;
import top.egon.cola.archetype.source.service.application.exam.pojo.convertor.ExamApplicationConverter;
import top.egon.cola.archetype.source.service.application.exam.manage.ExamManage;
import top.egon.cola.archetype.source.service.application.exam.pojo.query.GetExamQuery;
import top.egon.cola.archetype.source.service.application.exam.pojo.result.ExamDetailResult;
import top.egon.cola.archetype.source.service.application.exam.pojo.result.ExamPaperResult;
import top.egon.cola.archetype.source.service.application.exam.validators.ExamApplicationValidator;
import top.egon.cola.archetype.source.service.domain.course.service.CourseDomainService;
import top.egon.cola.archetype.source.service.domain.exam.service.ExamEventService;
import top.egon.cola.archetype.source.service.domain.exam.service.ExamDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.archetype.source.service.application.exam.executor.command.CreateExamCmdExe;
import top.egon.cola.archetype.source.service.application.exam.executor.command.AttachExamPaperCmdExe;
import top.egon.cola.archetype.source.service.application.exam.executor.command.PublishExamCmdExe;
import top.egon.cola.archetype.source.service.application.exam.executor.query.GetExamQueryExe;

@Service("evaluationExamManage")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ExamManageImpl implements ExamManage {

    @Qualifier("createExamCmdExe")
    private final CreateExamCmdExe createExamCmdExe;

    @Qualifier("attachExamPaperCmdExe")
    private final AttachExamPaperCmdExe attachExamPaperCmdExe;

    @Qualifier("publishExamCmdExe")
    private final PublishExamCmdExe publishExamCmdExe;

    @Qualifier("getExamQueryExe")
    private final GetExamQueryExe getExamQueryExe;

    public ExamManageImpl(CourseDomainService courseDomainService,
            ExamDomainService examDomainService,
            ExamEventService examEventService,
            ExamApplicationConverter converter,
            ExamApplicationValidator validator) {
        this(new CreateExamCmdExe(courseDomainService, examDomainService, converter, validator),
                new AttachExamPaperCmdExe(examDomainService, converter, validator),
                new PublishExamCmdExe(examDomainService, examEventService, converter, validator),
                new GetExamQueryExe(examDomainService, converter, validator));
    }

    @Override
    @Transactional
    public ExamDetailResult create(CreateExamCommand command) {
        return createExamCmdExe.execute(command);
    }

    @Override
    @Transactional
    public ExamPaperResult attachPaper(AttachExamPaperCommand command) {
        return attachExamPaperCmdExe.execute(command);
    }

    @Override
    @Transactional
    public ExamDetailResult publish(PublishExamCommand command) {
        return publishExamCmdExe.execute(command);
    }

    @Override
    public ExamDetailResult get(GetExamQuery query) {
        return getExamQueryExe.execute(query);
    }

}
