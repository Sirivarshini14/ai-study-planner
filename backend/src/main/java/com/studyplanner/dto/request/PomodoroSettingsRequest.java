package com.studyplanner.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PomodoroSettingsRequest {

    @Min(value = 1, message = "Focus time must be at least 1 minute")
    @Max(value = 120, message = "Focus time must be at most 120 minutes")
    private int focusMinutes = 25;

    @Min(value = 1, message = "Break time must be at least 1 minute")
    @Max(value = 30, message = "Break time must be at most 30 minutes")
    private int breakMinutes = 5;

    @Min(value = 1, message = "Long break must be at least 1 minute")
    @Max(value = 60, message = "Long break must be at most 60 minutes")
    private int longBreakMinutes = 15;

    @Min(value = 1, message = "Must be at least 1 session")
    @Max(value = 10, message = "Must be at most 10 sessions")
    private int sessionsBeforeLongBreak = 4;
}
