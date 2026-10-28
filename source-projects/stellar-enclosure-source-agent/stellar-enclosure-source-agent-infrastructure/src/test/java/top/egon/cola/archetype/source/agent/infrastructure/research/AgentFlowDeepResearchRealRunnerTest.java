package top.egon.cola.archetype.source.agent.infrastructure.research;

import com.google.adk.sessions.Session;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import reactor.core.publisher.Flux;
import top.egon.cola.archetype.source.agent.domain.research.model.DeepResearchEvent;
import top.egon.cola.archetype.source.agent.domain.research.model.DeepResearchTaskBO;
import top.egon.cola.archetype.source.agent.domain.research.model.ReportLanguageEnum;
import top.egon.cola.archetype.source.agent.infrastructure.research.service.impl.AgentFlowDeepResearchAgentServiceImpl;
import top.egon.cola.component.agentflow.api.AgentFlowService;
import top.egon.cola.component.agentflow.api.AgentFlowSessionCommand;
import top.egon.cola.component.agentflow.autoconfigure.AgentFlowProperties;
import top.egon.cola.component.agentflow.config.AgentConfigDTO;
import top.egon.cola.component.agentflow.config.AgentFlowConfigDTO;
import top.egon.cola.component.agentflow.config.AgentFlowConfigValidator;
import top.egon.cola.component.agentflow.config.AgentWorkflowConfigDTO;
import top.egon.cola.component.agentflow.config.AgentWorkflowTypeEnum;
import top.egon.cola.component.agentflow.execution.AgentFlowSessionExecutionGuard;
import top.egon.cola.component.agentflow.execution.DefaultAgentFlowService;
import top.egon.cola.component.agentflow.runtime.AgentFlowFactory;
import top.egon.cola.component.agentflow.runtime.AgentFlowRuntimeBO;
import top.egon.cola.component.agentflow.runtime.DefaultAgentFlowRegistry;
import top.egon.cola.component.agentflow.runtime.SpringAiModelAdapterFactory;
import top.egon.cola.component.agentflow.workflow.AgentWorkflowStrategyFactory;
import top.egon.cola.component.agentflow.workflow.LoopAgentWorkflowBuilderStrategy;
import top.egon.cola.component.agentflow.workflow.ParallelAgentWorkflowBuilderStrategy;
import top.egon.cola.component.agentflow.workflow.SequentialAgentWorkflowBuilderStrategy;
import top.egon.cola.component.common.core.validation.ValidationUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Timeout(20)
class AgentFlowDeepResearchRealRunnerTest {
    private static final Instant NOW = Instant.parse("2026-09-04T08:00:00Z");

