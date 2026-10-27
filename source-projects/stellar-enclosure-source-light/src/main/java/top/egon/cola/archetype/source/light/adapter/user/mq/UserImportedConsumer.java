package top.egon.cola.archetype.source.light.adapter.user.mq;

import lombok.extern.slf4j.Slf4j;
import top.egon.cola.archetype.source.light.adapter.user.validators.UserRequestValidator;
import top.egon.cola.archetype.source.light.application.user.executor.event.UserImportedEventExe;
import top.egon.cola.archetype.source.light.adapter.user.pojo.convertor.UserAdapterConvertor;
import top.egon.cola.archetype.source.light.application.user.manage.UserManage;
import top.egon.cola.archetype.source.light.facade.user.dto.CreateUserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;
import org.mapstruct.factory.Mappers;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class UserImportedConsumer {
    private final UserImportedEventExe userImportedEventExe;
    private final UserRequestValidator validator;
    @Qualifier("userAdapterConvertorImpl")
    private final UserAdapterConvertor convertor;

    public UserImportedConsumer(UserManage userManage, UserRequestValidator validator) {
        this(new UserImportedEventExe(userManage), validator, Mappers.getMapper(UserAdapterConvertor.class));
    }

    @RabbitListener(
            queues = "${spring.application.name}.user.imported",
            errorHandler = "rabbitConsumerErrorHandler",
            autoStartup = "${app.integrations.rabbitmq.enabled:false}")
    public void consume(CreateUserDTO message) {
        validator.validate(message);
        userImportedEventExe.execute(convertor.toImportedEvent(message));
    }
}
