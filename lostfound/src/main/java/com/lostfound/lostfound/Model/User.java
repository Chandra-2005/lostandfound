package com.lostfound.lostfound.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user")

public class User {

    @Id
    private String id;
    private String username;
    private String password;
    private String email;
    private LocalDateTime createdAt;

    // Auto-generate ID and timestamp if not provided
    public void init() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

}
