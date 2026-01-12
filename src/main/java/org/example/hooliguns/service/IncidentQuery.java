package org.example.hooliguns.service;

import java.time.Instant;
import org.example.hooliguns.domain.IncidentType;

public record IncidentQuery(
        String query,
        String faculty,
        String groupName,
        IncidentType type,
        Instant from,
        Instant to,
        IncidentSort sort
) {
}
