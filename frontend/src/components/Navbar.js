import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import notificationService from '../services/notificationService';
import { HiBell, HiMenu, HiX } from 'react-icons/hi';

export default function Navbar() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const [notifications, setNotifications] = useState([]);
  const [showNotif, setShowNotif] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  useEffect(() => {
    if (!user) return;

    const fetchNotifications = () => {
      notificationService.getUnread().then(({ data }) => setNotifications(data)).catch(() => {});
    };

    fetchNotifications();
    const interval = setInterval(fetchNotifications, 30000);
    return () => clearInterval(interval);
  }, [user]);

  const handleMarkAllRead = async () => {
    await notificationService.markAllAsRead();
    setNotifications([]);
    setShowNotif(false);
  };

  if (!user) return null;

  const navLinks = [
    { to: '/dashboard', label: 'Dashboard' },
    { to: '/pomodoro', label: 'Pomodoro' },
    { to: '/chat', label: 'AI Chat' },
  ];

  const isActive = (path) => location.pathname === path;

  return (
    <nav className="bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          {/* Logo + Links */}
          <div className="flex items-center space-x-8">
            <Link to="/dashboard" className="text-xl font-bold text-indigo-600">
              StudyPlanner
            </Link>
            <div className="hidden md:flex space-x-4">
              {navLinks.map((link) => (
                <Link
                  key={link.to}
                  to={link.to}
                  className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                    isActive(link.to)
                      ? 'bg-indigo-50 text-indigo-700'
                      : 'text-gray-600 hover:text-indigo-600 hover:bg-gray-50'
                  }`}
                >
                  {link.label}
                </Link>
              ))}
            </div>
          </div>

          {/* Right side */}
          <div className="flex items-center space-x-4">
            {/* Notification bell */}
            <div className="relative">
              <button
                onClick={() => setShowNotif(!showNotif)}
                className="relative p-2 text-gray-500 hover:text-indigo-600 transition-colors"
              >
                <HiBell className="h-6 w-6" />
                {notifications.length > 0 && (
                  <span className="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                    {notifications.length}
                  </span>
                )}
              </button>

              {showNotif && (
                <div className="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-lg border z-50">
                  <div className="p-3 border-b flex justify-between items-center">
                    <span className="font-semibold text-gray-700">Notifications</span>
                    {notifications.length > 0 && (
                      <button
                        onClick={handleMarkAllRead}
                        className="text-xs text-indigo-600 hover:underline"
                      >
                        Mark all read
                      </button>
                    )}
                  </div>
                  <div className="max-h-64 overflow-y-auto">
                    {notifications.length === 0 ? (
                      <p className="p-4 text-sm text-gray-500 text-center">No new notifications</p>
                    ) : (
                      notifications.map((n) => (
                        <div key={n.id} className="p-3 border-b last:border-b-0 hover:bg-gray-50">
                          <p className="text-sm text-gray-700">{n.message}</p>
                          <p className="text-xs text-gray-400 mt-1">
                            {new Date(n.createdAt).toLocaleString()}
                          </p>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              )}
            </div>

            {/* User info + logout */}
            <span className="hidden md:inline text-sm text-gray-600">Hi, {user.name}</span>
            <button
              onClick={logout}
              className="hidden md:inline-block text-sm text-red-500 hover:text-red-700 font-medium"
            >
              Logout
            </button>

            {/* Mobile menu toggle */}
            <button
              onClick={() => setMobileOpen(!mobileOpen)}
              className="md:hidden p-2 text-gray-500"
            >
              {mobileOpen ? <HiX className="h-6 w-6" /> : <HiMenu className="h-6 w-6" />}
            </button>
          </div>
        </div>

        {/* Mobile menu */}
        {mobileOpen && (
          <div className="md:hidden pb-3 space-y-1">
            {navLinks.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                onClick={() => setMobileOpen(false)}
                className={`block px-3 py-2 rounded-md text-base font-medium ${
                  isActive(link.to)
                    ? 'bg-indigo-50 text-indigo-700'
                    : 'text-gray-600 hover:bg-gray-50'
                }`}
              >
                {link.label}
              </Link>
            ))}
            <button
              onClick={logout}
              className="block w-full text-left px-3 py-2 text-base font-medium text-red-500"
            >
              Logout
            </button>
          </div>
        )}
      </div>
    </nav>
  );
}
