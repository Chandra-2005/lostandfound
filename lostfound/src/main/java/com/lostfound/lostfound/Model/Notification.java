package com.lostfound.lostfound.Model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String userId;       // Who receives the notification
    private String message;      // Notification text
    private boolean read;        // Has user seen it
    private LocalDateTime createdAt;

    public void init() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }
}
