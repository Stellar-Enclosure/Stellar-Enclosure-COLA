package top.egon.cola.archetype.source.agent.application.knowledge.executor.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import top.egon.cola.archetype.source.agent.application.knowledge.pojo.command.AskKnowledgeBaseCommand;
import top.egon.cola.archetype.source.agent.common.exception.KnowledgeApplicationException;
import top.egon.cola.archetype.source.agent.application.knowledge.service.KnowledgeQaCapacityService;
import top.egon.cola.archetype.source.agent.common.error.KnowledgeErrorCodeEnum;
import top.egon.cola.archetype.source.agent.domain.knowledge.service.KnowledgeAnswerService;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeAnswerTaskBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeBaseBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeQaEvent;
import top.egon.cola.archetype.source.agent.domain.knowledge.model.KnowledgeRetrievedChunkBO;
import top.egon.cola.archetype.source.agent.domain.knowledge.service.KnowledgeAnswerRunService;
import top.egon.cola.archetype.source.agent.domain.knowledge.service.KnowledgeQaEventObserverService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import top.egon.cola.archetype.source.agent.application.knowledge.service.KnowledgeRetrievalService;

@Service("askKnowledgeBaseCmdExe")
@Slf4j(topic = "top.egon.cola.archetype.source.agent.application.knowledge.manage.impl.KnowledgeQaManageImpl")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AskKnowledgeBaseCmdExe {

    @Qualifier("knowledgeRetrievalService")
    private final KnowledgeRetrievalService retrievalService;

    /** The answer path takes every retrieved chunk: the reference cap is {@code topK}, not a floor. */
    private static final double ACCEPT_EVERY_SCORE = 0.0d;

    @Qualifier("knowledgeAnswerService")
    private final KnowledgeAnswerService answerService;

    @Qualifier("knowledgeQaCapacityService")
    private final KnowledgeQaCapacityService capacityService;

    @Qualifier("agentClock")
    private final Clock clock;

    public KnowledgeAnswerRunService execute(AskKnowledgeBaseCommand command, KnowledgeQaEventObserverService observer) {
        retrievalService.validate(command, command == null ? null : command.traceId());
        if (observer == null) {
            throw new KnowledgeApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_VALIDATION_ERROR,
                    command == null ? null : command.traceId());
        }
        String traceId = command.traceId();
        KnowledgeBaseBO base = retrievalService.requiredBase(command.knowledgeBaseId(), traceId);
        KnowledgeQaCapacityService.Lease lease = acquire(base, traceId);

        List<KnowledgeRetrievedChunkBO> matched;
        try {
            // A saturated instance refuses before the retrieval, and an empty result is a valid
            // answer source: the model is told there is nothing to cite instead of being skipped.
            matched = retrievalService.search(base, command.question(), command.topK(), ACCEPT_EVERY_SCORE, traceId);
        } catch (RuntimeException failure) {
            lease.close();
            throw failure;
        }

        Instant startedAt = clock.instant();
        String answerId = UUID.randomUUID().toString();
        // The task carries the chunk text, which is what the model answers from; the events carry the
        // same chunks without it, which is what the caller is shown.
        KnowledgeAnswerTaskBO task = new KnowledgeAnswerTaskBO(answerId, command.question(),
                base.embeddingModel(), matched, traceId);
        AtomicBoolean terminal = new AtomicBoolean();
        AtomicReference<KnowledgeAnswerRunService> downstream = new AtomicReference<>();
        KnowledgeQaEventObserverService guardedObserver =
                guardedObserver(observer, terminal, downstream, lease, base, answerId, startedAt);
        try {
            KnowledgeAnswerRunService run = Objects.requireNonNull(answerService.generate(task, guardedObserver),
                    "the answer service returned no run");
            downstream.set(run);
            if (terminal.get()) {
                // The generation ended while it was being set up: its events are done, so the model
                // subscription is released instead of being held by a handle nobody will cancel.
                cancelDownstream(run);
            }
            return new ManagedRunService(run, terminal, lease);
        } catch (KnowledgeApplicationException failure) {
            lease.close();
            throw failure;
        } catch (RuntimeException failure) {
            // Nothing was published, so the caller still sees an ordinary failure response rather
            // than a stream that ends without a terminal event.
            lease.close();
            log.warn("knowledge qa answerId={} knowledgeBaseId={} logicalModelName={} outcome=FAILED code={}",
                    answerId, base.knowledgeBaseId(), base.embeddingModel(),
                    KnowledgeErrorCodeEnum.KNOWLEDGE_DEPENDENCY_UNAVAILABLE.code());
            throw new KnowledgeApplicationException(
                    KnowledgeErrorCodeEnum.KNOWLEDGE_DEPENDENCY_UNAVAILABLE, traceId, failure);
        }
    }

    /**
     * Forwards the generation's events, releasing the permit on the one that ends the stream.
     *
     * <p>The permit is closed before the observer is told nothing further will come, and an observer
     * that fails ends the stream the same way a terminal event does: the caller is gone, so the
     * model is cancelled and the permit released rather than left with a reader that never returns.
     */
    private KnowledgeQaEventObserverService guardedObserver(KnowledgeQaEventObserverService observer,
                                                            AtomicBoolean terminal,
                                                            AtomicReference<KnowledgeAnswerRunService> downstream,
                                                            KnowledgeQaCapacityService.Lease lease,
                                                            KnowledgeBaseBO base, String answerId,
                                                            Instant startedAt) {
        return event -> {
            if (terminal.get()) {
                return;
            }
            try {
                KnowledgeQaEvent checked = Objects.requireNonNull(event, "qa event must not be null");
                observer.onEvent(checked);
                if (checked.isTerminal() && terminal.compareAndSet(false, true)) {
                    lease.close();
                    log.info("knowledge qa answerId={} knowledgeBaseId={} logicalModelName={} stage=RUN"
                                    + " outcome={} durationMs={} code={}",
                            answerId, base.knowledgeBaseId(), base.embeddingModel(), checked.type(),
                            durationMs(startedAt), checked.errorCode() == null ? "NONE" : checked.errorCode().code());
                }
            } catch (RuntimeException observerFailure) {
                if (terminal.compareAndSet(false, true)) {
                    lease.close();
                    cancelDownstream(downstream.get());
                    log.warn("knowledge qa answerId={} knowledgeBaseId={} logicalModelName={} stage=RUN"
                                    + " outcome=OBSERVER_ERROR durationMs={} code={}",
                            answerId, base.knowledgeBaseId(), base.embeddingModel(), durationMs(startedAt),
                            KnowledgeErrorCodeEnum.KNOWLEDGE_INTERNAL_ERROR.code());
                }
                throw observerFailure;
            }
        };
    }

    private KnowledgeQaCapacityService.Lease acquire(KnowledgeBaseBO base, String traceId) {
        Optional<KnowledgeQaCapacityService.Lease> acquired = capacityService.tryAcquire();
        if (acquired.isEmpty()) {
            log.warn("knowledge qa knowledgeBaseId={} outcome=REJECTED code={}", base.knowledgeBaseId(),
                    KnowledgeErrorCodeEnum.KNOWLEDGE_CAPACITY_EXHAUSTED.code());
            throw new KnowledgeApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_CAPACITY_EXHAUSTED, traceId);
        }
        return acquired.get();
    }

    private static void cancelDownstream(KnowledgeAnswerRunService run) {
        if (run == null) {
            return;
        }
        try {
            run.cancel();
        } catch (RuntimeException ignored) {
            // The permit is released by the caller; downstream cancellation is best effort.
        }
    }

    private long durationMs(Instant startedAt) {
        return Duration.between(startedAt, clock.instant()).toMillis();
    }

    /** The handle that ends one generation early; its permit is released once, on the first close. */
    private static final class ManagedRunService implements KnowledgeAnswerRunService {

        private final KnowledgeAnswerRunService downstream;

        private final AtomicBoolean terminal;

        private final KnowledgeQaCapacityService.Lease lease;

        private final AtomicBoolean cancelled = new AtomicBoolean();

        private ManagedRunService(KnowledgeAnswerRunService downstream, AtomicBoolean terminal,
                                  KnowledgeQaCapacityService.Lease lease) {
            this.downstream = downstream;
            this.terminal = terminal;
            this.lease = lease;
        }

        @Override
        public void cancel() {
            // A run that already ended has released its permit; cancelling it again must not.
            if (!terminal.compareAndSet(false, true) || !cancelled.compareAndSet(false, true)) {
                return;
            }
            try {
                downstream.cancel();
            } finally {
                lease.close();
            }
        }
    }
}
