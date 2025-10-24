package com.lostfound.lostfound.Controller;

import com.lostfound.lostfound.Utils.JwtUtil;
import com.lostfound.lostfound.Model.User;
import com.lostfound.lostfound.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:5173")
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerCandidate(@RequestBody User user) {
        try {
            User savedUser = registerService.registerUser(user);

            String token = jwtUtil.generateToken(user);
            return ResponseEntity.ok(
                    java.util.Map.of(
                            "status", "success",
                            "message", "Registration successful",
                            "candidateid", savedUser.getId(),
                            "token", token
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    java.util.Map.of(
                            "status", "error",
                            "message", e.getMessage()
                    )
            );
        }
    }
}
