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

    private String userid;       // Owner of the item
    private String name;
    private boolean proofUploadedByOwner = false;
    private boolean proofUploadedByFinder = false;
    private boolean validated = false;
    private String location;
    private String itemtype;
    private String type;         // LOST / FOUND / PROOF
    private String description;
    private String status;       // e.g., Pending, Found, Matched
    private String phone;
    private String address;
    private String email;

    private String imageUrl;     // Main image stored in Supabase

    private List<String> ownerproofUrls;
    private List<String> finderproofUrls; // List of proof images (optional)

    private boolean notificationSent; // true if a notification has been sent to the owner
    private boolean matched;           // true if a lost item is matched with a found item

    private String foundByUserId;
    private String finderEmail;
    private String finderPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;   // Track updates like proof upload or match

    // Auto-generate ID and timestamp if not provided
    public void init() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }
}
