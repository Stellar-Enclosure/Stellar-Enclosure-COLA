package top.egon.cola.archetype.source.serviceopen.application.exam.executor.command;

import top.egon.cola.archetype.source.serviceopen.common.enums.ApplicationErrorCode;
import top.egon.cola.archetype.source.serviceopen.common.exception.ApplicationException;
import top.egon.cola.archetype.source.serviceopen.application.exam.pojo.command.AttachExamPaperCommand;
import top.egon.cola.archetype.source.serviceopen.application.exam.pojo.convertor.ExamApplicationConverter;
import top.egon.cola.archetype.source.serviceopen.application.exam.pojo.result.ExamPaperResult;
import top.egon.cola.archetype.source.serviceopen.application.exam.validators.ExamApplicationValidator;
import top.egon.cola.archetype.source.serviceopen.domain.exam.entities.Exam;
import top.egon.cola.archetype.source.serviceopen.domain.exam.entities.ExamPaper;
import top.egon.cola.archetype.source.serviceopen.domain.exam.service.ExamDomainService;
import top.egon.cola.archetype.source.serviceopen.domain.exam.vos.ExamId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service("attachExamPaperCmdExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AttachExamPaperCmdExe {

    @Qualifier("examDomainService")
    private final ExamDomainService examDomainService;

    @Qualifier("examApplicationConverterImpl")
    private final ExamApplicationConverter converter;

    @Qualifier("examApplicationValidator")
    private final ExamApplicationValidator validator;

    @Transactional
    public ExamPaperResult execute(AttachExamPaperCommand command) {
        validator.positive(command.examId(), "examId");
        Exam exam = requireExam(command.examId());
        ExamPaper paper = examDomainService.attachPaper(exam, command.title(), command.totalPoints());
        return converter.toResult(examDomainService.savePaper(paper));
    }

    private Exam requireExam(Long examId) {
        return examDomainService.findById(new ExamId(examId))
                .orElseThrow(() -> failure(ApplicationErrorCode.EXAM_NOT_FOUND, "exam not found"));
    }

    private static ApplicationException failure(ApplicationErrorCode code, String message) {
        return new ApplicationException(code, message);
    }
}
