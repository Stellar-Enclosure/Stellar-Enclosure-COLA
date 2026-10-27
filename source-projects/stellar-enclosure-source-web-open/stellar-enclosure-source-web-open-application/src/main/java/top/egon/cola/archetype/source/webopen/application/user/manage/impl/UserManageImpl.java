package top.egon.cola.archetype.source.webopen.application.user.manage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.archetype.source.webopen.application.user.manage.UserManage;
import top.egon.cola.archetype.source.webopen.application.user.pojo.command.CreateUserCommand;
import top.egon.cola.archetype.source.webopen.application.user.pojo.convertor.UserConverter;
import top.egon.cola.archetype.source.webopen.application.user.pojo.query.UserDetailQuery;
import top.egon.cola.archetype.source.webopen.application.user.pojo.result.UserDetailResult;
import top.egon.cola.archetype.source.webopen.application.user.validators.UserApplicationValidator;
import top.egon.cola.archetype.source.webopen.domain.service.CommandIdempotencyService;
import top.egon.cola.archetype.source.webopen.domain.service.OrganizationEventService;
import top.egon.cola.archetype.source.webopen.domain.user.entities.User;
import top.egon.cola.archetype.source.webopen.domain.user.service.UserDomainService;
import top.egon.cola.archetype.source.webopen.application.user.executor.command.CreateUserCmdExe;
import top.egon.cola.archetype.source.webopen.application.user.executor.query.UserDetailQueryExe;

/** User use cases; caching and transport stay inside the domain service implementations. */
@Service("userManage")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserManageImpl implements UserManage {

    @Qualifier("createUserCmdExe")
    private final CreateUserCmdExe createUserCmdExe;

    @Qualifier("userDetailQueryExe")
    private final UserDetailQueryExe userDetailQueryExe;

    public UserManageImpl(UserDomainService userDomainService,
            UserApplicationValidator validator,
            UserConverter converter,
            CommandIdempotencyService idempotency,
            OrganizationEventService eventService) {
        this(new CreateUserCmdExe(userDomainService, validator, converter, idempotency, eventService),
                new UserDetailQueryExe(userDomainService, converter));
    }

    @Override
    @Transactional
    public UserDetailResult createUser(CreateUserCommand command) {
        return createUserCmdExe.execute(command);
    }

    @Override
    public UserDetailResult getUser(UserDetailQuery query) {
        return userDetailQueryExe.execute(query);
    }

}
