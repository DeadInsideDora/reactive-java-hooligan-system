package org.example.hooliguns.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class UserAccount {
    @Id
    private UUID id;
    private String username;
    private String password;
    @Column("display_name")
    private String displayName;
    private UserRole role;
    private String faculty;
    @Column("group_name")
    private String groupName;
    @Column("created_at")
    private Instant createdAt;
}
