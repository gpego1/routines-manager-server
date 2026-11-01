package br.com.github.gpego1.routines.dto.responses;

import br.com.github.gpego1.routines.database.model.RolesEntity;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;


public record UserResponseDto (
        Integer id,
        @NotBlank String name,
        @NotBlank String email,
        Set<RolesEntity> roles
){
}
