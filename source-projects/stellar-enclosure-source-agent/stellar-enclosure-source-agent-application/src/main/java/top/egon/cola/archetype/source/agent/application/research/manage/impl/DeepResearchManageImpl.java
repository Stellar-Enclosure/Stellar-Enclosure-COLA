package top.egon.cola.archetype.source.agent.application.research.manage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.agent.application.research.pojo.command.StartDeepResearchCommand;
import top.egon.cola.archetype.source.agent.application.research.config.DeepResearchRuntimeProperties;
import top.egon.cola.archetype.source.agent.application.research.manage.DeepResearchManage;
import top.egon.cola.archetype.source.agent.application.research.service.ResearchCapacityService;
import top.egon.cola.archetype.source.agent.domain.research.service.DeepResearchAgentService;
import top.egon.cola.archetype.source.agent.domain.research.service.DeepResearchEventObserverService;
import top.egon.cola.archetype.source.agent.domain.research.service.DeepResearchRunService;
import top.egon.cola.component.common.core.validation.ValidationUtils;
import java.time.Clock;
import top.egon.cola.archetype.source.agent.application.research.executor.command.StartDeepResearchCmdExe;

/** Atomic application use case that owns capacity and terminal cleanup. */
@Service("deepResearchManage")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DeepResearchManageImpl implements DeepResearchManage {

    private final @Qualifier("startDeepResearchCmdExe") StartDeepResearchCmdExe startDeepResearchCmdExe;

    public DeepResearchManageImpl(DeepResearchAgentService agentService,
            ResearchCapacityService capacityService,
            ValidationUtils validationUtils,
            DeepResearchRuntimeProperties properties,
            Clock clock) {
        this(new StartDeepResearchCmdExe(agentService, capacityService, validationUtils, properties, clock));
    }

    @Override
    public DeepResearchRunService startResearch(
            StartDeepResearchCommand command, DeepResearchEventObserverService observer) {
        return startDeepResearchCmdExe.execute(command, observer);
    }

}
