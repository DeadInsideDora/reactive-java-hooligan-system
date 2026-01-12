package org.example.hooliguns.controller;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.example.hooliguns.domain.IncidentType;
import org.example.hooliguns.dto.CommentRequest;
import org.example.hooliguns.dto.CreateIncidentRequest;
import org.example.hooliguns.dto.IncidentResponse;
import org.example.hooliguns.dto.ModerationRequest;
import org.example.hooliguns.dto.ReactionRequest;
import org.example.hooliguns.security.AuthenticatedUser;
import org.example.hooliguns.service.IncidentQuery;
import org.example.hooliguns.service.IncidentService;
import org.example.hooliguns.service.IncidentSort;
import org.example.hooliguns.service.IncidentSocialService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentService incidentService;
    private final IncidentSocialService incidentSocialService;

    public IncidentController(IncidentService incidentService,
                              IncidentSocialService incidentSocialService) {
        this.incidentService = incidentService;
        this.incidentSocialService = incidentSocialService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public Mono<IncidentResponse> create(@Valid @RequestBody CreateIncidentRequest request,
                                         @AuthenticationPrincipal AuthenticatedUser user) {
        return incidentService.create(request, user.getId());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT','ADMIN')")
    public Flux<IncidentResponse> list(@RequestParam(required = false) String q,
                                       @RequestParam(required = false) String faculty,
                                       @RequestParam(required = false) String group,
                                       @RequestParam(required = false) IncidentType type,
                                       @RequestParam(required = false)
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                       @RequestParam(required = false)
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                       @RequestParam(required = false, defaultValue = "DATE_DESC") IncidentSort sort) {
        IncidentQuery query = new IncidentQuery(q, faculty, group, type, from, to, sort);
        return incidentService.list(query);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT','ADMIN')")
    public Mono<IncidentResponse> get(@PathVariable UUID id) {
        return incidentService.get(id);
    }

    @PatchMapping("/{id}/moderation")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<IncidentResponse> moderate(@PathVariable UUID id,
                                           @Valid @RequestBody ModerationRequest request) {
        return incidentService.updateModeration(id, request.status());
    }

    @PostMapping("/{id}/reactions")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT')")
    public Mono<Void> react(@PathVariable UUID id,
                            @Valid @RequestBody ReactionRequest request,
                            @AuthenticationPrincipal AuthenticatedUser user) {
        return incidentSocialService.addReaction(id, user.getId(), request.type()).then();
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT')")
    public Mono<Void> comment(@PathVariable UUID id,
                              @Valid @RequestBody CommentRequest request,
                              @AuthenticationPrincipal AuthenticatedUser user) {
        return incidentSocialService.addComment(id, user.getId(), request.text()).then();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT','ADMIN')")
    public Flux<ServerSentEvent<IncidentResponse>> stream() {
        return incidentService.stream()
                .map(response -> ServerSentEvent.builder(response)
                        .event("incident")
                        .build());
    }
}
