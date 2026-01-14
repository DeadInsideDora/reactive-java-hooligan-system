package org.example.hooliguns.dto;

import jakarta.validation.constraints.NotNull;
import org.example.hooliguns.domain.UserRole;

public record UpdateUserRoleRequest(@NotNull UserRole role) {
}
