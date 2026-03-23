package com.studyplanner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "pomodoro_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PomodoroSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "focus_minutes", nullable = false)
    @Builder.Default
    private int focusMinutes = 25;

    @Column(name = "break_minutes", nullable = false)
    @Builder.Default
    private int breakMinutes = 5;

    @Column(name = "long_break_min", nullable = false)
    @Builder.Default
    private int longBreakMinutes = 15;

    @Column(name = "sessions_before_long_break", nullable = false)
    @Builder.Default
    private int sessionsBeforeLongBreak = 4;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
