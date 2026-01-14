package org.example.hooliguns.dto;

public record BoardEntryResponse(
        String userId,
        String displayName,
        String faculty,
        String groupName,
        double points,
        long incidentsCount
) {
}
