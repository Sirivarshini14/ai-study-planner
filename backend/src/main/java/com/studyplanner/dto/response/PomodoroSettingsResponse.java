package com.studyplanner.dto.response;

import com.studyplanner.entity.PomodoroSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PomodoroSettingsResponse {

    private int focusMinutes;
    private int breakMinutes;
    private int longBreakMinutes;
    private int sessionsBeforeLongBreak;

    public static PomodoroSettingsResponse from(PomodoroSettings settings) {
        return PomodoroSettingsResponse.builder()
                .focusMinutes(settings.getFocusMinutes())
                .breakMinutes(settings.getBreakMinutes())
                .longBreakMinutes(settings.getLongBreakMinutes())
                .sessionsBeforeLongBreak(settings.getSessionsBeforeLongBreak())
                .build();
    }
}
