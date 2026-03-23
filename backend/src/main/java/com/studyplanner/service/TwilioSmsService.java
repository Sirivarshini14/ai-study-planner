package com.studyplanner.service;

import com.studyplanner.config.TwilioProperties;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.twilio.account-sid", matchIfMissing = false)
public class TwilioSmsService implements SmsService {

    private final TwilioProperties twilioProperties;

    @Override
    public void sendOtp(String mobile, String otp) {
        String body = "Your StudyPlanner verification code is: " + otp + ". Valid for 5 minutes.";
        sendSms(mobile, body);
    }

    @Override
    public void sendSms(String mobile, String message) {
        String to = formatIndianNumber(mobile);
        try {
            Message msg = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(twilioProperties.getPhoneNumber()),
                    message
            ).create();

            log.info("SMS sent to {} | SID: {} | Status: {}", to, msg.getSid(), msg.getStatus());
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send SMS: " + e.getMessage());
        }
    }

    private String formatIndianNumber(String mobile) {
        if (mobile.startsWith("+")) return mobile;
        if (mobile.length() == 10) return "+91" + mobile;
        if (mobile.length() == 12 && mobile.startsWith("91")) return "+" + mobile;
        return "+" + mobile;
    }
}
