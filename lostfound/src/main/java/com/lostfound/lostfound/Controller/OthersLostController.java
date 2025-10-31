package com.lostfound.lostfound.Controller;

import com.lostfound.lostfound.Model.Items;
import com.lostfound.lostfound.Utils.JwtUtil;
import com.lostfound.lostfound.Repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/lost")
@CrossOrigin(origins = "http://localhost:5173")
public class OthersLostController {
    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    private ItemRepository itemsRepository;



    // ===================== Show LOST items of other users =====================
    @GetMapping("/others")
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
}
