import React, { useState, useEffect, useRef, useCallback } from 'react';
import { BookOpen, LogOut, Bell, CheckCircle2, XCircle, Clock, Info, Sun, Moon, X } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { useTheme } from '../../context/ThemeContext';
import '../../styles/dashboard.scss';

const DashboardLayout = ({ children }) => {
  const { user, logout, token } = useAuth();
  const { isDarkMode, toggleTheme } = useTheme();
  const navigate = useNavigate();
  
  const [notifications, setNotifications] = useState([]);
  const [showNotifications, setShowNotifications] = useState(false);
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false); // <- NEW
  const [selectedNotification, setSelectedNotification] = useState(null);

  // --- CLOCK STATE ---
  const [currentTime, setCurrentTime] = useState(new Date());
  
  const notificationRef = useRef(null);

  // --- CLOCK TIMER ---
  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  // --- FETCH NOTIFICATIONS ---
 useEffect(() => {
  if (!token) return;

  const fetchNotifications = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/notifications', {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        setNotifications(data.data || data);
      }
    } catch (err) { console.error(err); }
  };

  fetchNotifications(); // initial fetch

  const interval = setInterval(() => {
    fetchNotifications(); // auto-refresh every 5 seconds
  }, 5000);

  return () => clearInterval(interval); // cleanup on unmount
}, [token]);


  const markAsRead = useCallback(async () => {
    const hasUnread = notifications.some(n => !n.isRead);
    if (!hasUnread) return;

    setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));

    try {
      await fetch('http://localhost:8080/api/notifications/read', {
        method: 'PUT',
        headers: { Authorization: `Bearer ${token}` }
      });
    } catch (err) { console.error("Failed to mark read", err); }
  }, [notifications, token]);

  // --- HANDLE CLICK OUTSIDE ---
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (notificationRef.current && !notificationRef.current.contains(event.target)) {
        if (showNotifications) {
          setShowNotifications(false);
          markAsRead();
        }
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [notificationRef, showNotifications, markAsRead]);

  const toggleNotifications = (e) => {
    e.stopPropagation();
    if (showNotifications) {
      setShowNotifications(false);
      markAsRead();
    } else {
      setShowNotifications(true);
    }
  };

  // --- LOGOUT WITH CONFIRMATION ---
  const handleLogoutClick = () => setShowLogoutConfirm(true);
  const confirmLogout = () => { logout(); navigate('/login'); setShowLogoutConfirm(false); };
  const cancelLogout = () => setShowLogoutConfirm(false);

  const getNotificationStyle = (msg) => {
    const lower = msg.toLowerCase();
    if (lower.includes('approved') || lower.includes('successfully')) return { icon: <CheckCircle2 size={18} />, type: 'success' };
    if (lower.includes('rejected') || lower.includes('overdue')) return { icon: <XCircle size={18} />, type: 'error' };
    if (lower.includes('new request')) return { icon: <Clock size={18} />, type: 'pending' };
    return { icon: <Info size={18} />, type: 'info' };
  };

  const unreadCount = notifications.filter(n => !n.isRead).length;

  const formatTime = (date) => date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  const formatDate = (date) => date.toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric' });

  return (
    <div className="dashboard-container">
      <nav className="navbar">
        <div className="nav-container" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
          
          {/* BRAND LOGO */}
          <div 
            className="brand-section" 
            onClick={() => navigate(user?.role === 'ROLE_ADMIN' ? '/admin/dashboard' : '/student/dashboard')} 
            style={{ cursor: 'pointer' }}
          >
            <div className="brand-logo-box"><BookOpen size={24} strokeWidth={2} /></div>
            <div className="brand-info">
              <h1>ReadHub</h1>
              <p>Book Management</p>
            </div>
          </div>

          <div className="user-menu">
            <div className="system-clock">
              <span className="clock-time">{formatTime(currentTime)}</span>
              <span className="clock-date">{formatDate(currentTime)}</span>
            </div>

            <div className="menu-divider"></div>

            {/* THEME TOGGLE */}
            <button onClick={toggleTheme} className="notification-btn" title="Toggle Theme" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              {isDarkMode ? <Sun size={20} /> : <Moon size={20} />}
            </button>

            <div className="menu-divider"></div>

            {/* NOTIFICATIONS */}
            <div className="notification-wrapper" ref={notificationRef}>
              <button className={`notification-btn ${showNotifications ? 'active' : ''}`} onClick={toggleNotifications}>
                <Bell size={20} />
                {unreadCount > 0 && <span className="notification-badge">{unreadCount}</span>}
              </button>
              {showNotifications && (
                <div className="notification-dropdown">
                  <div className="dropdown-header">
                    <h3>Notifications</h3>
                    <span className="badge-count">{notifications.length} New</span>
                  </div>
                  <div className="notification-list">
                    {notifications.length > 0 ? (
                      notifications.map(note => {
                        const style = getNotificationStyle(note.message);
                        return (
                          <div 
                            key={note.notificationId} 
                            className={`notif-item ${style.type} ${!note.isRead ? 'unread-item' : ''}`}
                            onClick={() => {
                              setSelectedNotification(note);
                              setNotifications(prev => prev.map(n => n.notificationId === note.notificationId ? { ...n, isRead: true } : n));
                            }}
                            style={{ cursor: 'pointer' }}
                          >
                            <div className="notif-icon-box">{style.icon}</div>
                            <div className="notif-content">
                              <p className="notif-msg">{note.message}</p>
                              <span className="notif-date">{new Date(note.sentDate).toLocaleDateString()} • {new Date(note.sentDate).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})}</span>
                            </div>
                            {!note.isRead && <div className="dot-unread"></div>}
                          </div>
                        );
                      })
                    ) : (
                      <div className="empty-notif">
                        <Bell size={32} style={{ opacity: 0.2, marginBottom: 8 }}/>
                        <p>No new notifications</p>
                      </div>
                    )}
                  </div>

          {/*---- TESTING NI KANANG MA CLICK ANG NOTIF PARA MO READ BUT PROBLEM ANI ANI KAY IG REFRESH MO BALIK SIYA UNREAD ----*/}
                  {/*    
              <div className="notification-list">
  {notifications.length > 0 ? (
    notifications.map(note => {
      const style = getNotificationStyle(note.message);
      return (
        <button
          key={note.notificationId}
          className={`notif-item ${style.type} ${!note.isRead ? 'unread-item' : ''}`}
          onClick={() => {
            // Example action: navigate to transaction or mark as read
            console.log('Notification clicked:', note);
            // Optional: mark as read immediately
            setNotifications(prev =>
              prev.map(n =>
                n.notificationId === note.notificationId ? { ...n, isRead: true } : n
              )
            );
          }}
          style={{
            display: 'flex',
            width: '100%',
            border: 'none',
            background: 'transparent',
            cursor: 'pointer',
            textAlign: 'left',
            padding: '8px 0',
          }}
        >
          <div className="notif-icon-box">{style.icon}</div>
          <div className="notif-content">
            <p className="notif-msg">{note.message}</p>
            <span className="notif-date">
              {new Date(note.sentDate).toLocaleDateString()} •{' '}
              {new Date(note.sentDate).toLocaleTimeString([], { hour:'2-digit', minute:'2-digit' })}
            </span>
          </div>
          {!note.isRead && <div className="dot-unread"></div>}
        </button>
      );
    })
  ) : (
    <div className="empty-notif">
      <Bell size={32} style={{ opacity: 0.2, marginBottom: 8 }}/>
      <p>No new notifications</p>
    </div>
  )}
</div>
*/}
                </div>
              )}
            </div>

            <div className="menu-divider"></div>

            {/* PROFILE BUTTON */}
            <button className="user-profile-btn" onClick={() => navigate('/profile')} title="View Profile">
              <div className="user-avatar" style={user?.avatarUrl ? { backgroundImage: `url(${user.avatarUrl})`, backgroundSize: 'cover' } : {}}>
                {!user?.avatarUrl && (user?.firstName?.charAt(0) || 'U')}
              </div>
              <div className="user-info">
                <span className="user-name">{user?.firstName || 'User'}</span>
                <span className="user-role">{user?.role === 'ROLE_ADMIN' ? 'Admin' : 'Student'}</span>
              </div>
            </button>

            <button onClick={handleLogoutClick} className="logout-btn" title="Logout"><LogOut size={16} /></button>
          </div>
        </div>
      </nav>

      <main className="container" style={{ paddingBottom: '60px' }}>{children}</main>

      {/* --- LOGOUT CONFIRMATION MODAL --- */}
      {showLogoutConfirm && (
        <div className="confirmation-overlay">
          <div className="confirmation-box">
            <h2 className="confirm-title">Confirm Logout</h2>
            <p className="confirm-message">Are you sure you want to log out?</p>
            <div className="confirm-actions">
              <button className="btn-yes-conf" onClick={confirmLogout}>Yes, Logout</button>
              <button className="btn-cancel-conf" onClick={cancelLogout}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {/* --- NOTIFICATION DETAILS MODAL --- */}
      {selectedNotification && (
        <div className="modal-overlay" onClick={() => setSelectedNotification(null)} style={{ zIndex: 9999 }}>
          <div className="modal-container" onClick={(e) => e.stopPropagation()} style={{ width: '450px' }}>
            <div className="modal-header">
              <h2 style={{ fontSize: '18px', fontWeight: '700', color: 'var(--text-main)' }}>Notification Details</h2>
              <button onClick={() => setSelectedNotification(null)} className="btn-close"><X size={20} /></button>
            </div>
            <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                <div style={{ width: '40px', height: '40px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--beige-bg)', color: 'var(--maroon)' }}>
                  {getNotificationStyle(selectedNotification.message).icon}
                </div>
                <div>
                  <p style={{ margin: 0, fontWeight: '600', color: 'var(--text-main)' }}>Activity Update</p>
                  <span style={{ fontSize: '12px', color: 'var(--text-sub)' }}>
                    {new Date(selectedNotification.sentDate).toLocaleString()}
                  </span>
                </div>
              </div>
              <p style={{ 
                fontSize: '14px', 
                color: 'var(--text-main)', 
                lineHeight: '1.6', 
                background: 'var(--bg-page)', 
                padding: '16px', 
                borderRadius: '8px', 
                border: '1px solid var(--border-light)', 
                margin: 0 
              }}>
                {selectedNotification.message}
              </p>
            </div>
            <div className="modal-footer" style={{ borderTop: '1px solid var(--border-light)', background: 'var(--bg-page)' }}>
              <button onClick={() => setSelectedNotification(null)} className="btn-confirm" style={{ width: '100%' }}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DashboardLayout;
