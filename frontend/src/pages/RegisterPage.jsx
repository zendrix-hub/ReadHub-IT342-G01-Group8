import React, { useEffect, useState } from 'react';
import Logo from '../components/Logo.jsx';

function RegisterPage({ onRegister, setPage, error, clearError }) {
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [localError, setLocalError] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        if (password !== confirmPassword) {
            setLocalError('Passwords do not match.');
            return;
        }
        onRegister(firstName, lastName, email, password);
    };

    useEffect(() => {
        setLocalError('');
        clearError();
    }, [firstName, lastName, email, password, confirmPassword, clearError]);

    return (
        <div className="min-h-screen bg-gray-100 flex items-center justify-center px-4 py-12">
            <div className="w-full max-w-5xl bg-white rounded-3xl shadow-2xl overflow-hidden grid grid-cols-1 lg:grid-cols-2">
                <div className="flex flex-col items-center justify-center px-10 py-14">
                    <div className="w-full max-w-md">
                    <div className="mb-8">
                        <Logo />
                    </div>
                    <h2 className="text-3xl font-bold text-gray-900 mb-2">Welcome</h2>
                    <p className="text-gray-500 mb-6">Enter your details to create an account.</p>

                    {(error || localError) && (
                        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">
                            <span className="block sm:inline">{error || localError}</span>
                        </div>
                    )}

                    <form onSubmit={handleSubmit}>
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2" htmlFor="firstName">First Name</label>
                                <input
                                    type="text"
                                    id="firstName"
                                    value={firstName}
                                    onChange={(e) => setFirstName(e.target.value)}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-red-500 focus:border-red-500"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2" htmlFor="lastName">Last Name</label>
                                <input
                                    type="text"
                                    id="lastName"
                                    value={lastName}
                                    onChange={(e) => setLastName(e.target.value)}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-red-500 focus:border-red-500"
                                    required
                                />
                            </div>
                        </div>

                        <div className="mt-4">
                            <label className="block text-sm font-medium text-gray-700 mb-2" htmlFor="email-reg">Email</label>
                            <input
                                type="email"
                                id="email-reg"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-red-500 focus:border-red-500"
                                required
                            />
                        </div>
                        <div className="mt-4">
                            <label className="block text-sm font-medium text-gray-700 mb-2" htmlFor="password-reg">Password</label>
                            <input
                                type="password"
                                id="password-reg"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-red-500 focus:border-red-500"
                                required
                            />
                        </div>
                        <div className="mt-4 mb-6">
                            <label className="block text-sm font-medium text-gray-700 mb-2" htmlFor="confirmPassword">Confirm Password</label>
                            <input
                                type="password"
                                id="confirmPassword"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-red-500 focus:border-red-500"
                                required
                            />
                        </div>

                        <button
                            type="submit"
                            className="w-full bg-red-800 text-white py-3 px-4 rounded-xl font-semibold shadow-lg shadow-red-800/30 hover:bg-red-900 transition duration-300"
                        >
                            Sign Up
                        </button>
                    </form>

                    <p className="text-center text-sm text-gray-500 mt-8">
                        Already have an account?{' '}
                        <a href="#" onClick={(e) => { e.preventDefault(); setPage('login'); }} className="font-medium text-red-600 hover:text-red-500">
                            Sign in
                        </a>
                    </p>
                </div>
                </div>

                <div
                    className="hidden lg:block bg-cover bg-center"
                    style={{ backgroundImage: "url('https://images.unsplash.com/photo-1544258210-2d85b14c023d?auto=format&fit=crop&w=1080&q=60')" }}
                ></div>
            </div>
        </div>
    );
}

export default RegisterPage;

