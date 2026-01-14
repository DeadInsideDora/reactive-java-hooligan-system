package org.example.hooliguns.security;

import java.util.List;
import org.example.hooliguns.domain.UserAccount;
import org.example.hooliguns.repository.UserAccountRepository;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public ReactiveUserDetailsServiceImpl(UserAccountRepository userAccountRepository,
                                          PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        String normalized = username == null ? null : username.trim().toLowerCase();
        return userAccountRepository.findByIsu(normalized)
                .switchIfEmpty(userAccountRepository.findByUsername(normalized))
                .map(this::toUserDetails);
    }

    public ReactiveAuthenticationManager authenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager manager =
                new UserDetailsRepositoryReactiveAuthenticationManager(this);
        manager.setPasswordEncoder(passwordEncoder);
        return manager;
    }

    private UserDetails toUserDetails(UserAccount account) {
        return new AuthenticatedUser(
                account.getId(),
                account.getUsername(),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()))
        );
    }
}
