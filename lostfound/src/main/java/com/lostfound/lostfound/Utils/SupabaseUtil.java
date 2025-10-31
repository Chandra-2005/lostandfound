package com.lostfound.lostfound.Utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class SupabaseUtil {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey; // SERVICE ROLE KEY

    private final WebClient webClient = WebClient.builder().build();

    /**
     * Upload a file to Supabase Storage.
     * Example path: lost/{userId}/ or found/{userId}/
     */
    public String uploadFile(MultipartFile file, String bucket, String userId) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String path = bucket + "/" + userId + "/" + fileName;

        byte[] bytes = file.getBytes();

        webClient.put()
                .uri(supabaseUrl + "/storage/v1/object/" + path)
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .header(HttpHeaders.CONTENT_TYPE, file.getContentType())
                .bodyValue(bytes)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return supabaseUrl + "/storage/v1/object/public/" + path;
    }

    /**
     * Delete a file from Supabase using its full public URL.
     * Works for files uploaded under any bucket (lost/found/proof).
     */
    /**
     * Delete a file from Supabase using its full public URL.
     */
    public void deleteFileByUrl(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                System.err.println("⚠️ File URL is empty, skipping delete.");
                return;
            }

            // Example public URL: https://xyz.supabase.co/storage/v1/object/public/lost/user123/file.png
            String[] parts = fileUrl.split("/storage/v1/object/public/");
            if (parts.length < 2) {
                System.err.println("⚠️ Invalid file URL format: " + fileUrl);
                return;
            }

            String path = parts[1]; // lost/user123/file.png
            String[] pathParts = path.split("/", 2);
            String bucket = pathParts[0]; // lost
            String filePath = pathParts[1]; // user123/file.png

            // Supabase expects a *relative path inside the bucket*, e.g. "user123/file.png"
            String jsonBody = "[\"" + filePath + "\"]";

            System.out.println("🗑️ Deleting from bucket: " + bucket + " | Path: " + filePath);

            String response = webClient.post()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/remove")
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("✅ Supabase delete response: " + response);

        } catch (Exception e) {
            System.err.println("❌ Failed to delete file from Supabase: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Delete all files in a user's folder (optional recursive cleanup)
     */
    public void deleteFolder(String bucket, String userId, String itemId) {
        try {
            // Construct folder path like: found/userId/itemId/
            String folderPath = bucket + "/" + userId + "/" + itemId + "/";
            String jsonBody = "[\"" + folderPath + "\"]";

            webClient.post()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/remove")
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("✅ Deleted folder from Supabase: " + folderPath);
        } catch (Exception e) {
            System.err.println("❌ Failed to delete folder from Supabase: " + e.getMessage());
        }
    }
}
