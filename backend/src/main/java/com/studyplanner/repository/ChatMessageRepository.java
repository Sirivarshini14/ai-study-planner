package com.studyplanner.repository;

import com.studyplanner.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByUserIdAndSessionIdOrderByCreatedAtAsc(Long userId, Long sessionId);

    List<ChatMessage> findTop10ByUserIdAndSessionIdOrderByCreatedAtDesc(Long userId, Long sessionId);

    void deleteByUserIdAndSessionId(Long userId, Long sessionId);
}
