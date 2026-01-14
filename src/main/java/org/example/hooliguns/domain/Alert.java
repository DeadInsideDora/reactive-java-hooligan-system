package org.example.hooliguns.domain;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("alerts")
public class Alert {
    @Id
    private String id;
    private String message;
    @Indexed(expireAfterSeconds = 86400)
    private Instant createdAt;
    private String createdById;
}
