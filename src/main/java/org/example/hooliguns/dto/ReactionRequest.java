package org.example.hooliguns.dto;

import jakarta.validation.constraints.NotNull;
import org.example.hooliguns.domain.ReactionType;

public record ReactionRequest(@NotNull ReactionType type) {
}
