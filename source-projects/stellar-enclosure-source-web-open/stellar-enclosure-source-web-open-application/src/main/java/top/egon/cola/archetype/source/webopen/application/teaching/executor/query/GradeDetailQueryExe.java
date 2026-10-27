package top.egon.cola.archetype.source.webopen.application.teaching.executor.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.archetype.source.webopen.application.teaching.pojo.convertor.GradeConverter;
import top.egon.cola.archetype.source.webopen.application.teaching.pojo.query.GradeDetailQuery;
import top.egon.cola.archetype.source.webopen.application.teaching.pojo.result.GradeDetailResult;
import top.egon.cola.archetype.source.webopen.common.enums.OrganizationFailureType;
import top.egon.cola.archetype.source.webopen.common.exception.OrganizationApplicationException;
import top.egon.cola.archetype.source.webopen.domain.teaching.entities.Grade;
import top.egon.cola.archetype.source.webopen.domain.teaching.service.GradeDomainService;

@Service("gradeDetailQueryExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GradeDetailQueryExe {

    @Qualifier("gradeDomainService")
    private final GradeDomainService gradeDomainService;

    @Qualifier("gradeConverterImpl")
    private final GradeConverter converter;

    public GradeDetailResult execute(GradeDetailQuery query) {
        Grade grade = gradeDomainService.findById(query.gradeId())
                .orElseThrow(() -> notFound("grade not found"));
        return converter.toResult(grade);
    }

    private static OrganizationApplicationException notFound(String message) {
        return new OrganizationApplicationException(
                OrganizationFailureType.NOT_FOUND, "ORG_NOT_FOUND", message);
    }
}
