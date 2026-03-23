package com.studyplanner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.twilio")
public class TwilioProperties {
    private String accountSid;
    private String authToken;
    private String phoneNumber;
}
