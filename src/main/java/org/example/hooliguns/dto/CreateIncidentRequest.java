package org.example.hooliguns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import org.example.hooliguns.domain.IncidentType;

public record CreateIncidentRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String place,
        @NotNull IncidentType type,
        @NotNull Instant occurredAt,
        @NotNull UUID offenderId
) {
}
