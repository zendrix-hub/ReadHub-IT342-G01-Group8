import React from 'react';
import HeaderNav from '../components/HeaderNav.jsx';

function DashboardPage({ user, onLogout, setPage }) {
    return (
        <div className="min-h-screen bg-red-950">
            <HeaderNav user={user} onLogout={onLogout} setPage={setPage} />

            <main className="max-w-7xl mx-auto p-4 sm:p-6 lg:p-8">
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-white">Welcome back, {user?.firstName}!</h1>
                    <p className="text-red-200">Here's what's happening with your library activity.</p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                    <div className="bg-yellow-400 p-6 rounded-lg shadow-md">
                        <h3 className="text-4xl font-bold text-red-900">3</h3>
                        <p className="text-red-800 font-medium">Items Currently Borrowed</p>
                    </div>
                    <div className="bg-yellow-400 p-6 rounded-lg shadow-md">
                        <h3 className="text-4xl font-bold text-red-900">-2 days</h3>
                        <p className="text-red-800 font-medium">Next Due: "To Kill A Mockingbird"</p>
                    </div>
                    <div className="bg-yellow-400 p-6 rounded-lg shadow-md">
                        <h3 className="text-4xl font-bold text-red-900">2</h3>
                        <p className="text-red-800 font-medium">Pending Requests</p>
                    </div>
                </div>

                <div className="bg-red-800 text-white p-4 rounded-lg flex justify-between items-center mb-8">
                    <p><span className="font-bold">You have 1 overdue item!</span> Please return these items as soon as possible to avoid penalties.</p>
                    <button className="bg-white text-red-800 font-semibold px-4 py-2 rounded-md text-sm hover:bg-gray-200">
                        View Overdue
                    </button>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                    <div className="bg-white p-6 rounded-lg shadow-md">
                        <h2 className="text-xl font-bold text-gray-900 mb-4">My Loans (3)</h2>
                        <div className="space-y-4">
                            <div className="border p-4 rounded-md flex justify-between items-center">
                                <div>
                                    <h4 className="font-semibold">To Kill A Mockingbird</h4>
                                    <p className="text-sm text-gray-500">Lander: Candon University Library</p>
                                </div>
                                <span className="text-sm font-medium text-red-600">Due: 11/11/2025</span>
                            </div>
                            <div className="border p-4 rounded-md flex justify-between items-center">
                                <div>
                                    <h4 className="font-semibold">The Great Gatsby</h4>
                                    <p className="text-sm text-gray-500">Lander: Sarah Johnson</p>
                                </div>
                                <span className="text-sm font-medium text-gray-600">Due: 11/21/2025</span>
                            </div>
                        </div>
                    </div>
                    <div className="bg-white p-6 rounded-lg shadow-md">
                        <h2 className="text-xl font-bold text-gray-900 mb-4">My Requests (2)</h2>
                        <div className="border p-4 rounded-md text-center text-gray-500">
                            <p>Requests feature coming soon.</p>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}

export default DashboardPage;

