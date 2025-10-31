package com.lostfound.lostfound.Controller;

import com.lostfound.lostfound.Model.User;
import com.lostfound.lostfound.Repository.userRepository;
import com.lostfound.lostfound.Utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {

    @Autowired
    private userRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        Optional<User> candidateOpt = userRepository.findByEmail(email);

        if (candidateOpt.isPresent()) {
            User candidate = candidateOpt.get();
            if (!candidate.getPassword().equals(password)) {
                return ResponseEntity.status(400).body(Map.of(
                        "status", "error",
                        "message", "Invalid email or password"
                ));
            }

            // Generate JWT
            String token = jwtUtil.generateToken(candidate);

            // Return JWT to client
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "Login successful",
                    "token", token
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Invalid user type"
            ));
        }
    }
}
