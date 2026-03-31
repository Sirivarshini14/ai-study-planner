import React, { useState, useEffect, useRef } from 'react';
import chatService from '../services/chatService';
import sessionService from '../services/sessionService';
import { HiPaperAirplane, HiTrash, HiBookOpen } from 'react-icons/hi';

export default function Chat() {
  const [sessions, setSessions] = useState([]);
  const [selectedSession, setSelectedSession] = useState(null);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);

  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  // Load user's study sessions for context picker
  useEffect(() => {
    sessionService.getAll().then(({ data }) => setSessions(data)).catch(() => {});
  }, []);

  // Load chat history when session changes
  useEffect(() => {
    setHistoryLoading(true);
    chatService
      .getHistory(selectedSession?.id)
      .then(({ data }) => setMessages(data))
      .catch(() => setMessages([]))
      .finally(() => setHistoryLoading(false));
  }, [selectedSession]);

  // Auto-scroll to bottom
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = async (e) => {
    e.preventDefault();
    const text = input.trim();
    if (!text || loading) return;

    // Optimistic user message
    const userMsg = { id: Date.now(), role: 'user', content: text, createdAt: new Date().toISOString() };
    setMessages((prev) => [...prev, userMsg]);
    setInput('');
    setLoading(true);

    try {
      const { data } = await chatService.send({
        message: text,
        sessionId: selectedSession?.id || null,
      });
      setMessages((prev) => [...prev, data]);
    } catch {
      setMessages((prev) => [
        ...prev,
        { id: Date.now() + 1, role: 'assistant', content: 'Sorry, something went wrong. Please try again.', createdAt: new Date().toISOString() },
      ]);
    } finally {
      setLoading(false);
      inputRef.current?.focus();
    }
  };

  const handleClear = async () => {
    if (!window.confirm('Clear chat history?')) return;
    await chatService.clearHistory(selectedSession?.id).catch(() => {});
    setMessages([]);
  };

  const contextLabel = selectedSession
    ? `${selectedSession.subject} — ${selectedSession.topic}`
    : 'General (no session selected)';

  return (
    <div className="max-w-4xl mx-auto px-4 py-6 flex flex-col" style={{ height: 'calc(100vh - 4rem)' }}>
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">AI Study Assistant</h1>
          <div className="flex items-center text-sm text-gray-500 mt-1">
            <HiBookOpen className="h-4 w-4 mr-1" />
            <span>Context: {contextLabel}</span>
          </div>
        </div>
        <button
          onClick={handleClear}
          className="flex items-center space-x-1 text-sm text-red-500 hover:text-red-700 px-3 py-1.5 hover:bg-red-50 rounded-lg transition"
        >
          <HiTrash className="h-4 w-4" />
          <span>Clear</span>
        </button>
      </div>

      {/* Session picker */}
      <div className="mb-4">
        <select
          value={selectedSession?.id || ''}
          onChange={(e) => {
            const id = e.target.value;
            setSelectedSession(id ? sessions.find((s) => s.id === Number(id)) : null);
          }}
          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none text-sm"
        >
          <option value="">General chat (no study session)</option>
          {sessions.map((s) => (
            <option key={s.id} value={s.id}>
              {s.subject} — {s.topic}
            </option>
          ))}
        </select>
      </div>

      {/* Messages area */}
      <div className="flex-1 overflow-y-auto rounded-xl border border-gray-200 bg-gray-50 p-4 space-y-4">
        {historyLoading ? (
          <div className="flex justify-center py-8">
            <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-indigo-600" />
          </div>
        ) : messages.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-400 text-lg">Start a conversation</p>
            <p className="text-gray-400 text-sm mt-1">
              {selectedSession
                ? `Ask anything about ${selectedSession.subject} — ${selectedSession.topic}`
                : 'Select a study session for context-aware responses'}
            </p>
          </div>
        ) : (
          messages.map((msg) => (
            <div
              key={msg.id}
              className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
            >
              <div
                className={`max-w-[80%] px-4 py-3 rounded-2xl text-sm leading-relaxed ${
                  msg.role === 'user'
                    ? 'bg-indigo-600 text-white rounded-br-md'
                    : 'bg-white border border-gray-200 text-gray-800 rounded-bl-md'
                }`}
              >
                <div className="whitespace-pre-wrap">{msg.content}</div>
                <div
                  className={`text-xs mt-1.5 ${
                    msg.role === 'user' ? 'text-indigo-200' : 'text-gray-400'
                  }`}
                >
                  {new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </div>
              </div>
            </div>
          ))
        )}

        {loading && (
          <div className="flex justify-start">
            <div className="bg-white border border-gray-200 px-4 py-3 rounded-2xl rounded-bl-md">
              <div className="flex space-x-1.5">
                <div className="h-2 w-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
                <div className="h-2 w-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
                <div className="h-2 w-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
              </div>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Input */}
      <form onSubmit={handleSend} className="mt-4 flex space-x-3">
        <input
          ref={inputRef}
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder={
            selectedSession
              ? `Ask about ${selectedSession.subject}...`
              : 'Type your message...'
          }
          disabled={loading}
          className="flex-1 px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none disabled:opacity-50 transition"
        />
        <button
          type="submit"
          disabled={loading || !input.trim()}
          className="px-5 py-3 bg-indigo-600 text-white rounded-xl hover:bg-indigo-700 disabled:opacity-50 transition"
        >
          <HiPaperAirplane className="h-5 w-5 transform rotate-90" />
        </button>
      </form>
    </div>
  );
}
