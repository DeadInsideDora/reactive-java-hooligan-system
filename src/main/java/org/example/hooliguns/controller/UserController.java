package org.example.hooliguns.controller;

import jakarta.validation.Valid;
import org.example.hooliguns.dto.CreateUserRequest;
import org.example.hooliguns.dto.UserResponse;
import org.example.hooliguns.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<UserResponse> list() {
        return userService.listUsers();
    }
}
