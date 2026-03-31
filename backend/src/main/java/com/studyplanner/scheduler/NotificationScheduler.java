package com.studyplanner.scheduler;

import com.studyplanner.entity.Notification;
import com.studyplanner.entity.StudySession;
import com.studyplanner.repository.NotificationRepository;
import com.studyplanner.repository.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final StudySessionRepository sessionRepository;
    private final NotificationRepository notificationRepository;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void sendUpcomingSessionReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusMinutes(10);

        List<StudySession> sessions = sessionRepository.findSessionsToNotify(now, threshold);

        if (sessions.isEmpty()) {
            return;
        }

        log.info("Found {} sessions to notify", sessions.size());

        for (StudySession session : sessions) {
            String message = String.format(
                    "StudyPlanner Reminder: \"%s - %s\" starts at %s. Get ready!",
                    session.getSubject(),
                    session.getTopic(),
                    session.getStartTime().format(TIME_FMT)
            );

            Notification notification = Notification.builder()
                    .user(session.getUser())
                    .session(session)
                    .message(message)
                    .build();
            notificationRepository.save(notification);

            session.setNotified(true);
            sessionRepository.save(session);

            log.info("Notification created for session {} user {}",
                    session.getId(), session.getUser().getId());
        }
    }
}
