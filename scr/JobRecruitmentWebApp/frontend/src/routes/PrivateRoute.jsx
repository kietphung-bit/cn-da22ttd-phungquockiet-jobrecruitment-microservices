import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { Loader2 } from 'lucide-react';

/**
 * PrivateRoute Component
 * Wrapper for protected routes that require authentication
 * 
 * Features:
 * - Checks if user is authenticated
 * - Saves current path for redirect after login
 * - Redirects to login if not authenticated
 * - Optional role-based access control
 * 
 * @param {Object} props
 * @param {React.ReactNode} props.children - Child components to render if authenticated
 * @param {string|Array<string>} props.allowedRoles - Optional role(s) required to access
 */
const PrivateRoute = ({ children, allowedRoles }) => {
  const { isAuthenticated, loading, user, saveRedirectPath } = useAuth();
  const location = useLocation();

  // Show loading spinner while checking auth state
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-neutral-50">
        <div className="text-center">
          <Loader2 className="w-12 h-12 text-primary animate-spin mx-auto mb-4" />
          <p className="text-neutral-600">Đang kiểm tra quyền truy cập...</p>
        </div>
      </div>
    );
  }

  // If not authenticated, save current path and redirect to login
  if (!isAuthenticated) {
    // Save the current path for redirect after login
    saveRedirectPath(location.pathname + location.search);
    
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // If allowedRoles is specified, check user role
  if (allowedRoles) {
    const roles = Array.isArray(allowedRoles) ? allowedRoles : [allowedRoles];
    
    if (!roles.includes(user?.role)) {
      // User doesn't have required role - show 403 Forbidden
      return (
        <div className="min-h-screen flex items-center justify-center bg-neutral-50">
          <div className="bg-white rounded-lg shadow-md p-8 max-w-md text-center">
            <div className="text-6xl mb-4">🚫</div>
            <h1 className="text-3xl font-bold text-neutral-900 mb-2">
              Truy Cập Bị Từ Chối
            </h1>
            <p className="text-neutral-600 mb-6">
              Bạn không có quyền truy cập vào trang này.
            </p>
            <button
              onClick={() => window.history.back()}
              className="px-6 py-3 bg-primary text-white rounded-lg hover:bg-primary-600"
            >
              Quay Lại
            </button>
          </div>
        </div>
      );
    }
  }

  // User is authenticated and has required role (if specified)
  return children;
};

export default PrivateRoute;
