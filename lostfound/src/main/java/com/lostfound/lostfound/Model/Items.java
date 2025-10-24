package com.lostfound.lostfound.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "Items")
public class Items {

    @Id
    private String id;
    private String userid;
    private String name;
    private String location;
    private String type; // LOST / FOUND / PROOF
    private String description;
    private String status; // e.g., Pending, Found
    private String phone;
    private String address;
    private String email;

    // Main image URL stored in Supabase
    private String imageUrl;

    // Optional list of proof URLs (for multiple files)
    private List<String> proofUrls;

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
