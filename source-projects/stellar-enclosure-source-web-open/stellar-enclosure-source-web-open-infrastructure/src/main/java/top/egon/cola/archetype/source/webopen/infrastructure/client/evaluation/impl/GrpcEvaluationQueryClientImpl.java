package top.egon.cola.archetype.source.webopen.infrastructure.client.evaluation.impl;

import top.egon.cola.archetype.source.webopen.common.exception.ExternalDependencyException;
import top.egon.cola.archetype.source.webopen.common.enums.ExternalDependencyFailure;
import top.egon.cola.archetype.source.webopen.domain.teaching.vos.EvaluationCourseBO;
import top.egon.cola.archetype.source.webopen.domain.teaching.vos.EvaluationExamBO;
import top.egon.cola.archetype.source.webopen.infrastructure.client.evaluation.EvaluationClientFailureMapper;
import top.egon.cola.archetype.source.webopen.infrastructure.client.evaluation.EvaluationQueryClient;
import top.egon.cola.archetype.source.webopen.domain.teaching.vos.EvaluationScoreBO;
import top.egon.cola.archetype.source.webopen.infrastructure.config.OrganizationIntegrationProperties;
import com.google.protobuf.Message;
import top.egon.cola.archetype.source.serviceopen.facade.evaluation.v1.Course;
import top.egon.cola.archetype.source.serviceopen.facade.evaluation.v1.Exam;
import top.egon.cola.archetype.source.serviceopen.facade.evaluation.v1.GetCourseRequest;
import top.egon.cola.archetype.source.serviceopen.facade.evaluation.v1.GetExamRequest;
import top.egon.cola.archetype.source.serviceopen.facade.evaluation.v1.GetScoreRequest;
import top.egon.cola.archetype.source.serviceopen.facade.evaluation.v1.Score;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Standard gRPC client for the three read-only Evaluation Triple methods. */
@Component("evaluationQueryClient")
@Profile({"dev", "prod"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class GrpcEvaluationQueryClientImpl implements EvaluationQueryClient {

    private static final String COURSE_SERVICE = "egon.evaluation.v1.CourseService";
    private static final String EXAM_SERVICE = "egon.evaluation.v1.ExamService";
    private static final String SCORE_SERVICE = "egon.evaluation.v1.ScoreService";

    private static final String SERVICE_LABEL = "[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?";
    private static final Pattern KUBERNETES_TARGET = Pattern.compile(
            "dns:///" + SERVICE_LABEL + "\\." + SERVICE_LABEL + "\\.svc\\.cluster\\.local:50051");

    @Qualifier("evaluationGrpcChannel")
    private final ManagedChannel channel;
    @Qualifier("evaluationGrpcDeadline")
    private final Duration deadline;

    @Bean(name = "evaluationGrpcChannel", destroyMethod = "shutdown")
    static ManagedChannel evaluationGrpcChannel(
            @Qualifier("organizationIntegrationProperties") OrganizationIntegrationProperties properties) {
        String target = properties.getIntegrations().getEvaluation().getGrpcTarget();
        if (target == null || !KUBERNETES_TARGET.matcher(target).matches()) {
            throw new IllegalArgumentException("Evaluation gRPC target must name an approved Kubernetes Service");
        }
        log.debug("Creating the Evaluation gRPC channel for the configured Kubernetes Service");
        return ManagedChannelBuilder.forTarget(target).usePlaintext().disableRetry().build();
    }

    @Bean("evaluationGrpcDeadline")
    static Duration evaluationGrpcDeadline(
            @Qualifier("organizationIntegrationProperties") OrganizationIntegrationProperties properties) {
        return properties.getIntegrations().getEvaluation().getGrpcDeadline();
    }

    @Override
    public EvaluationCourseBO getCourse(Long courseId) {
        long id = positiveId(courseId, "courseId");
        try {
            Course response = ClientCalls.blockingUnaryCall(
                    channel, getCourseMethod(), callOptions(), GetCourseRequest.newBuilder()
                            .setCourseId(id)
                            .build());
            if (response == null || response.getId() <= 0) {
                throw EvaluationClientFailureMapper.incompatible("getCourse");
            }
            return new EvaluationCourseBO(response.getId(), response.getCode(), response.getName(),
                    response.getCredit(), response.getStatus());
        } catch (ExternalDependencyException failure) {
            throw failure;
        } catch (RuntimeException failure) {
            throw EvaluationClientFailureMapper.map(failure);
        }
    }

    @Override
    public EvaluationExamBO getExam(Long examId) {
        long id = positiveId(examId, "examId");
        try {
            Exam response = ClientCalls.blockingUnaryCall(
                    channel, getExamMethod(), callOptions(), GetExamRequest.newBuilder()
                            .setExamId(id)
                            .build());
            if (response == null || response.getId() <= 0) {
                throw EvaluationClientFailureMapper.incompatible("getExam");
            }
            return new EvaluationExamBO(response.getId(), response.getCourseId(),
                    response.getTitle(), timestamp(response.getStartsAt().getSeconds(), response.getStartsAt().getNanos()),
                    timestamp(response.getEndsAt().getSeconds(), response.getEndsAt().getNanos()), response.getStatus());
        } catch (ExternalDependencyException failure) {
            throw failure;
        } catch (RuntimeException failure) {
            throw EvaluationClientFailureMapper.map(failure);
        }
    }

    @Override
    public EvaluationScoreBO getScore(Long examId, Long scoreId) {
        long exam = positiveId(examId, "examId");
        long score = positiveId(scoreId, "scoreId");
        try {
            Score response = ClientCalls.blockingUnaryCall(
                    channel, getScoreMethod(), callOptions(), GetScoreRequest.newBuilder()
                            .setExamId(exam)
                            .setScoreId(score)
                            .build());
            if (response == null || response.getId() <= 0) {
                throw EvaluationClientFailureMapper.incompatible("getScore");
            }
            return new EvaluationScoreBO(response.getId(), response.getExamId(),
                    response.getCourseId(), response.getStudentId(),
                    response.getPoints(), response.getStatus());
        } catch (ExternalDependencyException failure) {
            throw failure;
        } catch (RuntimeException failure) {
            throw EvaluationClientFailureMapper.map(failure);
        }
    }

    private CallOptions callOptions() {
        return CallOptions.DEFAULT.withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS);
    }

    private static MethodDescriptor<GetCourseRequest, Course> getCourseMethod() {
        return unary(COURSE_SERVICE, "GetCourse", GetCourseRequest.getDefaultInstance(), Course.getDefaultInstance());
    }

    private static MethodDescriptor<GetExamRequest, Exam> getExamMethod() {
        return unary(EXAM_SERVICE, "GetExam", GetExamRequest.getDefaultInstance(), Exam.getDefaultInstance());
    }

    private static MethodDescriptor<GetScoreRequest, Score> getScoreMethod() {
        return unary(SCORE_SERVICE, "GetScore", GetScoreRequest.getDefaultInstance(), Score.getDefaultInstance());
    }

    private static <Req extends Message, Resp extends Message> MethodDescriptor<Req, Resp> unary(
            String service, String method, Req request, Resp response) {
        return MethodDescriptor.<Req, Resp>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(MethodDescriptor.generateFullMethodName(service, method))
                .setRequestMarshaller(ProtoUtils.marshaller(request))
                .setResponseMarshaller(ProtoUtils.marshaller(response))
                .build();
    }

    private static long positiveId(Long raw, String field) {
        if (raw == null || raw <= 0) {
            throw new ExternalDependencyException(
                    "evaluation", ExternalDependencyFailure.VALIDATION_FAILED, "INVALID_ID",
                    field + " must be a positive Long", null);
        }
        return raw;
    }

    private static Instant timestamp(long seconds, int nanos) {
        return Instant.ofEpochSecond(seconds, nanos);
    }
}
