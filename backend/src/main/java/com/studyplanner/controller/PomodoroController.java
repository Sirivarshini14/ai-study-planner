package com.studyplanner.controller;

import com.studyplanner.dto.request.PomodoroSettingsRequest;
import com.studyplanner.dto.response.PomodoroSettingsResponse;
import com.studyplanner.service.PomodoroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pomodoro")
@RequiredArgsConstructor
public class PomodoroController {

    private final PomodoroService pomodoroService;

    @GetMapping("/settings")
    public ResponseEntity<PomodoroSettingsResponse> getSettings(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(pomodoroService.getSettings(userId));
    }

    @PutMapping("/settings")
    public ResponseEntity<PomodoroSettingsResponse> updateSettings(
            Authentication auth,
            @Valid @RequestBody PomodoroSettingsRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(pomodoroService.updateSettings(userId, request));
    }
}
