package org.example.hooliguns.dto;

import jakarta.validation.constraints.NotNull;
import org.example.hooliguns.domain.ModerationStatus;

public record ModerationRequest(@NotNull ModerationStatus status) {
}
