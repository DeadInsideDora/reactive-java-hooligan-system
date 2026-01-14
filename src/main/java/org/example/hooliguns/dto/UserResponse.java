package org.example.hooliguns.dto;

import java.time.Instant;
import java.util.UUID;
import org.example.hooliguns.domain.UserRole;

public record UserResponse(
        UUID id,
        String username,
        String isu,
        String displayName,
        UserRole role,
        String faculty,
        String groupName,
        Instant createdAt
) {
}
