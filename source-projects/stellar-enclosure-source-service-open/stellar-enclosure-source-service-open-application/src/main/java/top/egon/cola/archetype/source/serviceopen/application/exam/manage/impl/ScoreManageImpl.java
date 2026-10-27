package top.egon.cola.archetype.source.serviceopen.application.exam.manage.impl;

import top.egon.cola.archetype.source.serviceopen.application.exam.pojo.command.RecordScoreCommand;
import top.egon.cola.archetype.source.serviceopen.application.exam.pojo.convertor.ExamApplicationConverter;
import top.egon.cola.archetype.source.serviceopen.application.exam.manage.ScoreManage;
import top.egon.cola.archetype.source.serviceopen.application.exam.pojo.query.GetScoreQuery;
import top.egon.cola.archetype.source.serviceopen.application.exam.pojo.query.PageScoreQuery;
import top.egon.cola.archetype.source.serviceopen.application.exam.pojo.result.ScoreResult;
import top.egon.cola.archetype.source.serviceopen.application.exam.validators.ExamApplicationValidator;
import top.egon.cola.archetype.source.serviceopen.application.pojo.result.PageResult;
import top.egon.cola.archetype.source.serviceopen.domain.exam.service.ExamEventService;
import top.egon.cola.archetype.source.serviceopen.domain.exam.service.ExamDomainService;
import top.egon.cola.archetype.source.serviceopen.domain.exam.service.ScoreDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.archetype.source.serviceopen.application.exam.executor.command.RecordScoreCmdExe;
import top.egon.cola.archetype.source.serviceopen.application.exam.executor.query.GetScoreQueryExe;
import top.egon.cola.archetype.source.serviceopen.application.exam.executor.query.PageScoreQueryExe;

@Service("scoreManage")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ScoreManageImpl implements ScoreManage {

    @Qualifier("recordScoreCmdExe")
    private final RecordScoreCmdExe recordScoreCmdExe;

    @Qualifier("getScoreQueryExe")
    private final GetScoreQueryExe getScoreQueryExe;

    @Qualifier("pageScoreQueryExe")
    private final PageScoreQueryExe pageScoreQueryExe;

    public ScoreManageImpl(ExamDomainService examDomainService,
            ScoreDomainService scoreDomainService,
            ExamEventService examEventService,
            ExamApplicationConverter converter,
            ExamApplicationValidator validator) {
        this(new RecordScoreCmdExe(examDomainService, scoreDomainService, examEventService, converter, validator),
                new GetScoreQueryExe(scoreDomainService, converter, validator),
                new PageScoreQueryExe(scoreDomainService, converter, validator));
    }

    @Override
    @Transactional
    public ScoreResult record(RecordScoreCommand command) {
        return recordScoreCmdExe.execute(command);
    }

    @Override
    public ScoreResult get(GetScoreQuery query) {
        return getScoreQueryExe.execute(query);
    }

    @Override
    public PageResult<ScoreResult> page(PageScoreQuery query) {
        return pageScoreQueryExe.execute(query);
    }

}
