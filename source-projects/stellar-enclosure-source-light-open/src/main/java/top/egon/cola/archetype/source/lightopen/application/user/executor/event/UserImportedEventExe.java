package top.egon.cola.archetype.source.lightopen.application.user.executor.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import top.egon.cola.archetype.source.lightopen.application.user.manage.UserManage;
import top.egon.cola.archetype.source.lightopen.application.user.pojo.convertor.UserApplicationConvertor;
import top.egon.cola.archetype.source.lightopen.application.user.pojo.event.UserImportedEvent;

@Service("userImportedEventExe")
@Lazy
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserImportedEventExe {

    @Qualifier("userManageImpl")
    private final UserManage manage;

    @Qualifier("userApplicationConvertorImpl")
    private final UserApplicationConvertor convertor;

    public UserImportedEventExe(UserManage manage) {
        this(manage, Mappers.getMapper(UserApplicationConvertor.class));
    }

    public void execute(@Valid @NotNull UserImportedEvent event) {
        manage.create(convertor.toImportedCommand(event));
    }
}
