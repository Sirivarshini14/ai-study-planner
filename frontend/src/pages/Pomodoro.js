import React, { useState, useEffect } from 'react';
import useTimer, { PHASES } from '../hooks/useTimer';
import pomodoroService from '../services/pomodoroService';
import { HiPlay, HiPause, HiRefresh, HiFastForward, HiCog } from 'react-icons/hi';

const PHASE_LABELS = {
  [PHASES.FOCUS]: 'Focus Time',
  [PHASES.BREAK]: 'Short Break',
  [PHASES.LONG_BREAK]: 'Long Break',
};

const PHASE_COLORS = {
  [PHASES.FOCUS]: { ring: 'text-indigo-500', bg: 'bg-indigo-50', text: 'text-indigo-700' },
  [PHASES.BREAK]: { ring: 'text-green-500', bg: 'bg-green-50', text: 'text-green-700' },
  [PHASES.LONG_BREAK]: { ring: 'text-amber-500', bg: 'bg-amber-50', text: 'text-amber-700' },
};

function formatTime(seconds) {
  const m = Math.floor(seconds / 60).toString().padStart(2, '0');
  const s = (seconds % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
}

export default function Pomodoro() {
  const [settings, setSettings] = useState({
    focusMinutes: 25,
    breakMinutes: 5,
    longBreakMinutes: 15,
    sessionsBeforeLongBreak: 4,
  });
  const [showSettings, setShowSettings] = useState(false);
  const [settingsForm, setSettingsForm] = useState(settings);
  const [saving, setSaving] = useState(false);

  const timer = useTimer(settings);
  const colors = PHASE_COLORS[timer.phase];

  // Load settings from backend
  useEffect(() => {
    pomodoroService.getSettings().then(({ data }) => {
      setSettings(data);
      setSettingsForm(data);
    }).catch(() => {});
  }, []);

  // Update document title with timer
  useEffect(() => {
    document.title = `${formatTime(timer.secondsLeft)} - ${PHASE_LABELS[timer.phase]}`;
    return () => { document.title = 'StudyPlanner'; };
  }, [timer.secondsLeft, timer.phase]);

  const handleSaveSettings = async () => {
    setSaving(true);
    try {
      const { data } = await pomodoroService.updateSettings(settingsForm);
      setSettings(data);
      setShowSettings(false);
    } catch {
      // keep panel open on error
    } finally {
      setSaving(false);
    }
  };

  const handleSettingsChange = (e) => {
    setSettingsForm({ ...settingsForm, [e.target.name]: parseInt(e.target.value, 10) || 0 });
  };

  // SVG circle progress
  const radius = 120;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (timer.progress / 100) * circumference;

  return (
    <div className="max-w-lg mx-auto px-4 py-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-2xl font-bold text-gray-800">Pomodoro Timer</h1>
        <button
          onClick={() => {
            setSettingsForm(settings);
            setShowSettings(!showSettings);
          }}
          className="p-2 text-gray-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition"
        >
          <HiCog className="h-6 w-6" />
        </button>
      </div>

      {/* Phase indicator */}
      <div className="flex justify-center space-x-2 mb-8">
        {Object.entries(PHASE_LABELS).map(([key, label]) => (
          <span
            key={key}
            className={`px-4 py-1.5 rounded-full text-sm font-medium transition ${
              timer.phase === key
                ? `${PHASE_COLORS[key].bg} ${PHASE_COLORS[key].text}`
                : 'bg-gray-100 text-gray-400'
            }`}
          >
            {label}
          </span>
        ))}
      </div>

      {/* Timer circle */}
      <div className="flex justify-center mb-8">
        <div className="relative">
          <svg width="280" height="280" className="transform -rotate-90">
            <circle
              cx="140"
              cy="140"
              r={radius}
              stroke="#e5e7eb"
              strokeWidth="8"
              fill="none"
            />
            <circle
              cx="140"
              cy="140"
              r={radius}
              stroke="currentColor"
              strokeWidth="8"
              fill="none"
              strokeLinecap="round"
              strokeDasharray={circumference}
              strokeDashoffset={strokeDashoffset}
              className={`${colors.ring} transition-all duration-1000`}
            />
          </svg>
          <div className="absolute inset-0 flex flex-col items-center justify-center">
            <span className="text-5xl font-mono font-bold text-gray-800">
              {formatTime(timer.secondsLeft)}
            </span>
            <span className={`text-sm font-medium mt-1 ${colors.text}`}>
              {PHASE_LABELS[timer.phase]}
            </span>
          </div>
        </div>
      </div>

      {/* Controls */}
      <div className="flex justify-center items-center space-x-4 mb-8">
        <button
          onClick={timer.reset}
          className="p-3 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition"
          title="Reset"
        >
          <HiRefresh className="h-6 w-6" />
        </button>

        <button
          onClick={timer.isRunning ? timer.pause : timer.start}
          className="p-5 bg-indigo-600 text-white rounded-full hover:bg-indigo-700 shadow-lg transition transform hover:scale-105"
        >
          {timer.isRunning ? (
            <HiPause className="h-8 w-8" />
          ) : (
            <HiPlay className="h-8 w-8 ml-0.5" />
          )}
        </button>

        <button
          onClick={timer.skip}
          className="p-3 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition"
          title="Skip"
        >
          <HiFastForward className="h-6 w-6" />
        </button>
      </div>

      {/* Session counter */}
      <div className="text-center mb-8">
        <p className="text-sm text-gray-500">
          Sessions completed:{' '}
          <span className="font-semibold text-gray-700">{timer.completedSessions}</span>
          <span className="text-gray-400"> / {settings.sessionsBeforeLongBreak} until long break</span>
        </p>
        <div className="flex justify-center space-x-2 mt-3">
          {Array.from({ length: settings.sessionsBeforeLongBreak }).map((_, i) => (
            <div
              key={i}
              className={`h-2.5 w-2.5 rounded-full transition ${
                i < timer.completedSessions % settings.sessionsBeforeLongBreak
                  ? 'bg-indigo-500'
                  : 'bg-gray-200'
              }`}
            />
          ))}
        </div>
      </div>

      {/* Settings panel */}
      {showSettings && (
        <div className="bg-white rounded-xl border shadow-sm p-6 space-y-4">
          <h2 className="font-semibold text-gray-800">Timer Settings</h2>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Focus (min)</label>
              <input
                type="number"
                name="focusMinutes"
                value={settingsForm.focusMinutes}
                onChange={handleSettingsChange}
                min={1}
                max={120}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Break (min)</label>
              <input
                type="number"
                name="breakMinutes"
                value={settingsForm.breakMinutes}
                onChange={handleSettingsChange}
                min={1}
                max={30}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Long Break (min)</label>
              <input
                type="number"
                name="longBreakMinutes"
                value={settingsForm.longBreakMinutes}
                onChange={handleSettingsChange}
                min={1}
                max={60}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Sessions before long</label>
              <input
                type="number"
                name="sessionsBeforeLongBreak"
                value={settingsForm.sessionsBeforeLongBreak}
                onChange={handleSettingsChange}
                min={1}
                max={10}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none"
              />
            </div>
          </div>

          <div className="flex space-x-3 pt-2">
            <button
              onClick={() => setShowSettings(false)}
              className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
            >
              Cancel
            </button>
            <button
              onClick={handleSaveSettings}
              disabled={saving}
              className="flex-1 px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition"
            >
              {saving ? 'Saving...' : 'Save Settings'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
