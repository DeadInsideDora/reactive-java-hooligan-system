package org.example.hooliguns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterUserRequest(
        @NotBlank
        @Pattern(regexp = "^s\\d{6}$", message = "ISU must match sXXXXXX")
        String username,
        @NotBlank String password,
        @NotBlank String displayName,
        String faculty,
        String groupName
) {
}
