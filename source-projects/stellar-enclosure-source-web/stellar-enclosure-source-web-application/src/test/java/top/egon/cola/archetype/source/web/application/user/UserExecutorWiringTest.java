package top.egon.cola.archetype.source.web.application.user;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import top.egon.cola.archetype.source.web.application.user.executor.command.CreateUserCmdExe;
import top.egon.cola.archetype.source.web.application.user.executor.query.UserDetailQueryExe;
import top.egon.cola.archetype.source.web.application.user.manage.UserManage;
import top.egon.cola.archetype.source.web.application.user.manage.impl.UserManageImpl;
import top.egon.cola.archetype.source.web.application.user.pojo.command.CreateUserCommand;
import top.egon.cola.archetype.source.web.application.user.pojo.convertor.UserConverter;
import top.egon.cola.archetype.source.web.application.user.pojo.result.UserDetailResult;
import top.egon.cola.archetype.source.web.application.user.validators.UserApplicationValidator;
import top.egon.cola.archetype.source.web.domain.service.CommandIdempotencyService;
import top.egon.cola.archetype.source.web.domain.service.OrganizationEventService;
import top.egon.cola.archetype.source.web.domain.user.entities.User;
import top.egon.cola.archetype.source.web.domain.user.enums.UserStatus;
import top.egon.cola.archetype.source.web.domain.user.service.UserDomainService;
import top.egon.cola.archetype.source.web.domain.user.vos.UserId;
import top.egon.cola.component.common.id.snowflake.SnowflakeIdGenerator;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserExecutorWiringTest {

    @Test
    void spring_injects_executors_and_the_command_joins_the_manage_transaction() {
        SnowflakeIdGenerator.initialize(0L, Duration.ofMillis(5));
        UserDomainService domain = mock(UserDomainService.class);
        UserApplicationValidator validator = mock(UserApplicationValidator.class);
        UserConverter converter = mock(UserConverter.class);
        CommandIdempotencyService idempotency = mock(CommandIdempotencyService.class);
        OrganizationEventService events = mock(OrganizationEventService.class);
        User user = new User(new UserId(2001L), "Mario", "mario@example.com", UserStatus.ACTIVE);
        UserDetailResult expected = new UserDetailResult(2001L, "Mario", "mario@example.com", "ACTIVE", List.of());
        when(validator.normalizedEmail("mario@example.com")).thenReturn("mario@example.com");
        when(idempotency.claim("create-user", "req-1")).thenReturn(true);
        when(domain.create(any(), any(), any())).thenReturn(user);
        when(domain.save(user)).thenReturn(user);
        when(converter.toResult(user)).thenReturn(expected);

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean("userDomainService", UserDomainService.class, () -> domain);
            context.registerBean("userApplicationValidator", UserApplicationValidator.class, () -> validator);
            context.registerBean("userConverterImpl", UserConverter.class, () -> converter);
            context.registerBean("commandIdempotencyService", CommandIdempotencyService.class, () -> idempotency);
            context.registerBean("organizationEventService", OrganizationEventService.class, () -> events);
            context.register(TransactionConfig.class, CreateUserCmdExe.class, UserDetailQueryExe.class, UserManageImpl.class);
            context.refresh();

            UserManage manage = context.getBean(UserManage.class);
            assertEquals(expected, manage.createUser(new CreateUserCommand("req-1", "Mario", "mario@example.com")));
            RecordingTransactionManager transactions = context.getBean(RecordingTransactionManager.class);
            assertEquals(1, transactions.begun);
            assertEquals(1, transactions.committed);
            verify(events, times(1)).publish(any());
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement
    static class TransactionConfig {
        @Bean
        RecordingTransactionManager transactionManager() {
            return new RecordingTransactionManager();
        }
    }

    static class RecordingTransactionManager extends AbstractPlatformTransactionManager {
        private Object active;
        private int begun;
        private int committed;

        @Override
        protected Object doGetTransaction() {
            return active == null ? new Object() : active;
        }

        @Override
        protected boolean isExistingTransaction(Object transaction) {
            return active != null && active == transaction;
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            active = transaction;
            begun++;
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            committed++;
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
        }

        @Override
        protected void doCleanupAfterCompletion(Object transaction) {
            active = null;
        }
    }
}
