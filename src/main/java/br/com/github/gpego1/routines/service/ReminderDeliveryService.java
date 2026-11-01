package br.com.github.gpego1.routines.service;
import br.com.github.gpego1.routines.database.repository.ITaskReminderRepository;
import br.com.github.gpego1.routines.enums.*;
import br.com.github.gpego1.routines.utils.TelegramClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderDeliveryService {
    private final ITaskReminderRepository reminders;
    private final TaskReminderService reminderService;
    private final TelegramClient telegram;
    private final br.com.github.gpego1.routines.database.repository.ITaskRepository tasks;
    // Lock held through delivery and commit; competing workers re-check status.
    @Transactional
    public void deliver(Integer id) {
        var taskId = reminders.findTaskIdByReminderId(id).orElse(null);
        if (taskId == null || tasks.lockById(taskId).isEmpty()) return;
        var r = reminders.lockById(id).orElse(null);
        Instant now = Instant.now();
        if (r == null || r.getStatus() != ReminderStatusTypeEnum.PENDING || r.getScheduledAt().isAfter(now)) return;
        var task = r.getTask();
        var user = task.getUser();
        Instant start = reminderService.startInstant(task);
        Instant expiresAt = r.getType() == ReminderTypeEnum.ON_START ? start.plusSeconds(60) : start;
        if (Boolean.TRUE.equals(task.getFinished()) || !now.isBefore(expiresAt)) {
            r.setStatus(ReminderStatusTypeEnum.CANCELLED);
            return;
        }
        if (!user.isTelegramEnabled() || user.getTelegramChatId() == null) {
            r.setScheduledAt(now.plusSeconds(60));
            return;
        }
        r.setStatus(ReminderStatusTypeEnum.PROCESSING);
        r.setAttempts((r.getAttempts() == null ? 0 : r.getAttempts()) + 1);
        String text = r.getType() == ReminderTypeEnum.BEFORE_START
                ? "Lembrete: sua tarefa começa em breve: " + task.getName()
                : "Sua tarefa está começando: " + task.getName();
        try {
            telegram.sendMessage(user.getTelegramChatId(), text);
            r.setSent(Instant.now());
            r.setStatus(ReminderStatusTypeEnum.SENT);
        } catch (RuntimeException failure) {
            r.setStatus(r.getAttempts() >= 3 ? ReminderStatusTypeEnum.FAILED : ReminderStatusTypeEnum.PENDING);
            r.setScheduledAt(now.plusSeconds(60L * r.getAttempts()));
            // Provider exceptions may include a URL containing the bot token.
            log.warn("Telegram reminder {} failed on attempt {}", id, r.getAttempts());
        }
    }
}
