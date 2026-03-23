package com.studyplanner.service;

import com.studyplanner.dto.request.ChatRequest;
import com.studyplanner.dto.response.ChatMessageResponse;
import com.studyplanner.entity.ChatMessage;
import com.studyplanner.entity.StudySession;
import com.studyplanner.entity.User;
import com.studyplanner.repository.ChatMessageRepository;
import com.studyplanner.repository.StudySessionRepository;
import com.studyplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatRepository;
    private final StudySessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final GroqApiClient groqApiClient;

    @Transactional
    public ChatMessageResponse sendMessage(Long userId, ChatRequest request) {
        User user = userRepository.getReferenceById(userId);

        // Resolve study session context (optional)
        StudySession session = null;
        String subject = "General";
        String topic = "General";

        if (request.getSessionId() != null) {
            session = sessionRepository.findByIdAndUserId(request.getSessionId(), userId)
                    .orElse(null);
            if (session != null) {
                subject = session.getSubject();
                topic = session.getTopic();
            }
        }

        // Save user message
        ChatMessage userMsg = ChatMessage.builder()
                .user(user)
                .session(session)
                .role("user")
                .content(request.getMessage())
                .build();
        chatRepository.save(userMsg);

        // Build conversation context for Groq
        List<Map<String, String>> messages = buildConversation(userId, request.getSessionId(), subject, topic, request.getMessage());

        // Call Groq API
        String aiResponse = groqApiClient.chatCompletion(messages);

        // Save assistant message
        ChatMessage assistantMsg = ChatMessage.builder()
                .user(user)
                .session(session)
                .role("assistant")
                .content(aiResponse)
                .build();
        chatRepository.save(assistantMsg);

        return ChatMessageResponse.from(assistantMsg);
    }

    public List<ChatMessageResponse> getHistory(Long userId, Long sessionId) {
        List<ChatMessage> messages;
        if (sessionId != null) {
            messages = chatRepository.findByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);
        } else {
            messages = chatRepository.findByUserIdAndSessionIdOrderByCreatedAtAsc(userId, null);
        }
        return messages.stream().map(ChatMessageResponse::from).toList();
    }

    @Transactional
    public void clearHistory(Long userId, Long sessionId) {
        chatRepository.deleteByUserIdAndSessionId(userId, sessionId);
    }

    /**
     * Builds the messages array for Groq:
     * 1. System prompt with subject/topic context
     * 2. Last 10 messages for conversation history
     * 3. Current user message
     */
    private List<Map<String, String>> buildConversation(
            Long userId, Long sessionId, String subject, String topic, String currentMessage) {

        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt with study context
        String systemPrompt = String.format(
                "You are a helpful AI study assistant. The student is currently studying the subject \"%s\", "
                + "specifically the topic \"%s\". Provide clear, concise, and educational responses. "
                + "Use examples when helpful. If the question is not related to the subject, you can still "
                + "answer but gently remind them of their current study focus.",
                subject, topic
        );
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // Load last 10 messages for context (returned in DESC order, so reverse)
        if (sessionId != null) {
            List<ChatMessage> recent = chatRepository
                    .findTop10ByUserIdAndSessionIdOrderByCreatedAtDesc(userId, sessionId);
            List<ChatMessage> reversed = new ArrayList<>(recent);
            Collections.reverse(reversed);
            for (ChatMessage msg : reversed) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
        }

        // Current message
        messages.add(Map.of("role", "user", "content", currentMessage));

        return messages;
    }
}
