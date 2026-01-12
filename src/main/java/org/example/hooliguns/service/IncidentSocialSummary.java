package org.example.hooliguns.service;

public record IncidentSocialSummary(long likes, long dislikes, long comments) {
    public long reactionsTotal() {
        return likes + dislikes;
    }
}
