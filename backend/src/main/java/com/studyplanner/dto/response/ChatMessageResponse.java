package com.studyplanner.dto.response;

import com.studyplanner.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ChatMessageResponse {

    private Long id;
    private String role;
    private String content;
    private Long sessionId;
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(ChatMessage msg) {
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .role(msg.getRole())
                .content(msg.getContent())
                .sessionId(msg.getSession() != null ? msg.getSession().getId() : null)
                .createdAt(msg.getCreatedAt())
                .build();
    }
}
