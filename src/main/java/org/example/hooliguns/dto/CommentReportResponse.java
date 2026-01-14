package org.example.hooliguns.dto;

import java.time.Instant;

public record CommentReportResponse(
        String id,
        java.util.UUID incidentId,
        java.util.UUID commentId,
        String reporterId,
        String reporterDisplayName,
        String commentAuthorId,
        String commentAuthorDisplayName,
        String commentText,
        Instant commentCreatedAt,
        String reason,
        Instant createdAt
) {
}
