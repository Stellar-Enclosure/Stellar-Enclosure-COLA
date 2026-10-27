package top.egon.cola.archetype.source.service.application.exam.executor.query;

import top.egon.cola.archetype.source.service.application.exam.pojo.convertor.ExamApplicationConverter;
import top.egon.cola.archetype.source.service.application.exam.pojo.query.PageScoreQuery;
import top.egon.cola.archetype.source.service.application.exam.pojo.result.ScoreResult;
import top.egon.cola.archetype.source.service.application.exam.validators.ExamApplicationValidator;
import top.egon.cola.archetype.source.service.application.pojo.result.PageResult;
import top.egon.cola.archetype.source.service.domain.common.Page;
import top.egon.cola.archetype.source.service.domain.exam.entities.Score;
import top.egon.cola.archetype.source.service.domain.exam.service.ScoreDomainService;
import top.egon.cola.archetype.source.service.domain.exam.vos.ExamId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service("pageScoreQueryExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PageScoreQueryExe {

    @Qualifier("scoreDomainService")
    private final ScoreDomainService scoreDomainService;

    @Qualifier("examApplicationConverterImpl")
    private final ExamApplicationConverter converter;

    @Qualifier("examApplicationValidator")
    private final ExamApplicationValidator validator;

    public PageResult<ScoreResult> execute(PageScoreQuery query) {
        validator.positive(query.examId(), "examId");
        Page<Score> page = scoreDomainService.findPageByExamId(
                new ExamId(query.examId()), query.currentPage(), query.pageSize());
        return PageResult.of(
                page.records().stream().map(converter::toResult).toList(),
                page.currentPage(), page.totalPages(), page.pageSize(), page.totalCount());
    }
}
