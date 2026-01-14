package org.example.hooliguns.service;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;
import org.example.hooliguns.domain.UserAccount;
import org.example.hooliguns.domain.UserRole;
import org.example.hooliguns.dto.CreateUserRequest;
import org.example.hooliguns.dto.RegisterUserRequest;
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
    private static final Pattern ISU_PATTERN = Pattern.compile("^s\\d{6}$");

    public UserService(UserAccountRepository userAccountRepository,
                       PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<UserResponse> createUser(CreateUserRequest request) {
        String isu = normalizeIsu(request.username());
        validateIsu(isu);
        return userAccountRepository.findByIsu(isu)
                .flatMap(existing -> Mono.<UserResponse>error(new IllegalArgumentException("ISU already exists")))
                .switchIfEmpty(Mono.defer(() -> userAccountRepository.save(new UserAccount(
                        UUID.randomUUID(),
                        isu,
                        isu,
                        passwordEncoder.encode(request.password()),
                        request.displayName(),
                        request.role(),
                        request.faculty(),
                        request.groupName(),
                        Instant.now(),
                        true
                )).map(this::toResponse)));
    }

    public Mono<UserResponse> registerUser(RegisterUserRequest request) {
        String isu = normalizeIsu(request.username());
        validateIsu(isu);
        return userAccountRepository.findByIsu(isu)
                .flatMap(existing -> Mono.<UserResponse>error(new IllegalArgumentException("ISU already exists")))
                .switchIfEmpty(Mono.defer(() -> userAccountRepository.save(new UserAccount(
                        UUID.randomUUID(),
                        isu,
                        isu,
                        passwordEncoder.encode(request.password()),
                        request.displayName(),
                        UserRole.STUDENT,
                        request.faculty(),
                        request.groupName(),
                        Instant.now(),
                        true
                )).map(this::toResponse)));
    }

    public Flux<UserResponse> listUsers() {
        return userAccountRepository.findAll()
                .map(this::toResponse);
    }

    public Mono<UserAccount> findByIsu(String isu) {
        String normalized = normalizeIsu(isu);
        return userAccountRepository.findByIsu(normalized);
    }

    public Mono<UserAccount> findById(UUID id) {
        return userAccountRepository.findById(id);
    }

    public Mono<UserResponse> getUser(UUID id) {
        return findById(id).map(this::toResponse);
    }

    public Mono<UserResponse> updateUserRole(UUID id, UserRole role) {
        return userAccountRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found")))
                .flatMap(user -> {
                    user.setRole(role);
                    return userAccountRepository.save(user);
                })
                .map(this::toResponse);
    }

    private UserResponse toResponse(UserAccount account) {
        return new UserResponse(
                account.getId(),
                account.getUsername(),
                account.getIsu(),
                account.getDisplayName(),
                account.getRole(),
                account.getFaculty(),
                account.getGroupName(),
                account.getCreatedAt()
        );
    }

    private String normalizeIsu(String username) {
        return username == null ? null : username.trim().toLowerCase();
    }

    private void validateIsu(String isu) {
        if (isu == null || !ISU_PATTERN.matcher(isu).matches()) {
            throw new IllegalArgumentException("ISU must match sXXXXXX");
        }
    }
}
