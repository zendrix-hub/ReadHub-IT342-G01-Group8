import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { BookOpen, MoreHorizontal, Eye, EyeOff, Sun, Moon, AlertCircle, CheckCircle, X } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { useToast } from '../context/ToastContext';
import '../styles/login.scss';

const Login = () => {
  // Logic State
  const [role, setRole] = useState('STUDENT'); 
  const [mode, setMode] = useState('LOGIN');   
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false); // New toggle for confirm
  const { isDarkMode, toggleTheme } = useTheme();
  const { showToast } = useToast();
  
  const [notification, setNotification] = useState(null); 

  // Updated Form State to match the Image
 const [form, setForm] = useState({ 
  firstName: '',
  lastName: '',
  studentId: '',
  email: '', 
  password: '',
  confirmPassword: ''
});

  const { login } = useAuth();
  const navigate = useNavigate();

  // Reset form when component mounts
  useEffect(() => {
    setRole('STUDENT');
    setMode('LOGIN');
    setForm({ fullName: '', studentId: '', email: '', password: '', confirmPassword: '' });
  }, []);

  const parseJwt = (token) => {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''));
      return JSON.parse(jsonPayload);
    } catch (e) {
      return null;
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // --- VALIDATION: Confirm Password ---
    if (mode === 'REGISTER' && form.password !== form.confirmPassword) {
      showToast('Passwords do not match.', 'error');
      return;
    }

    // --- VALIDATION: Password length ---
  if (mode === 'REGISTER' && form.password.length < 8) {
  showToast('Password must be at least 8 characters long.', 'error');
  return;
  }

    const endpoint = mode === 'LOGIN' ? '/auth/login' : '/auth/register';
    
    // --- LOGIC: Split Full Name for Backend ---
    let requestBody = {};
    
    if (mode === 'LOGIN') {
      requestBody = { email: form.email, password: form.password };
    } else {
   requestBody = { 
      firstName: form.firstName,
      lastName: form.lastName,
      email: form.email,
      password: form.password
  };

    }

    try {
      const res = await fetch(`http://localhost:8080/api${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody)
      });
      
      const data = await res.json();
      const token = data.success ? data.data?.token : data.token;
      
      if (token) {
        login(token);
        const payload = parseJwt(token);
        
        let userRole = 'ROLE_STUDENT';
        if (payload?.roles && Array.isArray(payload.roles)) userRole = payload.roles[0];

        // Role Checks
        if (role === 'ADMIN' && userRole !== 'ROLE_ADMIN') {
          showToast('Access Denied: Not an Admin account.', 'error');
          return;
        }
        if (role === 'STUDENT' && userRole === 'ROLE_ADMIN') {
          showToast('Access Denied: Please use Admin Login.', 'error');
          return;
        }

        navigate(userRole === 'ROLE_ADMIN' ? '/admin/dashboard' : '/student/dashboard');
      } else {
        const errorMsg = data.message || (mode === 'LOGIN' ? 'Invalid Credentials' : 'Registration Failed');
        showToast(errorMsg, 'error');
      }
    } catch (err) {
      showToast('Network Error: Unable to connect.', 'error');
    }
  };

  const handleTabSwitch = (newRole) => {
    setRole(newRole);
    setMode('LOGIN');
    setForm({ firstName: '', lastName: '', studentId: '', email: '', password: '', confirmPassword: '' });
  };

  return (
    <div className="login-container">
      <div className="bg-blobs">
        <div className="blob blob-1"></div>
        <div className="blob blob-2"></div>
        <div className="blob blob-3"></div>
      </div>

      {notification && (
        <div className={`login-toast ${notification.type}`}>
          {notification.type === 'success' ? <CheckCircle size={20} /> : <AlertCircle size={20} />}
          <span>{notification.message}</span>
          <button onClick={() => setNotification(null)} style={{ background: 'none', border: 'none', cursor: 'pointer', marginLeft: 'auto', color: 'inherit' }}><X size={16} /></button>
        </div>
      )}

      <div className="theme-toggle-wrapper">
        <button onClick={toggleTheme} className="theme-btn" aria-label="Toggle Dark Mode">
          {isDarkMode ? <Sun size={20} /> : <Moon size={20} />}
        </button>
      </div>

      <div className="brand-header">
        <div className="logo-circle"><BookOpen size={32} strokeWidth={2} /></div>
        <h1 className="uni-name">Cebu Institute of Technology University</h1>
        <h2 className="app-name">ReadHub Book Management System</h2>
        <p className="tagline">Share resources, build community</p>
      </div>

      <div className="role-toggle">
        <button className={`role-btn ${role === 'STUDENT' ? 'active' : ''}`} onClick={() => handleTabSwitch('STUDENT')}>Student Login</button>
        <button className={`role-btn ${role === 'ADMIN' ? 'active' : ''}`} onClick={() => handleTabSwitch('ADMIN')}>Admin Login</button>
      </div>

      <div className="login-card">
        <div className="card-header">
          <h3 className="portal-title">{role === 'STUDENT' ? 'Student Portal' : 'Admin Portal'}</h3>
          <p className="portal-desc">
            {role === 'STUDENT' ? 'Login or register to access the book management system' : 'Secure access for library administrators and staff'}
          </p>
        </div>

        {role === 'STUDENT' && (
          <div className="auth-toggle">
            <button className={`auth-btn ${mode === 'LOGIN' ? 'active' : ''}`} onClick={() => setMode('LOGIN')}>Login</button>
            <button className={`auth-btn ${mode === 'REGISTER' ? 'active' : ''}`} onClick={() => setMode('REGISTER')}>Register</button>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          
          {/* --- REGISTER FIELDS --- */}
          {mode === 'REGISTER' && (
            <>
              {/* First Name */}
<div className="input-group">
  <label className="label">First Name</label>
  <div className="input-wrapper">
    <input
      className="custom-input"
      placeholder="Juan"
      required
      value={form.firstName}
      onChange={e => setForm({ ...form, firstName: e.target.value })}
    />
  </div>
</div>

{/* Last Name */}
<div className="input-group">
  <label className="label">Last Name</label>
  <div className="input-wrapper">
    <input
      className="custom-input"
      placeholder="Dela Cruz"
      required
      value={form.lastName}
      onChange={e => setForm({ ...form, lastName: e.target.value })}
    />
  </div>
</div>


              {/* Student ID */}
              <div className="input-group">
                <label className="label">Student ID</label>
                <div className="input-wrapper">
                  <input 
                    className="custom-input" 
                    placeholder="20-1001-123"
                    required 
                    value={form.studentId}
                    onChange={e => setForm({...form, studentId: e.target.value})}
                  />
                </div>
              </div>
            </>
          )}

          {/* --- COMMON FIELDS --- */}
          <div className="input-group">
            <label className="label">{role === 'STUDENT' ? 'CIT-U Email' : 'Email Address'}</label>
            <div className="input-wrapper">
              <input 
                type="email" 
                className="custom-input" 
                placeholder={role === 'STUDENT' ? "student@citu.edu" : "admin@cit.edu"}
                required
                value={form.email}
                onChange={e => setForm({...form, email: e.target.value})}
              />
            </div>
          </div>

          <div className="input-group">
            <label className="label">Password</label>
            <div className="input-wrapper">
              <input 
                type={showPassword ? "text" : "password"} 
                className="custom-input" 
                placeholder="••••••••"
                required
                value={form.password}
                onChange={e => setForm({...form, password: e.target.value})}
              />
              <button type="button" className="password-toggle" onClick={() => setShowPassword(!showPassword)}>
                {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
              </button>
            </div>
          </div>

          {/* Confirm Password (Register Only) */}
         {mode === 'REGISTER' && (
  <div className="input-group">
    <label className="label">Confirm Password</label>
    <div className="input-wrapper">
      <input 
        type={showConfirmPassword ? "text" : "password"} 
        className="custom-input" 
        placeholder="••••••••"
        required
        minLength={8}   // <--- ADD THIS
        value={form.confirmPassword}
        onChange={e => setForm({...form, confirmPassword: e.target.value})}
      />
      <button 
        type="button" 
        className="password-toggle" 
        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
      >
        {showConfirmPassword ? <EyeOff size={20} /> : <Eye size={20} />}
      </button>
    </div>
  </div>
)}
          <button type="submit" className="submit-btn">
            {mode === 'LOGIN' ? 'Login' : 'Register'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default Login;
