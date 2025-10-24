package com.lostfound.lostfound.Utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
public class SupabaseUtil {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey; // SERVICE ROLE KEY

    private final WebClient webClient = WebClient.builder().build();

    /**
     * Upload a file to a Supabase bucket under lost/userid/ or found/userid/
     *
     * @param file   MultipartFile
     * @param bucket Bucket name: lost / found / proof
     * @param userId User ID to organize files
     * @return public URL of uploaded file
     */
    public String uploadFile(MultipartFile file, String bucket, String userId) throws IOException {
        // Use UUID + original filename
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // Full path: bucket/userid/filename
        String path = bucket + "/" + userId + "/" + fileName;

        byte[] bytes = file.getBytes();

        // PUT request to Supabase
        webClient.put()
                .uri(supabaseUrl + "/storage/v1/object/" + path)
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .header(HttpHeaders.CONTENT_TYPE, file.getContentType())
                .bodyValue(bytes)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Return public URL
        return supabaseUrl + "/storage/v1/object/public/" + path;
    }
}
