package org.example.hooliguns.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("incident_social")
public class IncidentSocial {
    @Id
    private String id;
    private UUID incidentId;
    private List<IncidentReaction> reactions = new ArrayList<>();
    private List<IncidentComment> comments = new ArrayList<>();
    private Instant updatedAt;
}
