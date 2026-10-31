package br.com.github.gpego1.routines.dto.requests;

import java.time.LocalDateTime;

public record CreateTaskRequest(
        String name,
        String description,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
) {
}
