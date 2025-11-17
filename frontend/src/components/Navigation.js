import React from 'react';
import { Link, useLocation } from 'react-router-dom';

const Navigation = () => {
  const location = useLocation();
  const isAuthenticated = localStorage.getItem('access_token');

  const handleLogout = () => {
    localStorage.removeItem('access_token');
    window.location.href = '/';
  };

  return (
    <nav className="bg-gradient-to-r from-blue-600 to-blue-800 shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center py-4">
          <div className="flex items-center space-x-4">
            <h1 className="text-white text-2xl font-bold">Stock Market</h1>
            {isAuthenticated && (
              <div className="flex space-x-4 ml-8">
                <Link
                  to="/stocks"
                  className={`text-white hover:bg-blue-700 px-4 py-2 rounded-lg transition duration-300 ${
                    location.pathname === '/stocks' ? 'bg-blue-700' : ''
                  }`}
                >
                  Stocks
                </Link>
              </div>
            )}
          </div>
          <div>
            {isAuthenticated ? (
              <button
                onClick={handleLogout}
                className="bg-red-500 hover:bg-red-600 text-white font-semibold py-2 px-6 rounded-lg transition duration-300"
              >
                Logout
              </button>
            ) : (
              <Link
                to="/"
                className="bg-green-500 hover:bg-green-600 text-white font-semibold py-2 px-6 rounded-lg transition duration-300"
              >
                Login
              </Link>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navigation;
