package com.studyplanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private Long id;
    private String name;
    private String mobile;
    private String accessToken;
    private String refreshToken;
}
