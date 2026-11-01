package br.com.github.gpego1.routines.dto.responses;

public record TokenResponseDto(
        String token,
        long expirationTime
) {
}
