package org.example.hooliguns.repository;

import java.util.UUID;
import org.example.hooliguns.domain.UserAccount;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserAccountRepository extends ReactiveCrudRepository<UserAccount, UUID> {
    Mono<UserAccount> findByUsername(String username);
    Mono<UserAccount> findByIsu(String isu);
}
