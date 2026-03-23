package com.studyplanner.service;

import com.studyplanner.config.GroqProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Calls the Groq API using their OpenAI-compatible endpoint.
 * POST https://api.groq.com/openai/v1/chat/completions
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroqApiClient {

    private final GroqProperties groqProperties;
    private final RestTemplate restTemplate;

    /**
     * @param messages list of {role, content} maps
     * @return assistant response content
     */
    public String chatCompletion(List<Map<String, String>> messages) {
        String url = groqProperties.getBaseUrl() + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqProperties.getApiKey());

        Map<String, Object> body = Map.of(
                "model", groqProperties.getModel(),
                "messages", messages,
                "temperature", 0.7,
                "max_tokens", 1024
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            Map responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Empty response from Groq API");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("No choices in Groq API response");
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            log.error("Groq API call failed: {}", e.getMessage());
            throw new RuntimeException("Failed to get response from AI. Please try again.");
        }
    }
}
