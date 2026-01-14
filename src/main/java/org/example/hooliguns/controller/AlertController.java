package org.example.hooliguns.controller;

import jakarta.validation.Valid;
import org.example.hooliguns.dto.AlertResponse;
import org.example.hooliguns.dto.CreateAlertRequest;
import org.example.hooliguns.security.AuthenticatedUser;
import org.example.hooliguns.service.AlertService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    @PreAuthorize("hasRole('IMMORTAL')")
    public Mono<AlertResponse> create(@Valid @RequestBody CreateAlertRequest request,
                                      @AuthenticationPrincipal AuthenticatedUser user) {
        return alertService.create(request, user.getId().toString());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT','ADMIN','IMMORTAL')")
    public Flux<AlertResponse> list() {
        return alertService.list();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT','ADMIN','IMMORTAL')")
    public Flux<ServerSentEvent<AlertResponse>> stream() {
        return alertService.stream()
                .map(alert -> ServerSentEvent.builder(alert).event("alert").build());
    }
}
