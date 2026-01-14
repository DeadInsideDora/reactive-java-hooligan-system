package org.example.hooliguns.dto;

import java.time.Instant;
import org.example.hooliguns.domain.IncidentType;
import org.example.hooliguns.domain.ModerationStatus;
import org.example.hooliguns.domain.Punishment;

public record IncidentResponse(
        java.util.UUID id,
        String title,
        String description,
        String place,
        String department,
        IncidentType type,
        Instant occurredAt,
        Instant createdAt,
        String offenderId,
        String createdById,
        ModerationStatus moderationStatus,
        Punishment punishment,
        long likes,
        long dislikes,
        long comments
) {
}
