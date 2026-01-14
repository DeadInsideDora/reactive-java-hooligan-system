package org.example.hooliguns.controller;

import jakarta.validation.Valid;
import org.example.hooliguns.dto.CreateUserRequest;
import org.example.hooliguns.dto.RegisterUserRequest;
import org.example.hooliguns.dto.UpdateUserRoleRequest;
import org.example.hooliguns.dto.UserResponse;
import org.example.hooliguns.security.AuthenticatedUser;
import org.example.hooliguns.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping("/register")
    public Mono<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','IMMORTAL')")
    public Mono<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('ADMIN','IMMORTAL')")
    public Mono<UserResponse> updateRole(@PathVariable String id,
                                         @Valid @RequestBody UpdateUserRoleRequest request) {
        return userService.updateUserRole(id, request.role());
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT','ADMIN','IMMORTAL')")
    public Mono<UserResponse> me(@AuthenticationPrincipal AuthenticatedUser user) {
        return userService.getUser(user.getId());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','IMMORTAL')")
    public Flux<UserResponse> list() {
        return userService.listUsers();
    }
}
