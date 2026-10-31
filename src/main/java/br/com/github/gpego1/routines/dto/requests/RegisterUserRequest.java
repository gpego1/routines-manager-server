package br.com.github.gpego1.routines.dto.requests;

public record RegisterUserRequest(
        String name,
        String email,
        String password
) {
}
