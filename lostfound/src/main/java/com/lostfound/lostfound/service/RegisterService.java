package com.lostfound.lostfound.service;

import com.lostfound.lostfound.Model.User;
import com.lostfound.lostfound.Repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RegisterService{

    @Autowired
    private userRepository userRepository;


    public User registerUser(User candidate) throws Exception {
        // Check if email already exists
        if (userRepository.findByEmail(candidate.getEmail()).isPresent()) {
            throw new Exception("Email already registered");
        }

        // Generate unique candidate ID
        candidate.setId(UUID.randomUUID().toString());

        // Set created timestamp
        candidate.setCreatedAt(LocalDateTime.now());

        // TODO: Hash the password before saving
        // candidate.setPassword(new BCryptPasswordEncoder().encode(candidate.getPassword()));

        return userRepository.save(candidate);
    }
}
