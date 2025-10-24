package com.lostfound.lostfound.Utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.UUID;

@Component
public class SupabaseUtil {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey; // SERVICE ROLE KEY

    private final WebClient webClient = WebClient.builder().build();

    public String uploadFile(MultipartFile file, String bucket) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        byte[] bytes = file.getBytes();

        // PUT request to Supabase
        String response = WebClient.builder()
                .baseUrl("https://oxhrjvhvvyuujtifqokf.supabase.co")
                .defaultHeader("apikey", supabaseKey)
                .defaultHeader("Authorization", "Bearer " + supabaseKey)
                .build()
                .put()
                .uri("/storage/v1/object/" + bucket + "/" + fileName)
                .header(HttpHeaders.CONTENT_TYPE, file.getContentType())
                .bodyValue(bytes)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Return public URL
        return  supabaseKey+ bucket + "/" + fileName;
    }


}
