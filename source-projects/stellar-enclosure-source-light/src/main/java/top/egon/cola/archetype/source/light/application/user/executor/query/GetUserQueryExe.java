package top.egon.cola.archetype.source.light.application.user.executor.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.light.application.user.pojo.convertor.UserApplicationConvertor;
import top.egon.cola.archetype.source.light.application.user.pojo.query.GetUserQuery;
import top.egon.cola.archetype.source.light.application.user.pojo.result.UserResult;
import top.egon.cola.archetype.source.light.common.exception.UserUseCaseException;
import top.egon.cola.archetype.source.light.domain.user.service.UserDomainService;
import top.egon.cola.archetype.source.light.domain.user.vos.UserId;

@Service("getUserQueryExe")
@Lazy
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GetUserQueryExe {

    @Qualifier("userDomainService")
    private final UserDomainService userDomainService;

    @Qualifier("userApplicationConvertorImpl")
    private final UserApplicationConvertor convertor;

    public UserResult execute(GetUserQuery query) {
        return userDomainService.findById(new UserId(query.userId()))
                .map(convertor::toTarget)
                .orElseThrow(() -> new UserUseCaseException("USER_NOT_FOUND", "user not found"));
    }
}
