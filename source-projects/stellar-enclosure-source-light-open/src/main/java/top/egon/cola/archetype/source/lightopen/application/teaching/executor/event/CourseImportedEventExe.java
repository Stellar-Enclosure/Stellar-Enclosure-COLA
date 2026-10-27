package top.egon.cola.archetype.source.lightopen.application.teaching.executor.event;

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
import top.egon.cola.archetype.source.lightopen.application.teaching.manage.CourseManage;
import top.egon.cola.archetype.source.lightopen.application.teaching.pojo.convertor.TeachingApplicationConvertor;
import top.egon.cola.archetype.source.lightopen.application.teaching.pojo.event.CourseImportedEvent;

@Service("courseImportedEventExe")
@Lazy
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CourseImportedEventExe {

    @Qualifier("courseManageImpl")
    private final CourseManage manage;

    @Qualifier("teachingApplicationConvertorImpl")
    private final TeachingApplicationConvertor convertor;

    public CourseImportedEventExe(CourseManage manage) {
        this(manage, Mappers.getMapper(TeachingApplicationConvertor.class));
    }

    public void execute(@Valid @NotNull CourseImportedEvent event) {
        manage.create(convertor.toImportedCommand(event));
    }
}
