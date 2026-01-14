package org.example.hooliguns.dto;

import java.util.UUID;

public record BoardEntryResponse(
        UUID userId,
        String displayName,
        String faculty,
        String groupName,
        double points,
        long incidentsCount
) {
}
