package com.helu.internship.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller serving as a secure proxy to the FastAPI Chatbot.
 * Ensures the chatbot endpoints are authenticated using Spring Security
 * and eliminates cross-origin port issues for the frontend.
 */
@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String FASTAPI_URL = "http://localhost:8000/ask";

    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> askChatbot(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Question cannot be empty");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            // Setup headers to satisfy the chatbot authentication middleware
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer HeluBackendSecretToken");

            Map<String, String> body = new HashMap<>();
            body.put("question", question);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.postForEntity(FASTAPI_URL, entity, Map.class);
            
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Tổng đài AI hiện tại đang bận, vui lòng thử lại sau.");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }
}
