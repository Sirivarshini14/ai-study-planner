package com.studyplanner.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudySessionRequest {

    @NotBlank(message = "Subject is required")
    @Size(max = 100, message = "Subject must be at most 100 characters")
    private String subject;

    @NotBlank(message = "Topic is required")
    @Size(max = 255, message = "Topic must be at most 255 characters")
    private String topic;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
