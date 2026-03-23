package com.studyplanner.dto.response;

import com.studyplanner.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String message;
    private boolean read;
    private Long sessionId;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .read(notification.isRead())
                .sessionId(notification.getSession().getId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