    @Test
    void completes_the_real_sequential_parallel_tree_and_deletes_the_session_after_root_completion() throws Exception {
        try (var validators = Validation.buildDefaultValidatorFactory()) {
            ValidationUtils validation = new ValidationUtils(validators.getValidator());
            FixtureModel model = new FixtureModel();
            AgentFlowConfigDTO flowConfig = flow();
            AgentFlowProperties properties = new AgentFlowProperties(true, Duration.ofSeconds(10),
                    Duration.ofSeconds(2), Map.of("deep-research", flowConfig));
            new AgentFlowConfigValidator(validation).validate(properties);
            AgentFlowFactory factory = new AgentFlowFactory(new SpringAiModelAdapterFactory(),
                    new AgentWorkflowStrategyFactory(List.of(new SequentialAgentWorkflowBuilderStrategy(),
                            new ParallelAgentWorkflowBuilderStrategy(), new LoopAgentWorkflowBuilderStrategy())));
            var compilation = factory.create("deep-research", flowConfig, Map.of("researchModel", model));
            AgentFlowRuntimeBO runtime = new AgentFlowRuntimeBO("deep-research", "deep-research", "researchModel",
                    "fixture-model", compilation.rootAgent(), compilation.runner(), NOW);
            var registry = new DefaultAgentFlowRegistry(Map.of("deep-research", runtime), Duration.ofSeconds(2));
            try (DefaultAgentFlowService real = new DefaultAgentFlowService(registry,
                    new AgentFlowSessionExecutionGuard(), properties, validation, Clock.fixed(NOW, ZoneOffset.UTC))) {
                AgentFlowService service = spy(real);
                CountDownLatch deleted = new CountDownLatch(1);
                doAnswer(invocation -> {
                    try {
                        return invocation.callRealMethod();
                    } finally {
                        deleted.countDown();
                    }
                }).when(service).deleteSession(any(AgentFlowSessionCommand.class));
                var gateway = new AgentFlowDeepResearchAgentServiceImpl(service, Clock.fixed(NOW, ZoneOffset.UTC));
                List<DeepResearchEvent> received = new CopyOnWriteArrayList<>();
                CountDownLatch terminal = new CountDownLatch(1);
                var task = DeepResearchTaskBO.create("run-real", "fixture topic", ReportLanguageEnum.EN_US,
                        8, NOW.plusSeconds(300), "trace-real");
                gateway.start(task, event -> {
                    received.add(event);
                    if (event.isTerminal()) terminal.countDown();
                });

                assertTrue(terminal.await(10, TimeUnit.SECONDS));
                assertTrue(deleted.await(5, TimeUnit.SECONDS));
                assertEquals(5, model.streams.get());
                assertEquals("COMPLETED", received.getLast().type().name());
                assertEquals("# final report", received.getLast().reportMarkdown());
                assertEquals(1, received.stream().filter(DeepResearchEvent::isTerminal).count());
                var command = org.mockito.ArgumentCaptor.forClass(AgentFlowSessionCommand.class);
                verify(service).deleteSession(command.capture());
                Session remaining = compilation.runner().sessionService().getSession("deep-research", "research-run-real",
                        command.getValue().sessionId(), Optional.empty()).blockingGet();
                assertNull(remaining);
            }
        }
    }

    private static AgentFlowConfigDTO flow() {
        List<AgentConfigDTO> agents = List.of(
                agent("Planner", "PLAN"), agent("EvidenceResearcher", "EVIDENCE"),
                agent("CounterpointResearcher", "COUNTERPOINT"), agent("FreshnessResearcher", "FRESHNESS"),
                agent("Writer", "WRITE"));
        return new AgentFlowConfigDTO("researchModel", "fixture-model", "deep-research", agents, List.of(
                new AgentWorkflowConfigDTO(AgentWorkflowTypeEnum.SEQUENTIAL, "deep-research", "root",
                        List.of("Planner", "ParallelResearch", "Writer"), null),
                new AgentWorkflowConfigDTO(AgentWorkflowTypeEnum.PARALLEL, "ParallelResearch", "researchers",
                        List.of("EvidenceResearcher", "CounterpointResearcher", "FreshnessResearcher"), null)));
    }

    private static AgentConfigDTO agent(String name, String instruction) {
        return new AgentConfigDTO(name, name, instruction, name + "_output");
    }

    private static final class FixtureModel implements ChatModel {
        private final AtomicInteger streams = new AtomicInteger();

        @Override
        public ChatResponse call(Prompt prompt) {
            throw new AssertionError("the real gateway should use streaming");
        }

        @Override
        public Flux<ChatResponse> stream(Prompt prompt) {
            return Flux.defer(() -> {
                streams.incrementAndGet();
                String instructions = prompt.getInstructions().stream().filter(SystemMessage.class::isInstance)
                        .map(message -> message.getText() == null ? "" : message.getText())
                        .reduce("", (a, b) -> a + " " + b);
                String output = instructions.contains("WRITE") ? "# final report" : "research step";
                return Flux.just(response(output.substring(0, 2), ""), response(output.substring(2), "stop"));
            });
        }

        @Override
        public ChatOptions getOptions() {
            return ToolCallingChatOptions.builder().model("fixture-model").build();
        }

        private static ChatResponse response(String text, String finishReason) {
            return new ChatResponse(List.of(new Generation(new AssistantMessage(text),
                    ChatGenerationMetadata.builder().finishReason(finishReason).build())));
        }
    }
}
