package com.studyplanner.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String mobile) {
        return buildToken(userId, mobile, jwtProperties.getAccessTokenExpiryMs(), "access");
    }

    public String generateRefreshToken(Long userId, String mobile) {
        return buildToken(userId, mobile, jwtProperties.getRefreshTokenExpiryMs(), "refresh");
    }

    private String buildToken(Long userId, String mobile, long expiryMs, String type) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("mobile", mobile)
                .claim("type", type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryMs))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public String getTokenType(String token) {
        return parseToken(token).get("type", String.class);
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
