package top.egon.cola.archetype.source.service.application.exam.executor.command;

import top.egon.cola.archetype.source.service.common.enums.ApplicationErrorCode;
import top.egon.cola.archetype.source.service.common.exception.ApplicationException;
import top.egon.cola.archetype.source.service.application.exam.pojo.command.RecordScoreCommand;
import top.egon.cola.archetype.source.service.application.exam.pojo.convertor.ExamApplicationConverter;
import top.egon.cola.archetype.source.service.application.exam.pojo.result.ScoreResult;
import top.egon.cola.archetype.source.service.application.exam.validators.ExamApplicationValidator;
import top.egon.cola.archetype.source.service.domain.exam.entities.Exam;
import top.egon.cola.archetype.source.service.domain.exam.entities.ExamPaper;
import top.egon.cola.archetype.source.service.domain.exam.entities.Score;
import top.egon.cola.archetype.source.service.domain.exam.service.ExamEventService;
import top.egon.cola.archetype.source.service.domain.exam.service.ExamDomainService;
import top.egon.cola.archetype.source.service.domain.exam.service.ScoreDomainService;
import top.egon.cola.archetype.source.service.domain.exam.vos.ExamId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service("recordScoreCmdExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RecordScoreCmdExe {

    @Qualifier("examDomainService")
    private final ExamDomainService examDomainService;

    @Qualifier("scoreDomainService")
    private final ScoreDomainService scoreDomainService;

    @Qualifier("examEventService")
    private final ExamEventService examEventService;

    @Qualifier("examApplicationConverterImpl")
    private final ExamApplicationConverter converter;

    @Qualifier("examApplicationValidator")
    private final ExamApplicationValidator validator;

    @Transactional
    public ScoreResult execute(RecordScoreCommand command) {
        validator.positive(command.examId(), "examId");
        validator.positive(command.studentId(), "studentId");
        ExamId examId = new ExamId(command.examId());
        Exam exam = examDomainService.findById(examId)
                .orElseThrow(() -> failure(ApplicationErrorCode.EXAM_NOT_FOUND, "exam not found"));
        ExamPaper paper = examDomainService.findPaperByExamId(examId)
                .orElseThrow(() -> failure(
                        ApplicationErrorCode.EXAM_PAPER_NOT_FOUND, "exam paper not found"));
        boolean duplicate = scoreDomainService.existsByExamIdAndStudentId(
                examId, command.studentId());
        Score score = scoreDomainService.recordScore(
                exam, paper, command.studentId(), command.points(), duplicate);
        Score saved = scoreDomainService.save(score);
        examEventService.scoreRecorded(saved);
        return converter.toResult(saved);
    }

    private static ApplicationException failure(ApplicationErrorCode code, String message) {
        return new ApplicationException(code, message);
    }
}
