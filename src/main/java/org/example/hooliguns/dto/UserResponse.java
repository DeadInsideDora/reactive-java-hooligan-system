package org.example.hooliguns.dto;

import java.time.Instant;
import org.example.hooliguns.domain.UserRole;

public record UserResponse(
        String id,
        String username,
        String displayName,
        UserRole role,
        String faculty,
        String groupName,
        Instant createdAt
) {
}
