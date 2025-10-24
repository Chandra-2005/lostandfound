package com.lostfound.lostfound.Controller;

import com.lostfound.lostfound.Model.Items;
import com.lostfound.lostfound.service.ItemsService;
import com.lostfound.lostfound.Utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/items")
@CrossOrigin(origins = "http://localhost:5173") // React dev port
public class ItemsController {

    @Autowired
    private ItemsService itemsService;

    @Autowired
    private JwtUtil jwtUtil;

    // ➕ Add a new item (extracts userid from token)
    @PostMapping("/add")
    public ResponseEntity<?> addItem(@RequestHeader("Authorization") String token, @RequestBody Items item) {
        try {
            String userid = JwtUtil.getUserID(token);
            Items saved = itemsService.addItem(item, userid);
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "Item added successfully",
                    "data", saved
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    // 📋 Get all items for the logged-in user
    @GetMapping("/all")
    public ResponseEntity<?> getAllItems(@RequestHeader("Authorization") String token) {
        try {
            String userid = JwtUtil.getUserID(token);
            List<Items> items = itemsService.getAllItemsForUser(userid);
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "data", items
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    // 🔍 Get items by type (LOST / FOUND)
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getItemsByType(@RequestHeader("Authorization") String token, @PathVariable String type) {
        try {
            String userid = JwtUtil.getUserID(token);
            List<Items> items = itemsService.getItemsByType(userid, type);
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "data", items
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    // ✏️ Update item
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateItem(@RequestHeader("Authorization") String token,
                                        @PathVariable String id,
                                        @RequestBody Items updatedItem) {
        try {
            String userid = JwtUtil.getUserID(token);
            Items updated = itemsService.updateItem(id, updatedItem, userid);
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "Item updated successfully",
                    "data", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    // ❌ Delete item
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteItem(@RequestHeader("Authorization") String token, @PathVariable String id) {
        try {
            String userid = JwtUtil.getUserID(token);
            itemsService.deleteItem(id, userid);
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "Item deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}
