package org.example.hooliguns.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReportCommentRequest(
        @NotNull UUID incidentId,
        @NotNull UUID commentId,
        String reason
) {
}
