package org.example.hooliguns.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID userId,
        String displayName,
        String text,
        Instant createdAt
) {
}
