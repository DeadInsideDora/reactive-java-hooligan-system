package org.example.hooliguns.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentReportResponse(
        String id,
        UUID incidentId,
        UUID commentId,
        UUID reporterId,
        String reporterDisplayName,
        UUID commentAuthorId,
        String commentAuthorDisplayName,
        String commentText,
        Instant commentCreatedAt,
        String reason,
        Instant createdAt
) {
}
