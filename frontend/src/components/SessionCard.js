import React from 'react';
import { HiPencil, HiTrash, HiClock, HiBookOpen } from 'react-icons/hi';

const STATUS_COLORS = {
  UPCOMING: 'bg-blue-100 text-blue-700',
  IN_PROGRESS: 'bg-green-100 text-green-700',
  COMPLETED: 'bg-gray-100 text-gray-600',
  CANCELLED: 'bg-red-100 text-red-600',
};

function formatDateTime(iso) {
  return new Date(iso).toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
  });
}

export default function SessionCard({ session, onEdit, onDelete }) {
  return (
    <div className="bg-white rounded-xl border border-gray-200 p-5 hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <div className="flex items-center space-x-2 mb-1">
            <HiBookOpen className="h-4 w-4 text-indigo-500" />
            <h3 className="font-semibold text-gray-800">{session.subject}</h3>
            <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_COLORS[session.status]}`}>
              {session.status}
            </span>
          </div>
          <p className="text-sm text-gray-600 ml-6">{session.topic}</p>
        </div>

        <div className="flex space-x-1">
          <button
            onClick={() => onEdit(session)}
            className="p-2 text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition"
            title="Edit"
          >
            <HiPencil className="h-4 w-4" />
          </button>
          <button
            onClick={() => onDelete(session.id)}
            className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition"
            title="Delete"
          >
            <HiTrash className="h-4 w-4" />
          </button>
        </div>
      </div>

      <div className="mt-3 flex items-center text-sm text-gray-500 ml-6">
        <HiClock className="h-4 w-4 mr-1" />
        <span>{formatDateTime(session.startTime)}</span>
        {session.endTime && <span className="mx-1">-</span>}
        {session.endTime && <span>{formatDateTime(session.endTime)}</span>}
      </div>
    </div>
  );
}
