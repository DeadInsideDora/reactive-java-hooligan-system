package org.example.hooliguns.service;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import org.example.hooliguns.dto.BoardEntryResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class BoardService {
    private final IncidentService incidentService;
    private final UserService userService;
    private final IncidentSocialService incidentSocialService;

    public BoardService(IncidentService incidentService,
                        UserService userService,
                        IncidentSocialService incidentSocialService) {
        this.incidentService = incidentService;
        this.userService = userService;
        this.incidentSocialService = incidentSocialService;
    }

    public Flux<BoardEntryResponse> board(int limit) {
        return incidentService.findAllIncidents()
                .flatMap(incident -> incidentSocialService.summary(incident.getId())
                        .map(summary -> Map.entry(
                                incident.getOffenderId(),
                                incident.getType().getBasePoints()
                                        + (summary.likes() - summary.dislikes()) * 0.5
                        )))
                .collectMultimap(Map.Entry::getKey, Map.Entry::getValue)
                .flatMapMany(map -> Flux.fromIterable(map.entrySet()))
                .flatMap(entry -> userService.findById(entry.getKey())
                        .map(user -> new BoardEntryResponse(
                                user.getId(),
                                user.getDisplayName(),
                                user.getFaculty(),
                                user.getGroupName(),
                                entry.getValue().stream().mapToDouble(Double::doubleValue).sum(),
                                entry.getValue().size()
                        )))
                .sort(Comparator.comparingDouble(BoardEntryResponse::points).reversed())
                .take(limit);
    }
}
