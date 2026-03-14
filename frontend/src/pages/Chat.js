import React, { useEffect, useState, useRef } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import api from '../api';

const Chat = () => {
  const [searchParams] = useSearchParams();
  const [users, setUsers] = useState([]);
  const [unreadCounts, setUnreadCounts] = useState({});
  const [selectedUser, setSelectedUser] = useState(null);
  const [messages, setMessages] = useState([]);
  const [messageText, setMessageText] = useState('');
  const [loading, setLoading] = useState(true);
  const messagesEndRef = useRef(null);
  const currentUserId = Number(localStorage.getItem('userId'));

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const loadUnreadCounts = async () => {
    try {
      const res = await api.get('chat/unread-counts');
      const counts = {};
      res.data.forEach((item) => {
        counts[item.userId] = item.unreadCount;
      });
      setUnreadCounts(counts);
    } catch (err) {
      console.error('Failed to load unread counts', err);
    }
  };

  useEffect(() => {
    const loadUsers = async () => {
      try {
        setLoading(true);
        const res = await api.get('admin/users');
        setUsers(res.data.filter((u) => Number(u.id) !== currentUserId));
        await loadUnreadCounts();
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    loadUsers();
  }, [currentUserId]);

  useEffect(() => {
    const paramUser = Number(searchParams.get('user'));
    if (paramUser) {
      setSelectedUser(paramUser);
    }
  }, [searchParams]);

  useEffect(() => {
    if (!selectedUser) return;

    const loadConversation = async () => {
      try {
        const res = await api.get(`chat/history/${selectedUser}`);
        setMessages(res.data);

        // Mark messages as read for this conversation
        await api.put(`chat/mark-read/${selectedUser}`);
        setUnreadCounts((prev) => ({
          ...prev,
          [selectedUser]: 0,
        }));
      } catch (err) {
        console.error(err);
      }
    };

    loadConversation();
  }, [selectedUser]);

  const sendMessage = async () => {
    if (!selectedUser || !messageText.trim()) return;
    try {
      await api.post('chat/send', { receiverId: selectedUser, content: messageText.trim(), productId: null });
      setMessageText('');
      const res = await api.get(`chat/history/${selectedUser}`);
      setMessages(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const selectedUserData = users.find(u => u.id === selectedUser);

  return (
    <div className="h-[calc(100vh-8rem)] bg-gray-50">
      <div className="max-w-6xl mx-auto h-full bg-white rounded-lg shadow-lg overflow-hidden">
        <div className="flex h-full">
          {/* Users Sidebar */}
          <div className="w-80 bg-gray-50 border-r border-gray-200 flex flex-col">
            <div className="p-4 bg-white border-b border-gray-200">
              <h2 className="text-lg font-semibold text-gray-900">Messages</h2>
              <p className="text-sm text-gray-600">Start a conversation</p>
            </div>

            <div className="flex-1 overflow-y-auto">
              {loading ? (
                <div className="flex justify-center items-center py-8">
                  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                </div>
              ) : users.length === 0 ? (
                <div className="text-center py-8">
                  <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                  </svg>
                  <p className="mt-4 text-gray-500">No users available</p>
                </div>
              ) : (
                users.map((user) => (
                  <div
                    key={user.id}
                    onClick={() => setSelectedUser(user.id)}
                    className={`p-4 cursor-pointer hover:bg-gray-100 transition-colors border-b border-gray-100 ${
                      selectedUser === user.id ? 'bg-blue-50 border-r-2 border-r-blue-500' : ''
                    }`}
                  >
                    <div className="flex items-center space-x-3">
                      <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center text-white font-medium">
                        {user.name ? user.name.charAt(0).toUpperCase() : 'U'}
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2">
                          <p className="text-sm font-medium text-gray-900 truncate">{user.name}</p>
                          {unreadCounts[user.id] > 0 && (
                            <span className="inline-flex items-center justify-center h-5 min-w-[1.25rem] px-2 text-xs font-semibold text-white bg-red-600 rounded-full">
                              {unreadCounts[user.id]}
                            </span>
                          )}
                        </div>
                        <p className="text-xs text-gray-500 truncate">{user.email}</p>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Chat Area */}
          <div className="flex-1 flex flex-col">
            {selectedUser ? (
              <>
                {/* Chat Header */}
                <div className="p-4 bg-white border-b border-gray-200 flex items-center space-x-3">
                  <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center text-white font-medium">
                    {selectedUserData?.name ? selectedUserData.name.charAt(0).toUpperCase() : 'U'}
                  </div>
                  <div>
                    <h3 className="text-lg font-medium text-gray-900">{selectedUserData?.name || 'User'}</h3>
                    <p className="text-sm text-gray-600">{selectedUserData?.email}</p>
                  </div>
                </div>

                {/* Messages */}
                <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50">
                  {messages.length === 0 ? (
                    <div className="text-center py-8">
                      <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                      </svg>
                      <p className="mt-4 text-gray-500">No messages yet. Start the conversation!</p>
                    </div>
                  ) : (
                    messages.map((msg) => {
                      const isCurrentUser = Number(msg.senderId) === currentUserId;
                      return (
                        <div key={msg.id} className={`flex ${isCurrentUser ? 'justify-end' : 'justify-start'}`}>
                          <div className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${
                            isCurrentUser
                              ? 'bg-blue-500 text-white rounded-br-none'
                              : 'bg-white text-gray-900 rounded-bl-none shadow-sm'
                          }`}>
                            <p className="text-sm">{msg.content}</p>
                            <p className={`text-xs mt-1 ${isCurrentUser ? 'text-blue-100' : 'text-gray-500'}`}>
                              {new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </p>
                          </div>
                        </div>
                      );
                    })
                  )}
                  <div ref={messagesEndRef} />
                </div>

                {/* Message Input */}
                <div className="p-4 bg-white border-t border-gray-200">
                  <div className="flex space-x-3">
                    <div className="flex-1">
                      <textarea
                        value={messageText}
                        onChange={(e) => setMessageText(e.target.value)}
                        onKeyPress={handleKeyPress}
                        placeholder="Type a message..."
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                        rows="1"
                      />
                    </div>
                    <button
                      onClick={sendMessage}
                      disabled={!messageText.trim()}
                      className="px-6 py-2 bg-blue-500 hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed text-white rounded-lg font-medium transition duration-200 flex items-center space-x-2"
                    >
                      <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                      </svg>
                      <span>Send</span>
                    </button>
                  </div>
                </div>
              </>
            ) : (
              <div className="flex-1 flex items-center justify-center bg-gray-50">
                <div className="text-center">
                  <svg className="mx-auto h-24 w-24 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                  </svg>
                  <h3 className="mt-4 text-lg font-medium text-gray-900">Select a conversation</h3>
                  <p className="mt-2 text-gray-500">Choose a user from the sidebar to start chatting</p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Chat;
