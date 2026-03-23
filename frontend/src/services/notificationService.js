import api from './api';

const notificationService = {
  getUnread: () => api.get('/notifications'),
  getAll: () => api.get('/notifications/all'),
  markAsRead: (id) => api.put(`/notifications/${id}/read`),
  markAllAsRead: () => api.put('/notifications/read-all'),
};

export default notificationService;
