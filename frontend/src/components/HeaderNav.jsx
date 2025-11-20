import React, { useState } from 'react';
import Logo from './Logo.jsx';

function HeaderNav({ user, onLogout, setPage }) {
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const firstInitial = user?.firstName?.[0]?.toUpperCase() ?? '';
    const lastInitial = user?.lastName?.[0]?.toUpperCase() ?? '';
    const userInitials = (firstInitial + lastInitial) || '...';

    return (
        <nav className="bg-white shadow-md w-full">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between h-16">
                    <div className="flex-shrink-0 flex items-center">
                        <Logo />
                    </div>

                    <div className="flex-1 flex justify-center px-2 lg:ml-6 lg:justify-end">
                        <div className="max-w-lg w-full lg:max-w-xs">
                            <label htmlFor="search" className="sr-only">Search</label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                    <svg className="h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                                        <path fillRule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clipRule="evenodd" />
                                    </svg>
                                </div>
                                <input id="search" name="search" className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:ring-red-500 focus:border-red-500 focus:placeholder-gray-400 sm:text-sm" placeholder="Search..." type="search" />
                            </div>
                        </div>
                    </div>

                    <div className="hidden sm:ml-6 sm:flex sm:items-center">
                        <div className="ml-3 relative">
                            <div>
                                <button onClick={() => setDropdownOpen(!dropdownOpen)} type="button" className="bg-white rounded-full flex items-center text-sm focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500" id="user-menu-button">
                                    <span className="sr-only">Open user menu</span>
                                    <div className="h-10 w-10 rounded-full bg-red-800 text-white flex items-center justify-center font-bold">
                                        {userInitials}
                                    </div>
                                    <span className="hidden lg:block ml-2 text-gray-700 text-sm font-medium">Hi, {user?.firstName}</span>
                                    <svg className="hidden lg:block ml-1 h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                                        <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                                    </svg>
                                </button>
                            </div>

                            {dropdownOpen && (
                                <div
                                    className="origin-top-right absolute right-0 mt-2 w-48 rounded-md shadow-lg py-1 bg-white ring-1 ring-black ring-opacity-5 focus:outline-none"
                                    onClick={() => setDropdownOpen(false)}
                                >
                                    <a href="#" onClick={(e) => { e.preventDefault(); setPage('profile'); }} className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">My Profile</a>
                                    <a href="#" onClick={(e) => { e.preventDefault(); onLogout(); }} className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">Sign out</a>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </nav>
    );
}

export default HeaderNav;

