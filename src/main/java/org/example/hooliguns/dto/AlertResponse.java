package org.example.hooliguns.dto;

import java.time.Instant;

public record AlertResponse(
        String id,
        String message,
        Instant createdAt,
        String createdById
) {
}
