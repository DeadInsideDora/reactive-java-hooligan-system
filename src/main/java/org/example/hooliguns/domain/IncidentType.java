package org.example.hooliguns.domain;

public enum IncidentType {
    DISRUPTION(3),
    DAMAGE(6),
    CHEATING(4),
    AGGRESSION(8),
    OTHER(2);

    private final int basePoints;

    IncidentType(int basePoints) {
        this.basePoints = basePoints;
    }

    public int getBasePoints() {
        return basePoints;
    }
}
