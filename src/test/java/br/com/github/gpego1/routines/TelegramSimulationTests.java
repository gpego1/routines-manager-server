package br.com.github.gpego1.routines;

import br.com.github.gpego1.routines.database.model.*;
import br.com.github.gpego1.routines.database.repository.*;
import br.com.github.gpego1.routines.enums.*;
import br.com.github.gpego1.routines.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "telegram.api-base-url=http://localhost:4010/telegram")
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "telegram.simulation", matches = "true")
class TelegramSimulationTests {
    @Autowired IUserRepository users;
    @Autowired ITaskRepository tasks;
    @Autowired ITaskReminderRepository reminders;
    @Autowired TaskReminderService scheduler;
    @Autowired ReminderDeliveryService delivery;
    @Autowired @org.springframework.beans.factory.annotation.Qualifier("telegramRestClient")
    org.springframework.web.client.RestClient telegramClient;

    @Test void registersWebhookWithSecretAndRejectsNonHttpsUrl() {
        var config = new br.com.github.gpego1.routines.config.TelegramProperties();
        config.setBotToken("test-token");
        config.setBotUsername("routines_test_bot");
        config.setWebhookSecret("test-secret");
        config.setWebhookUrl("https://routines.example/v1/telegram/webhook");
        var registration = new br.com.github.gpego1.routines.config.TelegramWebhookRegistration(config, telegramClient);
        assertDoesNotThrow(() -> registration.run(null));
        config.setWebhookUrl("http://routines.example/v1/telegram/webhook");
        assertThrows(IllegalStateException.class, () -> registration.run(null));
    }

    private TaskReminderEntity reminder(long chatId, boolean enabled) {
        var user = users.save(UserEntity.builder().name("Simulation").email(UUID.randomUUID() + "@test.local")
                .password("unused").roles(Set.of()).telegramChatId(chatId).telegramEnabled(enabled)
                .timezone("UTC").build());
        var task = tasks.save(TaskEntity.builder().user(user).name("Simulation task").finished(false)
                .startDateTime(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(10))
                .endDateTime(LocalDateTime.now(ZoneOffset.UTC).plusHours(1)).build());
        scheduler.schedule(task);
        return reminders.findByTaskId(task.getId()).getFirst();
    }
    @Test void sendsAndDoesNotSendAgainIncludingConcurrentWorkers() throws Exception {
        var r = reminder(12345, true);
        try (var workers = Executors.newFixedThreadPool(2)) {
            var futures = workers.invokeAll(List.<Callable<Void>>of(
                    () -> { delivery.deliver(r.getId()); return null; },
                    () -> { delivery.deliver(r.getId()); return null; }));
            for (var result : futures) result.get();
        }
        delivery.deliver(r.getId());
        var sent = reminders.findById(r.getId()).orElseThrow();
        assertEquals(ReminderStatusTypeEnum.SENT, sent.getStatus());
        assertNotNull(sent.getSent());
        assertEquals(1, sent.getAttempts());
    }
    @Test void providerFailureRetriesThenFailsAfterThreeAttempts() {
        var r = reminder(500, true);
        for (int attempt = 1; attempt <= 3; attempt++) {
            delivery.deliver(r.getId());
            var failed = reminders.findById(r.getId()).orElseThrow();
            assertEquals(attempt, failed.getAttempts());
            assertNull(failed.getSent());
            assertEquals(attempt == 3 ? ReminderStatusTypeEnum.FAILED : ReminderStatusTypeEnum.PENDING, failed.getStatus());
            assertTrue(failed.getScheduledAt().isAfter(Instant.now()));
            if (attempt < 3) {
                failed.setScheduledAt(Instant.now().minusSeconds(1));
                reminders.save(failed);
            }
        }
    }
    @Test void okFalseIsNotRecordedAsSent() {
        var r = reminder(400, true);
        delivery.deliver(r.getId());
        assertEquals(ReminderStatusTypeEnum.PENDING, reminders.findById(r.getId()).orElseThrow().getStatus());
        assertNull(reminders.findById(r.getId()).orElseThrow().getSent());
    }
    @Test void disabledUserDoesNotConsumeAttemptAndExpiredReminderIsCancelled() {
        var r = reminder(12345, false);
        delivery.deliver(r.getId());
        var pending = reminders.findById(r.getId()).orElseThrow();
        assertEquals(0, pending.getAttempts());
        assertEquals(ReminderStatusTypeEnum.PENDING, pending.getStatus());
        var task = tasks.findById(r.getTask().getId()).orElseThrow();
        task.setStartDateTime(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1));
        tasks.save(task);
        pending.setScheduledAt(Instant.now().minusSeconds(1));
        reminders.save(pending);
        delivery.deliver(r.getId());
        assertEquals(ReminderStatusTypeEnum.CANCELLED, reminders.findById(r.getId()).orElseThrow().getStatus());
    }
}
