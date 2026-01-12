package org.example.hooliguns.dto;

import java.time.Instant;
import java.util.UUID;
import org.example.hooliguns.domain.IncidentType;
import org.example.hooliguns.domain.ModerationStatus;

public record IncidentResponse(
        UUID id,
        String title,
        String description,
        String place,
        IncidentType type,
        Instant occurredAt,
        Instant createdAt,
        UUID offenderId,
        UUID createdById,
        ModerationStatus moderationStatus,
        long likes,
        long dislikes,
        long comments
) {
}
