package com.studyplanner.service;

import com.studyplanner.config.JwtUtil;
import com.studyplanner.dto.request.*;
import com.studyplanner.dto.response.AuthResponse;
import com.studyplanner.dto.response.UserResponse;
import com.studyplanner.entity.User;
import com.studyplanner.exception.BadRequestException;
import com.studyplanner.exception.ResourceNotFoundException;
import com.studyplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    @Transactional
    public Map<String, String> signup(SignupRequest request) {
        if (userRepository.existsByMobile(request.getMobile())) {
            throw new BadRequestException("Mobile number already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .mobile(request.getMobile())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .verified(false)
                .build();

        userRepository.save(user);

        // Generate and send OTP
        otpService.generateAndSend(request.getMobile());

        return Map.of("message", "OTP sent to " + request.getMobile() + ". Please verify to complete signup.");
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        otpService.verify(request.getMobile(), request.getOtp());

        User user = userRepository.findByMobile(request.getMobile())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setVerified(true);
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional
    public Map<String, String> resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByMobile(request.getMobile())
                .orElseThrow(() -> new BadRequestException("Mobile number not registered"));

        if (user.isVerified()) {
            throw new BadRequestException("Mobile number is already verified");
        }

        otpService.generateAndSend(request.getMobile());

        return Map.of("message", "OTP resent to " + request.getMobile());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByMobile(request.getMobile())
                .orElseThrow(() -> new BadRequestException("Invalid mobile number or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid mobile number or password");
        }

        if (!user.isVerified()) {
            throw new BadRequestException("Mobile number not verified. Please verify OTP first.");
        }

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        if (!jwtUtil.isValid(token) || !"refresh".equals(jwtUtil.getTokenType(token))) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        Long userId = jwtUtil.getUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return buildAuthResponse(user);
    }

    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserResponse.from(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .mobile(user.getMobile())
                .accessToken(jwtUtil.generateAccessToken(user.getId(), user.getMobile()))
                .refreshToken(jwtUtil.generateRefreshToken(user.getId(), user.getMobile()))
                .build();
    }
}
