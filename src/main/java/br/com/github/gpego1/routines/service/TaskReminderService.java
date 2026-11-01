package br.com.github.gpego1.routines.service;

import br.com.github.gpego1.routines.config.TelegramProperties;
import br.com.github.gpego1.routines.database.model.*;
import br.com.github.gpego1.routines.database.repository.*;
import br.com.github.gpego1.routines.dto.requests.CreateTaskReminderRequest;
import br.com.github.gpego1.routines.dto.responses.TaskReminderDto;
import br.com.github.gpego1.routines.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

@Service
@RequiredArgsConstructor
public class TaskReminderService {
    private final ITaskReminderRepository reminders;
    private final ITaskRepository tasks;
    private final TelegramProperties properties;

    public Instant startInstant(TaskEntity task) {
        String timezone = task.getUser().getTimezone();
        ZoneId zone = timezone == null || timezone.isBlank() ? properties.getDefaultTimezone() : ZoneId.of(timezone);
        return task.getStartDateTime().atZone(zone).toInstant();
    }
    private Instant scheduledAt(TaskEntity task, ReminderTypeEnum type) {
        Instant start = startInstant(task);
        return type == ReminderTypeEnum.BEFORE_START ? start.minusSeconds(900) : start;
    }
    @Transactional
    public TaskReminderDto createTaskReminder(CreateTaskReminderRequest dto) {
        TaskEntity task = tasks.findById(dto.taskId()).orElseThrow(() -> new IllegalArgumentException("Task not found"));
        ReminderTypeEnum type = dto.type() == null ? ReminderTypeEnum.BEFORE_START : dto.type();
        if (reminders.findByTaskId(task.getId()).stream().anyMatch(r -> r.getType() == type))
            throw new IllegalArgumentException("Reminder already exists for this task and type");
        TaskReminderEntity r = reminders.save(newReminder(task, type));
        return new TaskReminderDto(r.getId(), task.getId(), r.getType(), r.getStatus(), r.getScheduledAt(), r.getSent(), r.getAttempts());
    }
    private TaskReminderEntity newReminder(TaskEntity task, ReminderTypeEnum type) {
        return TaskReminderEntity.builder().task(task).type(type).status(ReminderStatusTypeEnum.PENDING)
                .attempts(0).scheduledAt(scheduledAt(task, type)).build();
    }
    @Transactional
    public void schedule(TaskEntity task) {
        var existing = reminders.findByTaskId(task.getId());
        if (existing.isEmpty()) {
            reminders.save(newReminder(task, ReminderTypeEnum.BEFORE_START));
            return;
        }
        for (var r : existing) {
            if (r.getStatus() != ReminderStatusTypeEnum.SENT) {
                r.setScheduledAt(scheduledAt(task, r.getType()));
                r.setStatus(ReminderStatusTypeEnum.PENDING);
                r.setAttempts(0);
                r.setSent(null);
            }
        }
    }
    @Transactional
    public void cancel(Integer taskId) {
        for (var r : reminders.findByTaskId(taskId)) {
            if (r.getStatus() != ReminderStatusTypeEnum.SENT) r.setStatus(ReminderStatusTypeEnum.CANCELLED);
        }
    }
    @Transactional
    public void delete(Integer taskId) { reminders.deleteByTaskId(taskId); }
}
