package org.example.hooliguns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import org.example.hooliguns.domain.IncidentType;
import org.example.hooliguns.domain.Punishment;

public record CreateIncidentRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String place,
        String department,
        @NotNull IncidentType type,
        @NotNull Instant occurredAt,
        @NotNull
        @Pattern(regexp = "^s\\d{6}$", message = "ISU must match sXXXXXX")
        String offenderId,
        @NotNull Punishment punishment
) {
}
