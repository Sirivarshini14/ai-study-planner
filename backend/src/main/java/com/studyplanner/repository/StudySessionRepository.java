package com.studyplanner.repository;

import com.studyplanner.entity.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    List<StudySession> findByUserIdOrderByStartTimeAsc(Long userId);

    List<StudySession> findByUserIdAndStatusOrderByStartTimeAsc(
            Long userId, StudySession.SessionStatus status);

    Optional<StudySession> findByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT s FROM StudySession s
            WHERE s.user.id = :userId
              AND s.startTime BETWEEN :from AND :to
            ORDER BY s.startTime ASC
            """)
    List<StudySession> findByUserIdAndTimeRange(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
            SELECT s FROM StudySession s
            WHERE s.notified = false
              AND s.status = 'UPCOMING'
              AND s.startTime BETWEEN :now AND :threshold
            """)
    List<StudySession> findSessionsToNotify(
            @Param("now") LocalDateTime now,
            @Param("threshold") LocalDateTime threshold);
}
