package com.studyplanner.service;

public interface SmsService {
    void sendOtp(String mobile, String otp);
    void sendSms(String mobile, String message);
}
