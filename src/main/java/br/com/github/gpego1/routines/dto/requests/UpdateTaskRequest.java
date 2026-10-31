package br.com.github.gpego1.routines.dto.requests;

import br.com.github.gpego1.routines.enums.CategoryEnumType;

import java.time.LocalDateTime;

public record UpdateTaskRequest(
        String name,
        String description,
        CategoryEnumType category,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
) {
}
