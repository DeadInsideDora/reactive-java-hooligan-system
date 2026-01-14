package org.example.hooliguns.repository;

import org.example.hooliguns.domain.Alert;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface AlertRepository extends ReactiveMongoRepository<Alert, String> {
    Flux<Alert> findAllByOrderByCreatedAtDesc();
}
