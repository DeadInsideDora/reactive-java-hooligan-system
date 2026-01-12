package org.example.hooliguns.service;

import java.time.Instant;
import java.util.UUID;
import org.example.hooliguns.domain.UserAccount;
import org.example.hooliguns.dto.CreateUserRequest;
import org.example.hooliguns.dto.UserResponse;
import org.example.hooliguns.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserAccountRepository userAccountRepository,
                       PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<UserResponse> createUser(CreateUserRequest request) {
        return userAccountRepository.findByUsername(request.username())
                .flatMap(existing -> Mono.error(new IllegalArgumentException("Username already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    UserAccount account = new UserAccount(
                            UUID.randomUUID(),
                            request.username(),
                            passwordEncoder.encode(request.password()),
                            request.displayName(),
                            request.role(),
                            request.faculty(),
                            request.groupName(),
                            Instant.now()
                    );
                    return userAccountRepository.save(account).map(this::toResponse);
                }));
    }

    public Flux<UserResponse> listUsers() {
        return userAccountRepository.findAll()
                .map(this::toResponse);
    }

    public Mono<UserAccount> findById(UUID id) {
        return userAccountRepository.findById(id);
    }

    public Mono<UserResponse> getUser(UUID id) {
        return findById(id).map(this::toResponse);
    }

    private UserResponse toResponse(UserAccount account) {
        return new UserResponse(
                account.getId(),
                account.getUsername(),
                account.getDisplayName(),
                account.getRole(),
                account.getFaculty(),
                account.getGroupName(),
                account.getCreatedAt()
        );
    }
}
