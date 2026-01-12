package org.example.hooliguns.repository;

import java.util.UUID;
import org.example.hooliguns.domain.IncidentSocial;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface IncidentSocialRepository extends ReactiveMongoRepository<IncidentSocial, String> {
    Mono<IncidentSocial> findByIncidentId(UUID incidentId);
}
