package top.egon.cola.archetype.source.lightopen.application.teaching.executor.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.lightopen.application.teaching.pojo.convertor.TeachingApplicationConvertor;
import top.egon.cola.archetype.source.lightopen.application.teaching.pojo.query.GetSchoolClassQuery;
import top.egon.cola.archetype.source.lightopen.application.teaching.pojo.result.SchoolClassResult;
import top.egon.cola.archetype.source.lightopen.common.exception.TeachingUseCaseException;
import top.egon.cola.archetype.source.lightopen.domain.teaching.aggregates.SchoolClassAggregate;
import top.egon.cola.archetype.source.lightopen.domain.teaching.service.SchoolClassDomainService;
import top.egon.cola.archetype.source.lightopen.domain.teaching.vos.SchoolClassId;

@Service("getSchoolClassQueryExe")
@Lazy
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GetSchoolClassQueryExe {

    @Qualifier("schoolClassDomainService")
    private final SchoolClassDomainService schoolClassDomainService;

    @Qualifier("teachingApplicationConvertorImpl")
    private final TeachingApplicationConvertor convertor;

    public SchoolClassResult execute(GetSchoolClassQuery query) {
        SchoolClassAggregate aggregate = schoolClassDomainService
                .findAggregateById(new SchoolClassId(query.schoolClassId()))
                .orElseThrow(() -> new TeachingUseCaseException("CLASS_NOT_FOUND", "class not found"));
        return convertor.toSchoolClassResult(aggregate);
    }
}
