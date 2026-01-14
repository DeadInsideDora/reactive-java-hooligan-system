package org.example.hooliguns.domain;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentReaction {
    private String userId;
    private ReactionType type;
    private Instant reactedAt;
}
