import React, { useEffect, useState } from 'react';
import HeaderNav from '../components/HeaderNav.jsx';

function ProfilePage({ user, onUpdate, onDelete, setPage, onLogout, error, clearError }) {
    const [firstName, setFirstName] = useState(user?.firstName || '');
    const [lastName, setLastName] = useState(user?.lastName || '');
    const [email, setEmail] = useState(user?.email || '');
    const [confirmDelete, setConfirmDelete] = useState(false);

    useEffect(() => {
        if (user) {
            setFirstName(user.firstName);
            setLastName(user.lastName);
            setEmail(user.email);
        }
    }, [user]);

    useEffect(() => {
        clearError();
    }, [firstName, lastName, email, clearError]);

    const handleUpdate = (e) => {
        e.preventDefault();
        onUpdate(firstName, lastName, email);
    };

    const handleDelete = (e) => {
        e.preventDefault();
        if (confirmDelete) {
            onDelete();
        } else {
            setConfirmDelete(true);
        }
    };

    return (
        <div className="min-h-screen bg-red-950">
            <HeaderNav user={user} onLogout={onLogout} setPage={setPage} />

            <main className="max-w-7xl mx-auto p-4 sm:p-6 lg:p-8">
                <div className="max-w-md mx-auto bg-white rounded-lg shadow-xl p-8 mt-12">
                    <h2 className="text-3xl font-bold text-center text-gray-900 mb-6">My Profile</h2>

                    {error && (
                        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">
                            <span className="block sm:inline">{error}</span>
                        </div>
                    )}

                    <form onSubmit={handleUpdate}>
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2" htmlFor="prof-firstName">First Name</label>
                                <input
                                    type="text"
                                    id="prof-firstName"
                                    value={firstName}
                                    onChange={(e) => setFirstName(e.target.value)}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-red-500 focus:border-red-500"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2" htmlFor="prof-lastName">Last Name</label>
                                <input
                                    type="text"
                                    id="prof-lastName"
                                    value={lastName}
                                    onChange={(e) => setLastName(e.target.value)}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-red-500 focus:border-red-500"
                                    required
                                />
                            </div>
                        </div>

                        <div className="mt-4">
                            <label className="block text-sm font-medium text-gray-700 mb-2" htmlFor="prof-email">Email</label>
                            <input
                                type="email"
                                id="prof-email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-red-500 focus:border-red-500"
                                required
                            />
                        </div>

                        <button
                            type="submit"
                            className="mt-6 w-full bg-red-800 text-white py-2 px-4 rounded-md font-semibold hover:bg-red-900 transition duration-300"
                        >
                            Update Profile
                        </button>
                    </form>

                    <hr className="my-6" />

                    <div className="text-center">
                        <h3 className="text-lg font-medium text-gray-900">Danger Zone</h3>
                        <p className="text-sm text-gray-500 mb-4">This action cannot be undone.</p>
                        <button
                            onClick={handleDelete}
                            className={`w-full py-2 px-4 rounded-md font-semibold transition duration-300 ${
                                confirmDelete
                                    ? 'bg-red-600 text-white hover:bg-red-700'
                                    : 'bg-transparent text-red-600 border border-red-600 hover:bg-red-100'
                            }`}
                        >
                            {confirmDelete ? 'Are you sure? Click to confirm.' : 'Delete My Account'}
                        </button>
                    </div>

                    <div className="text-center mt-6">
                        <a href="#" onClick={(e) => { e.preventDefault(); setPage('dashboard'); }} className="text-sm font-medium text-gray-500 hover:text-gray-700">
                            &larr; Back to Dashboard
                        </a>
                    </div>
                </div>
            </main>
        </div>
    );
}

export default ProfilePage;

