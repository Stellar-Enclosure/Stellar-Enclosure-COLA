package top.egon.cola.archetype.source.service.application.exam.executor.command;

import top.egon.cola.archetype.source.service.common.enums.ApplicationErrorCode;
import top.egon.cola.archetype.source.service.common.exception.ApplicationException;
import top.egon.cola.archetype.source.service.application.exam.pojo.command.CreateExamCommand;
import top.egon.cola.archetype.source.service.application.exam.pojo.convertor.ExamApplicationConverter;
import top.egon.cola.archetype.source.service.application.exam.pojo.result.ExamDetailResult;
import top.egon.cola.archetype.source.service.application.exam.validators.ExamApplicationValidator;
import top.egon.cola.archetype.source.service.domain.course.entities.Course;
import top.egon.cola.archetype.source.service.domain.course.service.CourseDomainService;
import top.egon.cola.archetype.source.service.domain.course.vos.CourseId;
import top.egon.cola.archetype.source.service.domain.exam.entities.Exam;
import top.egon.cola.archetype.source.service.domain.exam.service.ExamDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service("createExamCmdExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CreateExamCmdExe {

    @Qualifier("courseDomainService")
    private final CourseDomainService courseDomainService;

    @Qualifier("examDomainService")
    private final ExamDomainService examDomainService;

    @Qualifier("examApplicationConverterImpl")
    private final ExamApplicationConverter converter;

    @Qualifier("examApplicationValidator")
    private final ExamApplicationValidator validator;

    @Transactional
    public ExamDetailResult execute(CreateExamCommand command) {
        validator.positive(command.courseId(), "courseId");
        Course course = courseDomainService.findById(new CourseId(command.courseId()))
                .orElseThrow(() -> failure(ApplicationErrorCode.COURSE_NOT_FOUND, "course not found"));
        Exam exam = examDomainService.createExam(
                course, command.title(), command.startsAt(), command.endsAt());
        return converter.toResult(examDomainService.save(exam));
    }

    private static ApplicationException failure(ApplicationErrorCode code, String message) {
        return new ApplicationException(code, message);
    }
}
