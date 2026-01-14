package org.example.hooliguns.repository;

import java.util.UUID;
import org.example.hooliguns.domain.Incident;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface IncidentRepository extends ReactiveCrudRepository<Incident, UUID> {
    Flux<Incident> findAllByOffenderId(String offenderId);
}
