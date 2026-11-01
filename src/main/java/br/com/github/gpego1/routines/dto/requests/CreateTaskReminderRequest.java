package br.com.github.gpego1.routines.dto.requests;

import br.com.github.gpego1.routines.enums.ReminderStatusTypeEnum;
import br.com.github.gpego1.routines.enums.ReminderTypeEnum;

import java.time.Instant;

public record CreateTaskReminderRequest(
        Integer taskId,
        ReminderTypeEnum type,
        Instant scheduledAt,
        ReminderStatusTypeEnum status
) {
}
