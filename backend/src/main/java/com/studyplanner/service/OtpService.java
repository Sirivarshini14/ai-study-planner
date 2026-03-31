package com.studyplanner.service;

import com.studyplanner.entity.OtpVerification;
import com.studyplanner.exception.BadRequestException;
import com.studyplanner.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;

    private final SecureRandom random = new SecureRandom();

    @Transactional
    public boolean generateAndSend(String email) {
        String otp = generateOtp();

        OtpVerification verification = OtpVerification.builder()
                .email(email)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();

        otpRepository.save(verification);

        try {
            emailService.sendOtp(email, otp);
            return true;
        } catch (Exception e) {
            log.error("Email delivery failed for {}: {}", email, e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean verify(String email, String otp) {
        OtpVerification verification = otpRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
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
