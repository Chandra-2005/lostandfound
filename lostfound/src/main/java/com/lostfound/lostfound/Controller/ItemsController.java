package com.lostfound.lostfound.Controller;

import com.lostfound.lostfound.Model.Items;
import com.lostfound.lostfound.Repository.ItemRepository;
import com.lostfound.lostfound.Utils.JwtUtil;
import com.lostfound.lostfound.Utils.SupabaseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;@RestController
@RequestMapping("/items")
@CrossOrigin(origins = "http://localhost:5173")
public class ItemsController {

    @Autowired
    private ItemRepository itemsRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SupabaseUtil supabaseUtil;

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

    // 🔍 Fetch items by type (LOST / FOUND) for the logged-in user
    @GetMapping("/show/{type}")
    public ResponseEntity<?> showItemsByType(
            @RequestHeader("Authorization") String token,
            @PathVariable String type
    ) {
        try {
            // 1️⃣ Extract userId from JWT
            String userid = jwtUtil.getUserID(token);

            // 2️⃣ Find items in MongoDB by userid and type
            List<Items> items = itemsRepository.findByUseridAndType(userid, type.toUpperCase());

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

    // 🔍 Fetch LOST items of other users
    @GetMapping("/lost/others")
    public ResponseEntity<?> showLostItemsOfOthers(
            @RequestHeader("Authorization") String token
    ) {
        try {
            // 1️⃣ Get logged-in user's ID from JWT
            String userid = jwtUtil.getUserID(token);

            // 2️⃣ Fetch items where type = LOST and userid != logged-in user
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


}
