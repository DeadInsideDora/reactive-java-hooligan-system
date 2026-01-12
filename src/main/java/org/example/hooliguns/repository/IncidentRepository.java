package org.example.hooliguns.repository;

import java.util.UUID;
import org.example.hooliguns.domain.Incident;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface IncidentRepository extends ReactiveCrudRepository<Incident, UUID> {
}
