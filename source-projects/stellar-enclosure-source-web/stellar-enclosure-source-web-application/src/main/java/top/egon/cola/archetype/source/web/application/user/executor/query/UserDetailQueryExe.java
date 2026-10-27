package top.egon.cola.archetype.source.web.application.user.executor.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.archetype.source.web.application.user.pojo.convertor.UserConverter;
import top.egon.cola.archetype.source.web.application.user.pojo.query.UserDetailQuery;
import top.egon.cola.archetype.source.web.application.user.pojo.result.UserDetailResult;
import top.egon.cola.archetype.source.web.common.enums.OrganizationFailureType;
import top.egon.cola.archetype.source.web.common.exception.OrganizationApplicationException;
import top.egon.cola.archetype.source.web.domain.user.entities.User;
import top.egon.cola.archetype.source.web.domain.user.service.UserDomainService;
import top.egon.cola.archetype.source.web.domain.user.vos.UserId;

@Service("userDetailQueryExe")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDetailQueryExe {

    @Qualifier("userDomainService")
    private final UserDomainService userDomainService;

    @Qualifier("userConverterImpl")
    private final UserConverter converter;

    public UserDetailResult execute(UserDetailQuery query) {
        User user = userDomainService.findById(new UserId(query.userId()))
                .orElseThrow(() -> notFound("user not found"));
        return converter.toResult(user);
    }

    private static OrganizationApplicationException notFound(String message) {
        return new OrganizationApplicationException(
                OrganizationFailureType.NOT_FOUND, "ORG_NOT_FOUND", message);
    }
}
