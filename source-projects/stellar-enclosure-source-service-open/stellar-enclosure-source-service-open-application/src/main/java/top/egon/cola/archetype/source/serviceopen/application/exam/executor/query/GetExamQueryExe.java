package top.egon.cola.archetype.source.serviceopen.application.exam.executor.query;

import top.egon.cola.archetype.source.serviceopen.common.enums.ApplicationErrorCode;
import top.egon.cola.archetype.source.serviceopen.common.exception.ApplicationException;
import top.egon.cola.archetype.source.serviceopen.application.exam.pojo.convertor.ExamApplicationConverter;
import top.egon.cola.archetype.source.serviceopen.application.exam.pojo.query.GetExamQuery;
import top.egon.cola.archetype.source.serviceopen.application.exam.pojo.result.ExamDetailResult;
import top.egon.cola.archetype.source.serviceopen.application.exam.validators.ExamApplicationValidator;
import top.egon.cola.archetype.source.serviceopen.domain.exam.entities.Exam;
import top.egon.cola.archetype.source.serviceopen.domain.exam.service.ExamDomainService;
import top.egon.cola.archetype.source.serviceopen.domain.exam.vos.ExamId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service("getExamQueryExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GetExamQueryExe {

    @Qualifier("examDomainService")
    private final ExamDomainService examDomainService;

    @Qualifier("examApplicationConverterImpl")
    private final ExamApplicationConverter converter;

    @Qualifier("examApplicationValidator")
    private final ExamApplicationValidator validator;

    public ExamDetailResult execute(GetExamQuery query) {
        validator.positive(query.examId(), "examId");
        return converter.toResult(requireExam(query.examId()));
    }

    private Exam requireExam(Long examId) {
        return examDomainService.findById(new ExamId(examId))
                .orElseThrow(() -> failure(ApplicationErrorCode.EXAM_NOT_FOUND, "exam not found"));
    }

    private static ApplicationException failure(ApplicationErrorCode code, String message) {
        return new ApplicationException(code, message);
    }
}
