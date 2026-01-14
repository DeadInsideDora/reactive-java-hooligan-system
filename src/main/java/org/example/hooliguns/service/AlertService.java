package org.example.hooliguns.service;

import java.time.Instant;
import org.example.hooliguns.domain.Alert;
import org.example.hooliguns.dto.AlertResponse;
import org.example.hooliguns.dto.CreateAlertRequest;
import org.example.hooliguns.repository.AlertRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
public class AlertService {
    private final AlertRepository alertRepository;
    private final Sinks.Many<AlertResponse> sink;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    public Mono<AlertResponse> create(CreateAlertRequest request, String createdById) {
        Alert alert = new Alert();
        alert.setMessage(request.message());
        alert.setCreatedAt(Instant.now());
        alert.setCreatedById(createdById);
        return alertRepository.save(alert)
                .map(this::toResponse)
                .doOnNext(sink::tryEmitNext);
    }

    public Flux<AlertResponse> list() {
        return alertRepository.findAllByOrderByCreatedAtDesc()
                .map(this::toResponse);
    }

    public Flux<AlertResponse> stream() {
        return sink.asFlux();
    }

    private AlertResponse toResponse(Alert alert) {
        return new AlertResponse(alert.getId(), alert.getMessage(), alert.getCreatedAt(), alert.getCreatedById());
    }
}
