package org.example.hooliguns.service;

import java.util.List;
import java.util.UUID;
import org.example.hooliguns.dto.HooliganCardResponse;
import org.example.hooliguns.dto.IncidentResponse;
import org.example.hooliguns.dto.UserResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class HooliganService {
    private final UserService userService;
    private final IncidentService incidentService;

    public HooliganService(UserService userService,
                           IncidentService incidentService) {
        this.userService = userService;
        this.incidentService = incidentService;
    }

    public Mono<HooliganCardResponse> card(UUID userId) {
        Mono<UserResponse> user = userService.getUser(userId);
        Mono<List<IncidentResponse>> incidents = incidentService.findAllIncidents()
                .filter(incident -> incident.getOffenderId().equals(userId))
                .flatMap(incidentService::toResponse)
                .collectList();

        return Mono.zip(user, incidents)
                .map(tuple -> buildCard(tuple.getT1(), tuple.getT2()));
    }

    private HooliganCardResponse buildCard(UserResponse user, List<IncidentResponse> incidents) {
        long likes = incidents.stream().mapToLong(IncidentResponse::likes).sum();
        long dislikes = incidents.stream().mapToLong(IncidentResponse::dislikes).sum();
        double points = incidents.stream()
                .mapToDouble(incident -> incident.type().getBasePoints()
                        + (incident.likes() - incident.dislikes()) * 0.5)
                .sum();
        return new HooliganCardResponse(user, incidents, likes, dislikes, points);
    }
}
