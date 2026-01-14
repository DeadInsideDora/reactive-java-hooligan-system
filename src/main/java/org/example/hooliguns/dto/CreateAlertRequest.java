package org.example.hooliguns.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAlertRequest(
        @NotBlank String message
) {
}
