package top.egon.cola.archetype.source.webopen.application.user.executor.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.archetype.source.webopen.application.user.pojo.convertor.PermissionConverter;
import top.egon.cola.archetype.source.webopen.application.user.pojo.query.PermissionTreeQuery;
import top.egon.cola.archetype.source.webopen.application.user.pojo.result.PermissionTreeResult;
import top.egon.cola.archetype.source.webopen.domain.user.service.PermissionDomainService;
import top.egon.cola.archetype.source.webopen.domain.user.vos.UserId;

@Service("permissionTreeQueryExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PermissionTreeQueryExe {

    @Qualifier("permissionDomainService")
    private final PermissionDomainService permissionDomainService;

    @Qualifier("permissionConverterImpl")
    private final PermissionConverter converter;

    public PermissionTreeResult execute(PermissionTreeQuery query) {
        UserId userId = new UserId(query.userId());
        return converter.toResult(userId.value(), permissionDomainService.findByUserId(userId));
    }
}
