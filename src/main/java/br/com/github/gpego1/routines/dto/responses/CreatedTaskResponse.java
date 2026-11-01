package br.com.github.gpego1.routines.dto.responses;

import java.time.LocalDateTime;

public record CreatedTaskResponse(
        Integer id,
        String name,
        String description,
        Boolean finished,
        Integer durationHours,
        Integer durationMinutes
) {
}
