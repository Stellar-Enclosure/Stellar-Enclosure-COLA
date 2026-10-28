package top.egon.cola.archetype.source.agent.infrastructure.research;

import com.google.adk.events.Event;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.junit.jupiter.api.Test;
import top.egon.cola.archetype.source.agent.domain.research.model.DeepResearchEvent;
import top.egon.cola.archetype.source.agent.domain.research.model.DeepResearchTaskBO;
import top.egon.cola.archetype.source.agent.domain.research.model.ReportLanguageEnum;
import top.egon.cola.archetype.source.agent.domain.research.service.DeepResearchEventObserverService;
import top.egon.cola.archetype.source.agent.infrastructure.research.service.impl.AgentFlowDeepResearchAgentServiceImpl;
import top.egon.cola.component.agentflow.api.AgentFlowExecutionCommand;
import top.egon.cola.component.agentflow.api.AgentFlowService;
import top.egon.cola.component.agentflow.api.AgentFlowSessionCommand;
import top.egon.cola.component.agentflow.api.AgentFlowSessionResult;
import top.egon.cola.archetype.source.agent.domain.research.service.DeepResearchRunService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentFlowDeepResearchAgentGatewayTest {

    private static final Instant NOW = Instant.parse("2026-09-04T08:00:00Z");

    @Test
    void waits_for_root_completion_before_emitting_the_writer_report_and_deleting_the_session() {
        AgentFlowService flow = mock(AgentFlowService.class);
        PublishProcessor<Event> events = PublishProcessor.create();
        when(flow.createSession(any(AgentFlowSessionCommand.class)))
                .thenReturn(new AgentFlowSessionResult("deep-research", "session-1", NOW));
        when(flow.executeStream(any(AgentFlowExecutionCommand.class))).thenReturn(events);
        AgentFlowDeepResearchAgentServiceImpl gateway = gateway(flow);
        List<DeepResearchEvent> received = new ArrayList<>();

        DeepResearchRunService run = gateway.start(task(), received::add);
        events.onNext(progress("Planner", "planning"));
        events.onNext(finalOutput("Planner", "a plan is not the final report"));
        events.onNext(completed("# report"));
        assertEquals(2, received.size());
        assertFalse(received.stream().anyMatch(DeepResearchEvent::isTerminal));
        assertTrue(events.hasSubscribers());
        verify(flow, never()).deleteSession(any(AgentFlowSessionCommand.class));
        events.onComplete();

        assertEquals("run-1", run.runId());
        assertEquals(3, received.size());
        assertEquals(List.of(1L, 2L, 3L), received.stream().map(DeepResearchEvent::sequence).toList());
        assertEquals("STARTED", received.getFirst().type().name());
        assertEquals("COMPLETED", received.getLast().type().name());
        assertEquals("# report", received.getLast().reportMarkdown());
        verify(flow).createSession(new AgentFlowSessionCommand("deep-research", "research-run-1", null));
        verify(flow).executeStream(any(AgentFlowExecutionCommand.class));
        verify(flow).deleteSession(new AgentFlowSessionCommand("deep-research", "research-run-1", "session-1"));
    }

    @Test
    void writer_output_followed_by_failure_does_not_claim_success() {
        AgentFlowService flow = mock(AgentFlowService.class);
        PublishProcessor<Event> events = PublishProcessor.create();
        when(flow.createSession(any(AgentFlowSessionCommand.class)))
                .thenReturn(new AgentFlowSessionResult("deep-research", "session-1", NOW));
        when(flow.executeStream(any(AgentFlowExecutionCommand.class))).thenReturn(events);
        List<DeepResearchEvent> received = new ArrayList<>();

        gateway(flow).start(task(), received::add);
        events.onNext(completed("# report"));
        events.onError(new IllegalStateException("failure after writer"));

        assertEquals("FAILED", received.getLast().type().name());
        assertFalse(received.stream().anyMatch(event -> event.type().name().equals("COMPLETED")));
        verify(flow).deleteSession(new AgentFlowSessionCommand("deep-research", "research-run-1", "session-1"));
    }

    @Test
    void root_completion_without_a_writer_report_fails_closed() {
        AgentFlowService flow = mock(AgentFlowService.class);
        PublishProcessor<Event> events = PublishProcessor.create();
        when(flow.createSession(any(AgentFlowSessionCommand.class)))
                .thenReturn(new AgentFlowSessionResult("deep-research", "session-1", NOW));
        when(flow.executeStream(any(AgentFlowExecutionCommand.class))).thenReturn(events);
        List<DeepResearchEvent> received = new ArrayList<>();

        gateway(flow).start(task(), received::add);
        events.onNext(finalOutput("Planner", "a plan"));
        events.onComplete();

        assertEquals("FAILED", received.getLast().type().name());
        verify(flow).deleteSession(new AgentFlowSessionCommand("deep-research", "research-run-1", "session-1"));
    }

    @Test
    void synchronous_failure_event_disposes_before_later_work_can_start() {
        AgentFlowService flow = mock(AgentFlowService.class);
        AtomicInteger laterWork = new AtomicInteger();
        when(flow.createSession(any(AgentFlowSessionCommand.class)))
                .thenReturn(new AgentFlowSessionResult("deep-research", "session-1", NOW));
        Event failure = Event.builder().id("failure").author("Planner")
                .errorMessage("dependency failure").timestamp(NOW.toEpochMilli()).build();
        when(flow.executeStream(any(AgentFlowExecutionCommand.class))).thenReturn(
                Flowable.just(failure).concatWith(Flowable.defer(() -> {
                    laterWork.incrementAndGet();
                    return Flowable.empty();
                })));
        List<DeepResearchEvent> received = new ArrayList<>();

        gateway(flow).start(task(), received::add);

        assertEquals("FAILED", received.getLast().type().name());
        assertEquals(0, laterWork.get());
        verify(flow).deleteSession(new AgentFlowSessionCommand("deep-research", "research-run-1", "session-1"));
    }

    @Test
    void maps_stream_error_to_safe_failed_event_and_deletes_session_once() {
        AgentFlowService flow = mock(AgentFlowService.class);
        PublishProcessor<Event> events = PublishProcessor.create();
        when(flow.createSession(any(AgentFlowSessionCommand.class)))
                .thenReturn(new AgentFlowSessionResult("deep-research", "session-1", NOW));
        when(flow.executeStream(any(AgentFlowExecutionCommand.class))).thenReturn(events);
        AgentFlowDeepResearchAgentServiceImpl gateway = gateway(flow);
        List<DeepResearchEvent> received = new ArrayList<>();

        gateway.start(task(), received::add);
        events.onError(new IllegalStateException("provider-secret-must-not-escape"));

        assertEquals("FAILED", received.getLast().type().name());
        assertEquals(2, received.getLast().sequence());
        assertEquals("research dependency is unavailable", received.getLast().errorMessage());
        assertFalse(received.getLast().errorMessage().contains("provider-secret"));
        verify(flow).deleteSession(new AgentFlowSessionCommand("deep-research", "research-run-1", "session-1"));
    }

    @Test
    void cancellation_disposes_stream_and_deletes_session_exactly_once() {
        AgentFlowService flow = mock(AgentFlowService.class);
        PublishProcessor<Event> events = PublishProcessor.create();
        when(flow.createSession(any(AgentFlowSessionCommand.class)))
                .thenReturn(new AgentFlowSessionResult("deep-research", "session-1", NOW));
        when(flow.executeStream(any(AgentFlowExecutionCommand.class))).thenReturn(events);
        AgentFlowDeepResearchAgentServiceImpl gateway = gateway(flow);

        DeepResearchRunService run = gateway.start(task(), event -> { });
        run.cancel();
        run.cancel();
        events.onNext(progress("Planner", "late"));

        assertFalse(events.hasSubscribers());
        verify(flow).deleteSession(new AgentFlowSessionCommand("deep-research", "research-run-1", "session-1"));
    }

    @Test
    void session_creation_failure_does_not_attempt_delete() {
        AgentFlowService flow = mock(AgentFlowService.class);
        when(flow.createSession(any(AgentFlowSessionCommand.class)))
                .thenThrow(new IllegalStateException("not available"));
        AgentFlowDeepResearchAgentServiceImpl gateway = gateway(flow);

        assertThrows(IllegalStateException.class, () -> gateway.start(task(), event -> { }));

        verify(flow).createSession(any(AgentFlowSessionCommand.class));
        verify(flow, never()).executeStream(any(AgentFlowExecutionCommand.class));
        verify(flow, never()).deleteSession(any(AgentFlowSessionCommand.class));
    }

    private static AgentFlowDeepResearchAgentServiceImpl gateway(AgentFlowService flow) {
        return new AgentFlowDeepResearchAgentServiceImpl(flow,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static DeepResearchTaskBO task() {
        return DeepResearchTaskBO.create("run-1", "agent architecture", ReportLanguageEnum.EN_US,
                8, NOW.plusSeconds(300), "trace-1");
    }

    private static Event progress(String author, String text) {
        return Event.builder().id("event-" + author).author(author).partial(true)
                .content(Content.fromParts(Part.fromText(text))).timestamp(NOW.toEpochMilli()).build();
    }

    private static Event completed(String report) {
        return finalOutput("Writer", report);
    }

    private static Event finalOutput(String author, String report) {
        return Event.builder().id("event-" + author).author(author)
                .content(Content.fromParts(Part.fromText(report))).timestamp(NOW.toEpochMilli()).build();
    }
}
