package br.com.github.gpego1.routines.dto.requests;

import br.com.github.gpego1.routines.enums.RoleEnumType;

public record RegisterUserRequest(
        String name,
        String email,
        String password,
        RoleEnumType role
) {
}
