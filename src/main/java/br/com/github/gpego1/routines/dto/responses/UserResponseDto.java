package br.com.github.gpego1.routines.dto.responses;

import jakarta.validation.constraints.NotBlank;


public record UserResponseDto (
        @NotBlank String name,
        @NotBlank String email
){
}
