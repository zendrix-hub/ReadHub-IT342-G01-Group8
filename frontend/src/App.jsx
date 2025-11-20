import React, { Fragment, Suspense, lazy, useCallback, useEffect, useState } from 'react';
import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_URL,
});

const LoginPage = lazy(() => import('./pages/LoginPage.jsx'));
const RegisterPage = lazy(() => import('./pages/RegisterPage.jsx'));
const DashboardPage = lazy(() => import('./pages/DashboardPage.jsx'));
const ProfilePage = lazy(() => import('./pages/ProfilePage.jsx'));

const PageFallback = () => (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <div className="text-2xl font-semibold">Loading...</div>
    </div>
);

function App() {
    const [page, setPage] = useState('login');
    const [token, setToken] = useState(null);
    const [user, setUser] = useState(null);
    const [error, setError] = useState(null);

    const clearError = useCallback(() => setError(null), []);

    const handleLogout = useCallback(() => {
        setToken(null);
        setUser(null);
        localStorage.removeItem('readhubToken');
        delete api.defaults.headers.common.Authorization;
        setPage('login');
    }, []);

    const getProfile = useCallback(async (currentToken) => {
        try {
            const response = await api.get('/users/profile', {
                headers: {
                    Authorization: `Bearer ${currentToken}`,
                },
            });
            setUser(response.data);
            setPage('dashboard');
        } catch (err) {
            console.error('Profile fetch failed:', err);
            handleLogout();
        }
    }, [handleLogout]);

    useEffect(() => {
        const storedToken = localStorage.getItem('readhubToken');
        if (storedToken) {
            setToken(storedToken);
            api.defaults.headers.common.Authorization = `Bearer ${storedToken}`;
            getProfile(storedToken);
        }
    }, [getProfile]);

    const handleLogin = async (email, password) => {
        try {
            const response = await api.post('/auth/login', { email, password });
            const newToken = response.data.token;

            setToken(newToken);
            localStorage.setItem('readhubToken', newToken);
            api.defaults.headers.common.Authorization = `Bearer ${newToken}`;

            await getProfile(newToken);
            clearError();
        } catch (err) {
            console.error('Login error:', err);
            setError(err.response?.data?.message || 'Invalid email or password. Please try again.');
        }
    };

    const handleRegister = async (firstName, lastName, email, password) => {
        try {
            await api.post('/auth/register', { firstName, lastName, email, password });
            clearError();
            alert('Registration successful! Please log in.');
            setPage('login');
        } catch (err) {
            console.error('Registration error:', err);
            setError(err.response?.data || 'Registration failed. Please try again.');
        }
    };

    const handleUpdateProfile = async (firstName, lastName, email) => {
        try {
            const response = await api.put('/users/profile', {
                userId: user.userId,
                firstName,
                lastName,
                email,
            });
            setUser(response.data);
            clearError();
            alert('Profile updated successfully!');
        } catch (err) {
            console.error('Update profile error:', err);
            setError(err.response?.data || 'Failed to update profile.');
        }
    };

    const handleDeleteProfile = async () => {
        try {
            await api.delete('/users/profile');
            alert('Account deleted successfully.');
            handleLogout();
        } catch (err) {
            console.error('Delete profile error:', err);
            setError(err.response?.data || 'Failed to delete account.');
        }
    };

    const renderPage = () => {
        if (token && !user) {
            return <PageFallback />;
        }

        switch (page) {
            case 'login':
                return <LoginPage onLogin={handleLogin} setPage={setPage} error={error} clearError={clearError} />;
            case 'register':
                return <RegisterPage onRegister={handleRegister} setPage={setPage} error={error} clearError={clearError} />;
            case 'dashboard':
                return <DashboardPage user={user} onLogout={handleLogout} setPage={setPage} />;
            case 'profile':
                return (
                    <ProfilePage
                        user={user}
                        onUpdate={handleUpdateProfile}
                        onDelete={handleDeleteProfile}
                        setPage={setPage}
                        onLogout={handleLogout}
                        error={error}
                        clearError={clearError}
                    />
                );
            default:
                return <LoginPage onLogin={handleLogin} setPage={setPage} error={error} clearError={clearError} />;
        }
    };

    return (
        <Fragment>
            <Suspense fallback={<PageFallback />}>
                {renderPage()}
            </Suspense>
        </Fragment>
    );
}

export default App;