package com.lostfound.lostfound.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user")

public class User {

    @Id
    private String id;
    private String phone;
    private String username;
    private String password;
    private String email;
    private LocalDateTime createdAt;

}
