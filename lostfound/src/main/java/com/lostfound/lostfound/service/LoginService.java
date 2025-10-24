package com.lostfound.lostfound.service;

import com.lostfound.lostfound.Model.User;
import com.lostfound.lostfound.Repository.userRepository;
import com.lostfound.lostfound.Utils.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class LoginService  {

    private final userRepository userRepositorys;
    private final JwtUtil jwtUtil;

    public LoginService(userRepository userRepositorys,
                            JwtUtil jwtUtil) {
        this.userRepositorys = userRepositorys;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, String> login(String email, String password) {
        // Try Candidate login first
        Optional<User> useropt = userRepositorys.findByEmail(email);
        if (useropt.isPresent()) {
            User user = useropt.get();
            if (user.getPassword().equals(password)) {
                String token = jwtUtil.generateToken(user);
                return Map.of(
                        "status", "success",
                        "role", "candidate",
                        "message", "Login successful",
                        "token", token
                );
            }
        }


        // If neither matched
        return Map.of(
                "status", "error",
                "message", "Invalid email or password"
        );
    }
}

