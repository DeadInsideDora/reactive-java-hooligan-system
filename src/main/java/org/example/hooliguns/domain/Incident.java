package org.example.hooliguns.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("incidents")
public class Incident implements Persistable<UUID> {
    @Id
    private UUID id;
    private String title;
    private String description;
    private String place;
    private IncidentType type;
    @Column("occurred_at")
    private Instant occurredAt;
    @Column("created_at")
    private Instant createdAt;
    @Column("offender_id")
    private UUID offenderId;
    @Column("created_by_id")
    private UUID createdById;
    @Column("moderation_status")
    private ModerationStatus moderationStatus;
    @Transient
    private boolean isNew;

    @Override
    public boolean isNew() {
        return isNew;
    }
}
