package com.studyplanner.service;

import com.studyplanner.entity.OtpVerification;
import com.studyplanner.exception.BadRequestException;
import com.studyplanner.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final SmsService smsService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;

    private final SecureRandom random = new SecureRandom();

    @Transactional
    public void generateAndSend(String mobile) {
        String otp = generateOtp();

        OtpVerification verification = OtpVerification.builder()
                .mobile(mobile)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();

        otpRepository.save(verification);
        smsService.sendOtp(mobile, otp);
    }

    @Transactional
    public boolean verify(String mobile, String otp) {
        OtpVerification verification = otpRepository
                .findTopByMobileAndUsedFalseOrderByCreatedAtDesc(mobile)
                .orElseThrow(() -> new BadRequestException("No OTP found. Please request a new one."));

        if (verification.getAttempts() >= MAX_ATTEMPTS) {
            throw new BadRequestException("Too many attempts. Please request a new OTP.");
        }

        verification.setAttempts(verification.getAttempts() + 1);

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.save(verification);
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        if (!verification.getOtp().equals(otp)) {
            otpRepository.save(verification);
            throw new BadRequestException("Invalid OTP. " + (MAX_ATTEMPTS - verification.getAttempts()) + " attempts remaining.");
        }

        verification.setUsed(true);
        otpRepository.save(verification);
        return true;
    }

    private String generateOtp() {
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }
}
