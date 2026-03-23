package com.studyplanner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.groq")
public class GroqProperties {
    private String apiKey;
    private String model;
    private String baseUrl;
}
