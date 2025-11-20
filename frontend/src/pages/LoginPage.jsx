import React, { useEffect, useState } from 'react';
import Logo from '../components/Logo.jsx';

function LoginPage({ onLogin, setPage, error, clearError }) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        onLogin(email, password);
    };

    useEffect(() => {
        if (email || password) clearError();
    }, [email, password, clearError]);

    return (
        <div className="min-h-screen bg-gray-100 flex items-center justify-center px-4 py-12">
            <div className="w-full max-w-5xl bg-white rounded-3xl shadow-2xl overflow-hidden grid grid-cols-1 lg:grid-cols-2">
                <div className="flex flex-col items-center justify-center px-10 py-14">
                    <div className="w-full max-w-md">
                    <div className="mb-12">
                        <Logo />
                    </div>
                    <h2 className="text-3xl font-bold text-gray-900 mb-2">Welcome Back</h2>
                    <p className="text-gray-500 mb-8">Enter your details to proceed.</p>

                    {error && (
                        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">
                            <span className="block sm:inline">{error}</span>
                        </div>
                    )}

                    <form onSubmit={handleSubmit}>
                        <div className="mb-4">
                            <label className="block text-sm font-medium text-gray-700 mb-2" htmlFor="email">Email</label>
                            <input
                                type="email"
                                id="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-red-500 focus:border-red-500"
                                required
                            />
                        </div>
                        <div className="mb-4">
                            <label className="block text-sm font-medium text-gray-700 mb-2" htmlFor="password">Password</label>
                            <input
                                type="password"
                                id="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-red-500 focus:border-red-500"
                                required
                            />
                        </div>
                        <div className="flex items-center justify-between mb-8">
                            <div className="flex items-center">
                                <input id="remember-me" name="remember-me" type="checkbox" className="h-4 w-4 text-red-600 focus:ring-red-500 border-gray-300 rounded" />
                                <label htmlFor="remember-me" className="ml-2 block text-sm text-gray-900">Remember me</label>
                            </div>
                            <a href="#" className="text-sm font-medium text-red-600 hover:text-red-500">Forgot password?</a>
                        </div>

                        <button
                            type="submit"
                            className="w-full bg-red-800 text-white py-3 px-4 rounded-xl font-semibold shadow-lg shadow-red-800/30 hover:bg-red-900 transition duration-300"
                        >
                            Sign In
                        </button>
                    </form>

                    <p className="text-center text-sm text-gray-500 mt-8">
                        Don't have an account?{' '}
                        <a href="#" onClick={(e) => { e.preventDefault(); setPage('register'); }} className="font-medium text-red-600 hover:text-red-500">
                            Sign up
                        </a>
                    </p>
                </div>
                </div>

                <div
                    className="hidden lg:block bg-cover bg-center"
                    style={{ backgroundImage: "url('https://images.unsplash.com/photo-1560411326-61053077e01f?auto=format&fit=crop&w=1080&q=60')" }}
                ></div>
            </div>
        </div>
    );
}

export default LoginPage;

