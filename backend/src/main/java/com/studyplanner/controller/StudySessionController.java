package com.studyplanner.controller;

import com.studyplanner.dto.request.StudySessionRequest;
import com.studyplanner.dto.response.StudySessionResponse;
import com.studyplanner.entity.StudySession;
import com.studyplanner.service.StudySessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class StudySessionController {

    private final StudySessionService sessionService;

    @PostMapping
    public ResponseEntity<StudySessionResponse> create(
            Authentication auth,
            @Valid @RequestBody StudySessionRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.create(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<StudySessionResponse>> getAll(
            Authentication auth,
            @RequestParam(required = false) StudySession.SessionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        Long userId = (Long) auth.getPrincipal();

        if (from != null && to != null) {
            return ResponseEntity.ok(sessionService.getByTimeRange(userId, from, to));
        }
        if (status != null) {
            return ResponseEntity.ok(sessionService.getByStatus(userId, status));
        }
        return ResponseEntity.ok(sessionService.getAll(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudySessionResponse> getById(
            Authentication auth,
            @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(sessionService.getById(userId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudySessionResponse> update(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody StudySessionRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(sessionService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Authentication auth,
            @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        sessionService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
