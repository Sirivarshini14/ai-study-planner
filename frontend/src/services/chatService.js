import api from './api';

const chatService = {
  send: (data) => api.post('/chat', data),
  getHistory: (sessionId) =>
    api.get('/chat/history', { params: sessionId ? { sessionId } : {} }),
  clearHistory: (sessionId) =>
    api.delete('/chat/history', { params: sessionId ? { sessionId } : {} }),
};

export default chatService;
