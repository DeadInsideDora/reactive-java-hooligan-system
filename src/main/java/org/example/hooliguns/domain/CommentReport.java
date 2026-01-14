package org.example.hooliguns.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("comment_reports")
public class CommentReport {
    @Id
    private String id;
    private UUID incidentId;
    private UUID commentId;
    private String reporterId;
    private String commentAuthorId;
    private String commentText;
    private Instant commentCreatedAt;
    private String reason;
    private Instant createdAt;
}
