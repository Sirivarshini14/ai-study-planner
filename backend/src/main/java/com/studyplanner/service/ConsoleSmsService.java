package com.studyplanner.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Fallback: logs SMS to console when Twilio is not configured.
 */
@Slf4j
@Service
public class ConsoleSmsService implements SmsService {

    @Override
    public void sendOtp(String mobile, String otp) {
        log.info("=======================================================");
        log.info("  OTP for {} : {}", mobile, otp);
        log.info("=======================================================");
    }

    @Override
    public void sendSms(String mobile, String message) {
        log.info("=======================================================");
        log.info("  SMS to {} : {}", mobile, message);
        log.info("=======================================================");
    }
}
