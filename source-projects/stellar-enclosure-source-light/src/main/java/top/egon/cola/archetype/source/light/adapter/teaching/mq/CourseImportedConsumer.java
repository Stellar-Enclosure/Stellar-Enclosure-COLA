package top.egon.cola.archetype.source.light.adapter.teaching.mq;

import lombok.extern.slf4j.Slf4j;
import top.egon.cola.archetype.source.light.adapter.teaching.validators.TeachingRequestValidator;
import top.egon.cola.archetype.source.light.application.teaching.executor.event.CourseImportedEventExe;
import top.egon.cola.archetype.source.light.adapter.teaching.pojo.convertor.TeachingAdapterConvertor;
import top.egon.cola.archetype.source.light.application.teaching.manage.CourseManage;
import top.egon.cola.archetype.source.light.facade.teaching.dto.CreateCourseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;
import org.mapstruct.factory.Mappers;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class CourseImportedConsumer {
    private final CourseImportedEventExe courseImportedEventExe;
    private final TeachingRequestValidator validator;
    @Qualifier("teachingAdapterConvertorImpl")
    private final TeachingAdapterConvertor convertor;

    public CourseImportedConsumer(CourseManage courseManage, TeachingRequestValidator validator) {
        this(new CourseImportedEventExe(courseManage), validator, Mappers.getMapper(TeachingAdapterConvertor.class));
    }

    @RabbitListener(
            queues = "${spring.application.name}.course.imported",
            errorHandler = "rabbitConsumerErrorHandler",
            autoStartup = "${app.integrations.rabbitmq.enabled:false}")
    public void consume(CreateCourseDTO message) {
        validator.validate(message);
        courseImportedEventExe.execute(convertor.toImportedEvent(message));
    }
}
