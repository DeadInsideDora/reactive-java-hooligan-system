package org.example.hooliguns.config;

import java.time.Instant;
import java.util.UUID;
import org.example.hooliguns.domain.UserAccount;
import org.example.hooliguns.domain.UserRole;
import org.example.hooliguns.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapConfig {
    @Bean
    public ApplicationRunner bootstrapAdmin(UserAccountRepository userAccountRepository,
                                            PasswordEncoder passwordEncoder,
                                            @Value("${hooliguns.admin.username:admin}") String username,
                                            @Value("${hooliguns.admin.password:admin}") String password) {
        return args -> userAccountRepository.count()
                .filter(count -> count == 0)
                .flatMap(count -> userAccountRepository.save(new UserAccount(
                        UUID.randomUUID(),
                        username,
                        passwordEncoder.encode(password),
                        "Administrator",
                        UserRole.ADMIN,
                        null,
                        null,
                        Instant.now(),
                        true
                )))
                .subscribe();
    }
}
