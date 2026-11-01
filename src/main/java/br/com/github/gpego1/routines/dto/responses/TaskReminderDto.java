package br.com.github.gpego1.routines.dto.responses;

import br.com.github.gpego1.routines.database.model.TaskEntity;
import br.com.github.gpego1.routines.enums.ReminderStatusTypeEnum;
import br.com.github.gpego1.routines.enums.ReminderTypeEnum;

import java.time.Instant;

public record TaskReminderDto(
        Integer id,
        Integer taskId,
        ReminderTypeEnum type,
        ReminderStatusTypeEnum status,
        Instant scheduledAt,
        Instant sent,
        Integer attempts
) {
}
