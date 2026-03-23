package com.studyplanner.scheduler;

import com.studyplanner.entity.Notification;
import com.studyplanner.entity.StudySession;
import com.studyplanner.repository.NotificationRepository;
import com.studyplanner.repository.StudySessionRepository;
import com.studyplanner.service.SmsService;
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
    private final SmsService smsService;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    /**
     * Runs every 60 seconds.
     * Finds sessions starting within the next 10 minutes that haven't been notified yet.
     * Creates a notification record, sends SMS, and marks the session as notified.
     */
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

            // Save in-app notification
            Notification notification = Notification.builder()
                    .user(session.getUser())
                    .session(session)
                    .message(message)
                    .build();
            notificationRepository.save(notification);

            // Send SMS reminder
            try {
                smsService.sendSms(session.getUser().getMobile(), message);
                log.info("SMS reminder sent for session {} to user {}",
                        session.getId(), session.getUser().getMobile());
            } catch (Exception e) {
                log.error("Failed to send SMS for session {}: {}", session.getId(), e.getMessage());
                // Don't fail the whole batch — continue with other sessions
            }

            session.setNotified(true);
            sessionRepository.save(session);
        }
    }
}
