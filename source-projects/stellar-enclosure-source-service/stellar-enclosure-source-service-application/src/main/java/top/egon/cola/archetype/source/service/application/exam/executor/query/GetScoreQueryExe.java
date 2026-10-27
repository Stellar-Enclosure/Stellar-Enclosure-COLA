package top.egon.cola.archetype.source.service.application.exam.executor.query;

import top.egon.cola.archetype.source.service.common.enums.ApplicationErrorCode;
import top.egon.cola.archetype.source.service.common.exception.ApplicationException;
import top.egon.cola.archetype.source.service.application.exam.pojo.convertor.ExamApplicationConverter;
import top.egon.cola.archetype.source.service.application.exam.pojo.query.GetScoreQuery;
import top.egon.cola.archetype.source.service.application.exam.pojo.result.ScoreResult;
import top.egon.cola.archetype.source.service.application.exam.validators.ExamApplicationValidator;
import top.egon.cola.archetype.source.service.domain.exam.service.ScoreDomainService;
import top.egon.cola.archetype.source.service.domain.exam.vos.ExamId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service("getScoreQueryExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GetScoreQueryExe {

    @Qualifier("scoreDomainService")
    private final ScoreDomainService scoreDomainService;

    @Qualifier("examApplicationConverterImpl")
    private final ExamApplicationConverter converter;

    @Qualifier("examApplicationValidator")
    private final ExamApplicationValidator validator;

    public ScoreResult execute(GetScoreQuery query) {
        validator.positive(query.examId(), "examId");
        validator.positive(query.scoreId(), "scoreId");
        return scoreDomainService.findByExamIdAndId(
                        new ExamId(query.examId()), query.scoreId())
                .map(converter::toResult)
                .orElseThrow(() -> failure(ApplicationErrorCode.SCORE_NOT_FOUND, "score not found"));
    }

    private static ApplicationException failure(ApplicationErrorCode code, String message) {
        return new ApplicationException(code, message);
    }
}
