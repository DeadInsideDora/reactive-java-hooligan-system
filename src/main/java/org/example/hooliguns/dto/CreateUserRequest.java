package org.example.hooliguns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.hooliguns.domain.UserRole;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String displayName,
        @NotNull UserRole role,
        String faculty,
        String groupName
) {
}
