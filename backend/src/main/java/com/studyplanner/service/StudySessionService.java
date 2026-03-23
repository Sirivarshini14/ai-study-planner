package com.studyplanner.service;

import com.studyplanner.dto.request.StudySessionRequest;
import com.studyplanner.dto.response.StudySessionResponse;
import com.studyplanner.entity.StudySession;
import com.studyplanner.entity.User;
import com.studyplanner.exception.BadRequestException;
import com.studyplanner.exception.ResourceNotFoundException;
import com.studyplanner.repository.StudySessionRepository;
import com.studyplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudySessionService {

    private final StudySessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public StudySessionResponse create(Long userId, StudySessionRequest request) {
        validateTimeRange(request);

        User user = userRepository.getReferenceById(userId);

        StudySession session = StudySession.builder()
                .user(user)
                .subject(request.getSubject())
                .topic(request.getTopic())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        return StudySessionResponse.from(sessionRepository.save(session));
    }

    public List<StudySessionResponse> getAll(Long userId) {
        return sessionRepository.findByUserIdOrderByStartTimeAsc(userId)
                .stream()
                .map(StudySessionResponse::from)
                .toList();
    }

    public List<StudySessionResponse> getByStatus(Long userId, StudySession.SessionStatus status) {
        return sessionRepository.findByUserIdAndStatusOrderByStartTimeAsc(userId, status)
                .stream()
                .map(StudySessionResponse::from)
                .toList();
    }

    public List<StudySessionResponse> getByTimeRange(Long userId, LocalDateTime from, LocalDateTime to) {
        return sessionRepository.findByUserIdAndTimeRange(userId, from, to)
                .stream()
                .map(StudySessionResponse::from)
                .toList();
    }

    public StudySessionResponse getById(Long userId, Long sessionId) {
        StudySession session = findUserSession(userId, sessionId);
        return StudySessionResponse.from(session);
    }

    @Transactional
    public StudySessionResponse update(Long userId, Long sessionId, StudySessionRequest request) {
        validateTimeRange(request);

        StudySession session = findUserSession(userId, sessionId);

        session.setSubject(request.getSubject());
        session.setTopic(request.getTopic());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());

        // Reset notification flag if time changed
        session.setNotified(false);

        return StudySessionResponse.from(sessionRepository.save(session));
    }

    @Transactional
    public void delete(Long userId, Long sessionId) {
        StudySession session = findUserSession(userId, sessionId);
        sessionRepository.delete(session);
    }

    private StudySession findUserSession(Long userId, Long sessionId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Study session not found"));
    }

    private void validateTimeRange(StudySessionRequest request) {
        if (request.getEndTime() != null && request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }
    }
}
