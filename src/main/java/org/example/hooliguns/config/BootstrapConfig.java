package org.example.hooliguns.config;

import java.time.Instant;
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
        return args -> userAccountRepository.findByUsername(username)
                .flatMap(existing -> {
                    existing.setPassword(passwordEncoder.encode(password));
                    existing.setRole(UserRole.ADMIN);
                    existing.setDisplayName("Administrator");
                    return userAccountRepository.save(existing);
                })
                .switchIfEmpty(userAccountRepository.save(new UserAccount(
                        username,
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

    @Bean
    public ApplicationRunner bootstrapImmortal(UserAccountRepository userAccountRepository,
                                               PasswordEncoder passwordEncoder,
                                               @Value("${hooliguns.immortal.username:s999999}") String username,
                                               @Value("${hooliguns.immortal.password:immortal}") String password) {
        return args -> userAccountRepository.findByUsername(username)
                .flatMap(existing -> {
                    existing.setPassword(passwordEncoder.encode(password));
                    existing.setRole(UserRole.IMMORTAL);
                    existing.setDisplayName("Immortal");
                    return userAccountRepository.save(existing);
                })
                .switchIfEmpty(userAccountRepository.save(new UserAccount(
                        username,
                        username,
                        passwordEncoder.encode(password),
                        "Immortal",
                        UserRole.IMMORTAL,
                        null,
                        null,
                        Instant.now(),
                        true
                )))
                .subscribe();
    }
}
