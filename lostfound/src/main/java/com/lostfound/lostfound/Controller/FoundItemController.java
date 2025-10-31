package com.lostfound.lostfound.Controller;

import com.lostfound.lostfound.Model.Items;
import com.lostfound.lostfound.Utils.JwtUtil;
import com.lostfound.lostfound.Repository.ItemRepository;
import com.lostfound.lostfound.Utils.SupabaseUtil;
import com.lostfound.lostfound.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/found")
@CrossOrigin(origins = "http://localhost:5173")
public class FoundItemController {
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    ItemRepository itemsRepository;
    @Autowired
    NotificationService notificationService;
    @Autowired
    private SupabaseUtil supabaseUtil;

    // ===================== Show items found by logged-in user =====================
    @GetMapping("/me")
    public ResponseEntity<?> showFoundItems(
            @RequestHeader("Authorization") String token
    ) {
        try {

            String userId = jwtUtil.getUserID(token);

            // Fetch items where logged-in user is the finder

            List<Items> items = itemsRepository.findByFoundByUserId(userId);

            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "Items found by you fetched successfully",
                    "data", items
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", 500,
                    "message", e.getMessage()
            ));
        }
    }


    // ===================== Mark item as found =====================
    @PostMapping("/{itemId}")
    public ResponseEntity<?> markFound(
            @RequestHeader("Authorization") String token,
            @PathVariable String itemId
    ) {
        String finderId = jwtUtil.getUserID(token);
        Items item = itemsRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getUserid().equals(finderId))
            return ResponseEntity.badRequest().body(Map.of("status", 400, "message", "Cannot mark your own item as found"));

        item.setStatus("FOUND");
        item.setFoundByUserId(finderId);
        itemsRepository.save(item);

        notificationService.sendNotification(item.getUserid(),
                "Your lost item '" + item.getName() + "' has been found! Upload proof to validate.");

        return ResponseEntity.ok(Map.of("status", 200, "message", "Marked as found and owner notified"));

}
    @PostMapping("/not/{itemId}")
    public ResponseEntity<?> markNotFound(
            @RequestHeader("Authorization") String token,
            @PathVariable String itemId
    ) {
        String finderId = jwtUtil.getUserID(token);
        Items item = itemsRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getUserid().equals(finderId))
            return ResponseEntity.badRequest().body(Map.of("status", 400, "message", "Cannot mark your own item as not found"));

        try {
            // Delete proof images from Supabase
            if (item.getOwnerproofUrls() != null) {
                for (String url : item.getOwnerproofUrls()) {
                    supabaseUtil.deleteFileByUrl(url);
                }
            }
            if (item.getFinderproofUrls() != null) {
                for (String url : item.getFinderproofUrls()) {
                    supabaseUtil.deleteFileByUrl(url);
                }
            }

            // Reset item details
            item.setStatus("LOST");
            item.setValidated(false);
            item.setProofUploadedByFinder(false);
            item.setProofUploadedByOwner(false);
            item.setMatched(false);
            item.setOwnerproofUrls(null);
            item.setFinderproofUrls(null);
            item.setFoundByUserId(null);
            item.setFinderEmail(null);
            item.setFinderPhone(null);
            item.setNotificationSent(false);
            item.setUpdatedAt(LocalDateTime.now());

            itemsRepository.save(item);

            return ResponseEntity.ok(Map.of("status", 200, "message", "Item reset to LOST and all proofs deleted"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", 500,
                    "message", "Error while resetting item: " + e.getMessage()
            ));
        }
    }

}
