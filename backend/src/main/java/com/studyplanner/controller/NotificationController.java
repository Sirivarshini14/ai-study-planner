package com.studyplanner.controller;

import com.studyplanner.dto.response.NotificationResponse;
import com.studyplanner.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getUnread(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(notificationService.getUnread(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<NotificationResponse>> getAll(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(notificationService.getAll(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            Authentication auth,
            @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(notificationService.markAsRead(userId, id));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("markedAsRead", count));
    }
}
