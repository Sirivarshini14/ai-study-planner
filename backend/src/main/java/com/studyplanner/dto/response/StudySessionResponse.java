package com.studyplanner.dto.response;

import com.studyplanner.entity.StudySession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class StudySessionResponse {

    private Long id;
    private String subject;
    private String topic;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private LocalDateTime createdAt;

    public static StudySessionResponse from(StudySession session) {
        return StudySessionResponse.builder()
                .id(session.getId())
                .subject(session.getSubject())
                .topic(session.getTopic())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .status(session.getStatus().name())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
