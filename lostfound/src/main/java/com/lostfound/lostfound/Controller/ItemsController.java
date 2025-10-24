package com.lostfound.lostfound.Controller;

import com.lostfound.lostfound.Model.Items;
import com.lostfound.lostfound.Model.User;
import com.lostfound.lostfound.Repository.ItemRepository;
import com.lostfound.lostfound.Repository.userRepository;
import com.lostfound.lostfound.service.NotificationService;
import com.lostfound.lostfound.Utils.JwtUtil;
import com.lostfound.lostfound.Utils.SupabaseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/items")
@CrossOrigin(origins = "http://localhost:5173")
public class ItemsController {

    @Autowired
    private ItemRepository itemsRepository;

    @Autowired
    private userRepository userRepository; // To fetch finder info

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SupabaseUtil supabaseUtil;

    @Autowired
    private NotificationService notificationService;

    // ===================== Add new item =====================
    @PostMapping("/add")
    public ResponseEntity<?> addItem(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("type") String type,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("status") String status,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("email") String email
    ) {
        try {
            String userid = jwtUtil.getUserID(token);

            // Determine bucket
            String bucket = switch (type.toLowerCase()) {
                case "lost" -> "lost";
                case "found" -> "found";
                default -> throw new RuntimeException("Invalid type: must be lost or found");
            };

            // Upload image to Supabase
            String imageUrl = supabaseUtil.uploadFile(file, bucket, userid);

            // Save item in MongoDB
            Items item = Items.builder()
                    .userid(userid)
                    .name(name)
                    .type(type.toUpperCase())
                    .description(description)
                    .location(location)
                    .status(status)
                    .phone(phone)
                    .address(address)
                    .email(email)
                    .imageUrl(imageUrl)
                    .build();

            item.init();
            Items saved = itemsRepository.save(item);

            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "Item added successfully",
                    "data", saved
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", 500,
                    "message", e.getMessage()
            ));
        }
    }

    // ===================== Show items by type for logged-in user =====================
    @GetMapping("/show/{type}")
    public ResponseEntity<?> showItemsByType(
            @RequestHeader("Authorization") String token,
            @PathVariable String type
    ) {
        try {
            String userid = jwtUtil.getUserID(token);
            List<Items> items = itemsRepository.findByUseridAndType(userid, type.toUpperCase());

            // Populate finder info if validated
            for (Items item : items) {
                if (item.isValidated() && item.getFoundByUserId() != null) {
                    User finder = userRepository.findById(item.getFoundByUserId()).orElse(null);
                    if (finder != null) {
                        item.setFinderEmail(finder.getEmail());
                        item.setFinderPhone(finder.getPhone());
                    }
                }
            }

            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", type.toUpperCase() + " items fetched successfully",
                    "data", items
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", 500,
                    "message", e.getMessage()
            ));
        }
    }

    // ===================== Show LOST items of other users =====================
    @GetMapping("/lost/others")
    public ResponseEntity<?> showLostItemsOfOthers(@RequestHeader("Authorization") String token) {
        try {
            String userid = jwtUtil.getUserID(token);
            List<Items> items = itemsRepository.findByTypeAndUseridNot("LOST", userid);
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "LOST items of other users fetched successfully",
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
    @PostMapping("/found/{itemId}")
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

    // ===================== Upload proof =====================
    @PostMapping("/upload-proof/{itemId}")
    public ResponseEntity<?> uploadProof(
            @RequestHeader("Authorization") String token,
            @PathVariable String itemId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        String userId = jwtUtil.getUserID(token);
        Items item = itemsRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        boolean isOwner = userId.equals(item.getUserid());
        boolean isFinder = userId.equals(item.getFoundByUserId());

        if (!isOwner && !isFinder)
            return ResponseEntity.status(403).body(Map.of("status", 403, "message", "Not authorized"));

        String imageUrl = supabaseUtil.uploadFile(file, "proof", itemId + "/" + userId);

        if (isOwner) {
            item.setOwnerproofUrls(List.of(imageUrl));
            item.setProofUploadedByOwner(true);

            // Notify finder
            notificationService.sendNotification(item.getFoundByUserId(),
                    "Owner uploaded proof for item '" + item.getName() + "'. Please verify.");
        } else if (isFinder) {
            if (!item.isProofUploadedByOwner()) {
                return ResponseEntity.status(400).body(Map.of("status", 400, "message", "Owner proof not uploaded yet"));
            }
            item.setFinderproofUrls(List.of(imageUrl));
            item.setProofUploadedByFinder(true);
            item.setValidated(true);

            // Populate finder contact in the item
            User finder = userRepository.findById(item.getFoundByUserId()).orElse(null);
            if (finder != null) {
                item.setFinderEmail(finder.getEmail());
                item.setFinderPhone(finder.getPhone());
            }

            // Notify owner that finder validated
            notificationService.sendNotification(item.getUserid(),
                    "Finder validated your proof for item '" + item.getName() + "'. Contact info is now shared.");
        }

        itemsRepository.save(item);

        return ResponseEntity.ok(Map.of("status", 200, "message", "Proof uploaded successfully"));
    }

    // ===================== Show items found by logged-in user =====================
    @GetMapping("/found-by-me")
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


}
