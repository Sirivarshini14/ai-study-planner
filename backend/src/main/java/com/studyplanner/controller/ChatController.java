package com.studyplanner.controller;

import com.studyplanner.dto.request.ChatRequest;
import com.studyplanner.dto.response.ChatMessageResponse;
import com.studyplanner.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatMessageResponse> sendMessage(
            Authentication auth,
            @Valid @RequestBody ChatRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(chatService.sendMessage(userId, request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageResponse>> getHistory(
            Authentication auth,
            @RequestParam(required = false) Long sessionId) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(chatService.getHistory(userId, sessionId));
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory(
            Authentication auth,
            @RequestParam(required = false) Long sessionId) {
        Long userId = (Long) auth.getPrincipal();
        chatService.clearHistory(userId, sessionId);
        return ResponseEntity.noContent().build();
    }
}
