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
import java.util.*;

@RestController
@RequestMapping("/items")
@CrossOrigin(origins = "http://localhost:5173")
public class ItemsController {

    @Autowired
    private ItemRepository itemsRepository;

    @Autowired
    private userRepository userRepository;

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
            @RequestParam("file") List<MultipartFile> files,
            @RequestParam("name") String name,
            @RequestParam("type") String type,
            @RequestParam("itemtype") String itemtype,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("status") String status,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("email") String email
    ) throws IOException {

        String userid = jwtUtil.getUserID(token);
        String bucket = type.equalsIgnoreCase("lost") ? "lost" : "found";

        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = supabaseUtil.uploadFile(file, bucket, userid);
            uploadedUrls.add(url);
        }

        Items item = Items.builder()
                .userid(userid)
                .name(name)
                .type(type.toUpperCase())
                .itemtype(itemtype.toUpperCase())
                .description(description)
                .location(location)
                .status(status)
                .phone(phone)
                .address(address)
                .email(email)
                .imageUrl(uploadedUrls)
                .build();

        item.init();
        Items saved = itemsRepository.save(item);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Item added successfully",
                "data", saved
        ));
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

            for (Items item : items) {
                //  Add finder contact details when item is validated
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
    // ===================== Upload proof =====================
    @PostMapping("/upload-proof/{itemId}")
    public ResponseEntity<?> uploadProof(
            @RequestHeader("Authorization") String token,
            @PathVariable String itemId,
            @RequestParam("files") List<MultipartFile> files
    ) throws IOException {

        String userId = jwtUtil.getUserID(token);
        Items item = itemsRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        boolean isOwner = userId.equals(item.getUserid());
        boolean isFinder = userId.equals(item.getFoundByUserId());
        System.out.println("isOwner: " + isOwner + " | isFinder: " + isFinder);

        if (!isOwner && !isFinder)
            return ResponseEntity.status(403).body(Map.of("status", 403, "message", "Not authorized"));

        if (files == null || files.isEmpty())
            return ResponseEntity.status(400).body(Map.of("status", 400, "message", "No files uploaded"));

        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = supabaseUtil.uploadFile(file, "proof", itemId + "/" + userId);
            uploadedUrls.add(url);
        }

        // ========== OWNER UPLOADS PROOF ==========
        if (isOwner) {
            if (item.getOwnerproofUrls() == null) item.setOwnerproofUrls(new ArrayList<>());
            item.getOwnerproofUrls().addAll(uploadedUrls);
            item.setProofUploadedByOwner(true);

            notificationService.sendNotification(
                    item.getFoundByUserId(),
                    "Owner uploaded proof for item '" + item.getName() + "'. Please review and validate."
            );
        }

        // ========== FINDER VALIDATES ==========
        else if (isFinder) {
            if (!item.isProofUploadedByOwner()) {
                return ResponseEntity.status(400).body(Map.of(
                        "status", 400,
                        "message", "Owner proof not uploaded yet"
                ));
            }

            System.out.print(item.isFinderValidated());
            item.setFinderValidated(true);
            if (item.getFinderproofUrls() == null) item.setFinderproofUrls(new ArrayList<>());
            item.getFinderproofUrls().addAll(uploadedUrls);
            item.setProofUploadedByFinder(true);

            // Save finder contact
            User finder = userRepository.findById(item.getFoundByUserId()).orElse(null);
            if (finder != null) {
                item.setFinderEmail(finder.getEmail());
                item.setFinderPhone(finder.getPhone());
            }

            notificationService.sendNotification(
                    item.getUserid(),
                    "Finder validated your proof for item '" + item.getName() + "'."
            );
        }

        // ========== CHECK BOTH VALIDATED ==========
        if (item.isFinderValidated() && item.isOwnerValidated()) {
            System.out.print(item.isFinderValidated());
            System.out.print(item.isOwnerValidated());
            item.setValidated(true);
            notificationService.sendNotification(
                    item.getUserid(),
                    "Item '" + item.getName() + "' has been mutually validated. Contact information is now shared."
            );
            notificationService.sendNotification(
                    item.getFoundByUserId(),
                    "Item '" + item.getName() + "' has been mutually validated. Contact information is now shared."
            );
        }

        itemsRepository.save(item);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Proof uploaded successfully",
                "uploadedUrls", uploadedUrls
        ));
    }
    @PostMapping("/validate/{itemId}")
    public ResponseEntity<?> validateProof(
            @RequestHeader("Authorization") String token,
            @PathVariable String itemId
    ) {
        String userId = jwtUtil.getUserID(token);
        Items item = itemsRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        boolean isOwner = userId.equals(item.getUserid());
        if (!isOwner) {
            return ResponseEntity.status(403).body(Map.of("status", 403, "message", "Only owner can validate"));
        }

        if (!item.isProofUploadedByOwner() || !item.isProofUploadedByFinder()) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", 400,
                    "message", "Both proofs must be uploaded before validation"
            ));
        }

        // Owner validates the finder proof
        item.setOwnerValidated(true);

        if (item.isFinderValidated() && item.isOwnerValidated()) {
            item.setValidated(true);

            notificationService.sendNotification(
                    item.getUserid(),
                    "Item '" + item.getName() + "' has been mutually validated. Contact info shared."
            );
            notificationService.sendNotification(
                    item.getFoundByUserId(),
                    "Item '" + item.getName() + "' has been mutually validated. Contact info shared."
            );
        }

        itemsRepository.save(item);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Owner validated finder proof successfully",
                "validated", item.isValidated()
        ));
    }

    // ===================== DELETE LOST ITEM =====================
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteItem(
            @RequestHeader("Authorization") String token,
            @PathVariable String itemId
    ) {
        try {
            String userId = jwtUtil.getUserID(token);

            Items item = itemsRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            //  Ensure only the owner can delete their item
            if (!item.getUserid().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "status", 403,
                        "message", "You are not authorized to delete this item."
                ));
            }

            //  Optional: Remove images from Supabase storage
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                for (String url : item.getImageUrl()) {
                    try {
                        supabaseUtil.deleteFileByUrl(url);
                    } catch (Exception e) {
                        System.out.println("Warning: Could not delete file " + url);
                    }
                }
            }

            //  Delete the item record
            itemsRepository.delete(item);

            //  Optional: Send a notification
            notificationService.sendNotification(
                    userId,
                    "Your lost item '" + item.getName() + "' has been successfully deleted."
            );

            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "Item removed successfully."
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "status", 500,
                    "message", "Failed to remove item: " + e.getMessage()
            ));
        }
    }

}
