package com.studyplanner.repository;

import com.studyplanner.entity.PomodoroSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PomodoroSettingsRepository extends JpaRepository<PomodoroSettings, Long> {

    Optional<PomodoroSettings> findByUserId(Long userId);
}
