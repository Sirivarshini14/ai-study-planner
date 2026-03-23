import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import sessionService from '../services/sessionService';
import SessionCard from '../components/SessionCard';
import SessionModal from '../components/SessionModal';
import { HiPlus } from 'react-icons/hi';

const FILTERS = ['ALL', 'UPCOMING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];

export default function Dashboard() {
  const { user } = useAuth();
  const [sessions, setSessions] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editSession, setEditSession] = useState(null);

  const fetchSessions = useCallback(async () => {
    try {
      const params = filter !== 'ALL' ? { status: filter } : {};
      const { data } = await sessionService.getAll(params);
      setSessions(data);
    } catch {
      // handled by interceptor
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    fetchSessions();
  }, [fetchSessions]);

  const handleCreate = async (data) => {
    await sessionService.create(data);
    fetchSessions();
  };

  const handleUpdate = async (data) => {
    await sessionService.update(editSession.id, data);
    setEditSession(null);
    fetchSessions();
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this session?')) return;
    await sessionService.delete(id);
    fetchSessions();
  };

  const openEdit = (session) => {
    setEditSession(session);
    setModalOpen(true);
  };

  const openCreate = () => {
    setEditSession(null);
    setModalOpen(true);
  };

  const upcomingCount = sessions.filter((s) => s.status === 'UPCOMING').length;

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">
            Welcome back, {user?.name}
          </h1>
          <p className="text-gray-500 mt-1">
            You have {upcomingCount} upcoming session{upcomingCount !== 1 ? 's' : ''}
          </p>
        </div>
        <button
          onClick={openCreate}
          className="flex items-center space-x-2 bg-indigo-600 text-white px-4 py-2.5 rounded-lg hover:bg-indigo-700 transition font-medium"
        >
          <HiPlus className="h-5 w-5" />
          <span>New Session</span>
        </button>
      </div>

      {/* Filters */}
      <div className="flex space-x-2 mb-6 overflow-x-auto pb-2">
        {FILTERS.map((f) => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            className={`px-4 py-1.5 rounded-full text-sm font-medium whitespace-nowrap transition ${
              filter === f
                ? 'bg-indigo-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {f === 'ALL' ? 'All' : f.replace('_', ' ')}
          </button>
        ))}
      </div>

      {/* Sessions list */}
      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600" />
        </div>
      ) : sessions.length === 0 ? (
        <div className="text-center py-16">
          <p className="text-gray-400 text-lg">No study sessions found</p>
          <p className="text-gray-400 text-sm mt-1">Click "New Session" to get started</p>
        </div>
      ) : (
        <div className="space-y-3">
          {sessions.map((session) => (
            <SessionCard
              key={session.id}
              session={session}
              onEdit={openEdit}
              onDelete={handleDelete}
            />
          ))}
        </div>
      )}

      {/* Modal */}
      <SessionModal
        isOpen={modalOpen}
        onClose={() => {
          setModalOpen(false);
          setEditSession(null);
        }}
        onSubmit={editSession ? handleUpdate : handleCreate}
        session={editSession}
      />
    </div>
  );
}
