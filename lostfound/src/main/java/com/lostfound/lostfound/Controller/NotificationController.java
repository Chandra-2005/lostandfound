package com.lostfound.lostfound.Controller;

import com.lostfound.lostfound.Model.Notification;
import com.lostfound.lostfound.service.NotificationService;
import com.lostfound.lostfound.Utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    // Get notifications for logged-in user
    @GetMapping
    public ResponseEntity<?> getNotifications(@RequestHeader("Authorization") String token) {
        String userId = jwtUtil.getUserID(token);
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(Map.of("status", 200, "data", notifications));
    }

    // Mark a notification as read
    @PostMapping("/read/{id}")
    public ResponseEntity<?> markRead(@PathVariable String id) {
        Notification notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("status", 200, "data", notification));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@RequestHeader("Authorization") String token) {
        try {
            // Extract user ID from JWT
            String userId = jwtUtil.getUserID(token);

            long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(Map.of("count", count));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", 500,
                    "message", e.getMessage()
            ));
        }
    }


}
