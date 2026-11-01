package br.com.github.gpego1.routines.service;
import br.com.github.gpego1.routines.config.TelegramProperties;
import br.com.github.gpego1.routines.database.repository.ITaskReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "telegram.scheduler-enabled", havingValue = "true", matchIfMissing = true)
public class TaskReminderScheduler {
    private final ITaskReminderRepository reminders;
    private final ReminderDeliveryService delivery;
    private final TelegramProperties properties;
    @Scheduled(fixedDelayString = "${telegram.scheduler-delay-ms:30000}")
    public void sendDueReminders() {
        if (properties.getBotToken().isBlank()) return;
        for (Integer id : reminders.findDueIds(Instant.now(), PageRequest.of(0, 50))) {
            try { delivery.deliver(id); }
            catch (RuntimeException failure) { log.warn("Could not process reminder {}", id); }
        }
    }
}
