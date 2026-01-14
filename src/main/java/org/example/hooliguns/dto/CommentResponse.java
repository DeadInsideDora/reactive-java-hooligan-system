package org.example.hooliguns.dto;

import java.time.Instant;

public record CommentResponse(
        java.util.UUID id,
        String userId,
        String displayName,
        String text,
        Instant createdAt
) {
}
