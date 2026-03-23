package com.studyplanner.service;

import com.studyplanner.dto.request.PomodoroSettingsRequest;
import com.studyplanner.dto.response.PomodoroSettingsResponse;
import com.studyplanner.entity.PomodoroSettings;
import com.studyplanner.entity.User;
import com.studyplanner.repository.PomodoroSettingsRepository;
import com.studyplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PomodoroService {

    private final PomodoroSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    public PomodoroSettingsResponse getSettings(Long userId) {
        PomodoroSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> defaultSettings());
        return PomodoroSettingsResponse.from(settings);
    }

    @Transactional
    public PomodoroSettingsResponse updateSettings(Long userId, PomodoroSettingsRequest request) {
        PomodoroSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.getReferenceById(userId);
                    return PomodoroSettings.builder().user(user).build();
                });

        settings.setFocusMinutes(request.getFocusMinutes());
        settings.setBreakMinutes(request.getBreakMinutes());
        settings.setLongBreakMinutes(request.getLongBreakMinutes());
        settings.setSessionsBeforeLongBreak(request.getSessionsBeforeLongBreak());

        return PomodoroSettingsResponse.from(settingsRepository.save(settings));
    }

    private PomodoroSettings defaultSettings() {
        return PomodoroSettings.builder()
                .focusMinutes(25)
                .breakMinutes(5)
                .longBreakMinutes(15)
                .sessionsBeforeLongBreak(4)
                .build();
    }
}
