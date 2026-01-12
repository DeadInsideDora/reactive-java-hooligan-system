package org.example.hooliguns.dto;

import java.util.List;

public record HooliganCardResponse(
        UserResponse user,
        List<IncidentResponse> incidents,
        long likes,
        long dislikes,
        double points
) {
}
