import api from './api';

const pomodoroService = {
  getSettings: () => api.get('/pomodoro/settings'),
  updateSettings: (data) => api.put('/pomodoro/settings', data),
};

export default pomodoroService;
