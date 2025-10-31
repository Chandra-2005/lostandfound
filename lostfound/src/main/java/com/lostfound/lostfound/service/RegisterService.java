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


    public User registerUser(User user) throws Exception {
        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new Exception("Email already registered");
        }

        // Generate unique user ID
        user.setId(UUID.randomUUID().toString());

        // Set created timestamp
        user.setCreatedAt(LocalDateTime.now());

        // TODO: Hash the password before saving
        // user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));

        return userRepository.save(user);
    }
}
