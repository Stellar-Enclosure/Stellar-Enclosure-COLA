package top.egon.cola.archetype.source.lightopen.application.user.manage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.egon.cola.archetype.source.lightopen.application.user.manage.UserManage;
import top.egon.cola.archetype.source.lightopen.application.user.pojo.command.CreateUserCommand;
import top.egon.cola.archetype.source.lightopen.application.user.pojo.convertor.UserApplicationConvertor;
import top.egon.cola.archetype.source.lightopen.application.user.pojo.query.GetUserQuery;
import top.egon.cola.archetype.source.lightopen.application.user.pojo.result.UserResult;
import top.egon.cola.archetype.source.lightopen.application.user.validators.UserApplicationValidator;
import top.egon.cola.archetype.source.lightopen.domain.user.service.UserDomainService;
import top.egon.cola.archetype.source.lightopen.domain.user.service.UserEventService;
import top.egon.cola.archetype.source.lightopen.domain.user.service.UserIdempotencyService;
import top.egon.cola.archetype.source.lightopen.domain.user.service.UserQueryService;
import top.egon.cola.archetype.source.lightopen.application.user.executor.command.CreateUserCmdExe;
import top.egon.cola.archetype.source.lightopen.application.user.executor.query.GetUserQueryExe;

@Service("userManageImpl")
@Lazy
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserManageImpl implements UserManage {
    @Qualifier("createUserCmdExe")
    private final CreateUserCmdExe createUserCmdExe;

    @Qualifier("getUserQueryExe")
    private final GetUserQueryExe getUserQueryExe;

    public UserManageImpl(UserDomainService userDomainService,
            UserQueryService userQueryService,
            UserEventService userEventService,
            UserIdempotencyService userIdempotencyService,
            UserApplicationValidator applicationValidator,
            UserApplicationConvertor convertor) {
        this(new CreateUserCmdExe(userDomainService, userQueryService, userEventService, userIdempotencyService, applicationValidator, convertor),
                new GetUserQueryExe(userDomainService, convertor));
    }

    @Override
    @Transactional
    public UserResult create(CreateUserCommand command) {
        return createUserCmdExe.execute(command);
    }

    @Override
    public UserResult get(GetUserQuery query) {
        return getUserQueryExe.execute(query);
    }

}
