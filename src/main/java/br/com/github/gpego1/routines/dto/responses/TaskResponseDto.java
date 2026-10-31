package br.com.github.gpego1.routines.dto.responses;

import java.time.LocalDateTime;

public record TaskResponseDto(
        String name,
        String description,
        LocalDateTime startAt,
        LocalDateTime finishAt,
        Integer durationHours,
        Integer durationMinutes
) {
}
