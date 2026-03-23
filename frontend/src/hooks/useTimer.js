import { useState, useEffect, useRef, useCallback } from 'react';

const PHASES = { FOCUS: 'FOCUS', BREAK: 'BREAK', LONG_BREAK: 'LONG_BREAK' };

export default function useTimer(settings) {
  const [phase, setPhase] = useState(PHASES.FOCUS);
  const [secondsLeft, setSecondsLeft] = useState(settings.focusMinutes * 60);
  const [isRunning, setIsRunning] = useState(false);
  const [completedSessions, setCompletedSessions] = useState(0);

  const intervalRef = useRef(null);

  const getDuration = useCallback(
    (p) => {
      switch (p) {
        case PHASES.FOCUS:
          return settings.focusMinutes * 60;
        case PHASES.BREAK:
          return settings.breakMinutes * 60;
        case PHASES.LONG_BREAK:
          return settings.longBreakMinutes * 60;
        default:
          return settings.focusMinutes * 60;
      }
    },
    [settings]
  );

  // Handle countdown
  useEffect(() => {
    if (!isRunning) return;

    intervalRef.current = setInterval(() => {
      setSecondsLeft((prev) => {
        if (prev <= 1) {
          clearInterval(intervalRef.current);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(intervalRef.current);
  }, [isRunning]);

  // Handle phase transition when timer hits zero
  useEffect(() => {
    if (secondsLeft !== 0 || !isRunning) return;

    // Play notification sound
    try {
      const audio = new Audio('data:audio/wav;base64,UklGRlgBAABXQVZFZm10IBAAAAABAAEARKwAAIhYAQACABAAZGF0YTQBAABkAKgA1ADUAKQAZAAAAMD/oP+A/4D/oP/A/wAAAABkAKgA1ADUAKQAZAAAAMD/oP+A/4D/oP/A/wAAAABkAKgA1ADUAKQAZAAAAMD/oP+A/4D/oP/A/wAA');
      audio.play().catch(() => {});
    } catch {
      // ignore audio errors
    }

    if (phase === PHASES.FOCUS) {
      const newCount = completedSessions + 1;
      setCompletedSessions(newCount);

      if (newCount % settings.sessionsBeforeLongBreak === 0) {
        setPhase(PHASES.LONG_BREAK);
        setSecondsLeft(getDuration(PHASES.LONG_BREAK));
      } else {
        setPhase(PHASES.BREAK);
        setSecondsLeft(getDuration(PHASES.BREAK));
      }
    } else {
      setPhase(PHASES.FOCUS);
      setSecondsLeft(getDuration(PHASES.FOCUS));
    }
  }, [secondsLeft, isRunning, phase, completedSessions, settings.sessionsBeforeLongBreak, getDuration]);

  const start = () => setIsRunning(true);
  const pause = () => setIsRunning(false);

  const reset = () => {
    setIsRunning(false);
    setPhase(PHASES.FOCUS);
    setSecondsLeft(getDuration(PHASES.FOCUS));
    setCompletedSessions(0);
  };

  const skip = () => {
    setIsRunning(false);
    if (phase === PHASES.FOCUS) {
      const newCount = completedSessions + 1;
      setCompletedSessions(newCount);
      if (newCount % settings.sessionsBeforeLongBreak === 0) {
        setPhase(PHASES.LONG_BREAK);
        setSecondsLeft(getDuration(PHASES.LONG_BREAK));
      } else {
        setPhase(PHASES.BREAK);
        setSecondsLeft(getDuration(PHASES.BREAK));
      }
    } else {
      setPhase(PHASES.FOCUS);
      setSecondsLeft(getDuration(PHASES.FOCUS));
    }
  };

  // Resync timer when settings change (only if not running)
  useEffect(() => {
    if (!isRunning) {
      setSecondsLeft(getDuration(phase));
    }
  }, [settings, getDuration, phase, isRunning]);

  const totalSeconds = getDuration(phase);
  const progress = totalSeconds > 0 ? ((totalSeconds - secondsLeft) / totalSeconds) * 100 : 0;

  return {
    phase,
    secondsLeft,
    isRunning,
    completedSessions,
    progress,
    start,
    pause,
    reset,
    skip,
  };
}

export { PHASES };
