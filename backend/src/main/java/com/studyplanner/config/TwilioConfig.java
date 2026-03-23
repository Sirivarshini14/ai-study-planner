package com.studyplanner.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TwilioConfig {

    private final TwilioProperties twilioProperties;

    @PostConstruct
    public void init() {
        if (twilioProperties.getAccountSid() != null && !twilioProperties.getAccountSid().isBlank()) {
            Twilio.init(twilioProperties.getAccountSid(), twilioProperties.getAuthToken());
            log.info("Twilio initialized with account: {}", twilioProperties.getAccountSid());
        } else {
            log.warn("Twilio credentials not set — SMS will be logged to console only");
        }
    }
}
