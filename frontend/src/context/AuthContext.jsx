import React, { createContext, useState, useEffect, useContext } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [user, setUser] = useState(null);
  // NEW: Add loading state to prevent premature redirects
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUser = async (currentToken) => {
      setLoading(true); // Start loading
      try {
        const base64Url = currentToken.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''));
        const decoded = JSON.parse(jsonPayload);
        
        let role = 'ROLE_STUDENT';
        if (decoded.roles && Array.isArray(decoded.roles)) {
           role = decoded.roles[0];
        } else if (decoded.authorities) {
           const auth = decoded.authorities[0];
           role = typeof auth === 'object' ? auth.authority : auth;
        }

        const res = await fetch('http://localhost:8080/api/users/me', {
          headers: { Authorization: `Bearer ${currentToken}` }
        });
        
        if (res.ok) {
          const profileData = await res.json();
          const p = profileData.data || profileData;
          setUser({
            email: decoded.sub,
            role: role,
            firstName: p.firstName,
            lastName: p.lastName,
            avatarUrl: p.avatarUrl 
          });
        } else {
          setUser({ email: decoded.sub, role: role });
        }

      } catch (e) {
        console.error("Auth Context Error", e);
        logout();
      } finally {
        setLoading(false); // Stop loading regardless of success/failure
      }
    };

    if (token) {
      fetchUser(token);
    } else {
      setUser(null);
      setLoading(false); // No token? We are done loading (guest mode)
    }
  }, [token]);

  const login = (newToken) => {
    localStorage.setItem('token', newToken);
    setToken(newToken);
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
  };

  const updateUser = (updates) => {
    setUser(prev => ({ ...prev, ...updates }));
  };

  // Expose loading state
  return (
    <AuthContext.Provider value={{ token, user, login, logout, updateUser, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);