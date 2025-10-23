package com.lostfound.lostfound.Repository;

import com.lostfound.lostfound.Model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface userRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
}
