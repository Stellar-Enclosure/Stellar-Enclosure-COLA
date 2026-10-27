package top.egon.cola.archetype.source.service.application.exam.executor.command;

import top.egon.cola.archetype.source.service.common.enums.ApplicationErrorCode;
import top.egon.cola.archetype.source.service.common.exception.ApplicationException;
import top.egon.cola.archetype.source.service.application.exam.pojo.command.PublishExamCommand;
import top.egon.cola.archetype.source.service.application.exam.pojo.convertor.ExamApplicationConverter;
import top.egon.cola.archetype.source.service.application.exam.pojo.result.ExamDetailResult;
import top.egon.cola.archetype.source.service.application.exam.validators.ExamApplicationValidator;
import top.egon.cola.archetype.source.service.domain.exam.entities.Exam;
import top.egon.cola.archetype.source.service.domain.exam.entities.ExamPaper;
import top.egon.cola.archetype.source.service.domain.exam.service.ExamEventService;
import top.egon.cola.archetype.source.service.domain.exam.service.ExamDomainService;
import top.egon.cola.archetype.source.service.domain.exam.vos.ExamId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service("publishExamCmdExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PublishExamCmdExe {

    @Qualifier("examDomainService")
    private final ExamDomainService examDomainService;

    @Qualifier("examEventService")
    private final ExamEventService examEventService;

    @Qualifier("examApplicationConverterImpl")
    private final ExamApplicationConverter converter;

    @Qualifier("examApplicationValidator")
    private final ExamApplicationValidator validator;

    @Transactional
    public ExamDetailResult execute(PublishExamCommand command) {
        validator.positive(command.examId(), "examId");
        Exam exam = requireExam(command.examId());
        ExamPaper paper = examDomainService.findPaperByExamId(exam.getId())
                .orElseThrow(() -> failure(
                        ApplicationErrorCode.EXAM_PAPER_NOT_FOUND, "exam paper not found"));
        examDomainService.publishExam(exam, paper);
        Exam saved = examDomainService.save(exam);
        examDomainService.savePaper(paper);
        examEventService.examPublished(saved, paper);
        return converter.toResult(saved);
    }

    private Exam requireExam(Long examId) {
        return examDomainService.findById(new ExamId(examId))
                .orElseThrow(() -> failure(ApplicationErrorCode.EXAM_NOT_FOUND, "exam not found"));
    }

    private static ApplicationException failure(ApplicationErrorCode code, String message) {
        return new ApplicationException(code, message);
    }
}
