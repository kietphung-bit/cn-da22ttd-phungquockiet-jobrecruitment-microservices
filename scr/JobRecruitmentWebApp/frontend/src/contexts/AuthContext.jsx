import React, { createContext, useState, useContext, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

/**
 * AuthContext
 * Manages authentication state and provides auth-related functions
 * 
 * Features:
 * - Login/Logout functionality
 * - Token management in localStorage
 * - User data persistence
 * - Smart redirect (return to original page after login)
 * - Role-based access control
 */

// Create Context
const AuthContext = createContext(null);

// Storage keys
const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';
const REDIRECT_KEY = 'auth_redirect';

/**
 * AuthProvider Component
 * Wraps the app and provides authentication context
 */
export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const location = useLocation();

  // Initialize auth state from localStorage on mount
  useEffect(() => {
    const initAuth = () => {
      try {
        const storedToken = localStorage.getItem(TOKEN_KEY);
        const storedUser = localStorage.getItem(USER_KEY);

        if (storedToken && storedUser) {
          setToken(storedToken);
          setUser(JSON.parse(storedUser));
          setIsAuthenticated(true);
        }
      } catch (error) {
        console.error('Error initializing auth:', error);
        // Clear corrupted data
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
      } finally {
        setLoading(false);
      }
    };

    initAuth();
  }, []);

  /**
   * Save the current path for redirect after login
   * @param {string} path - Path to redirect to after login
   */
  const saveRedirectPath = (path) => {
    // Don't save auth pages as redirect paths
    const excludedPaths = ['/login', '/register', '/logout'];
    if (!excludedPaths.includes(path)) {
      localStorage.setItem(REDIRECT_KEY, path);
    }
  };

  /**
   * Get and clear the saved redirect path
   * @returns {string} Saved path or default '/'
   */
  const getRedirectPath = () => {
    const path = localStorage.getItem(REDIRECT_KEY);
    localStorage.removeItem(REDIRECT_KEY);
    return path || '/';
  };

  /**
   * Login function with smart redirect
   * @param {string} authToken - JWT token from backend
   * @param {string} role - User role (ADM, DN, UV)
   * @param {Object} userData - User profile data
   * @param {string} from - Path to redirect to after login (optional)
   */
  const login = (authToken, role, userData, from = null) => {
    try {
      // Save to localStorage
      localStorage.setItem(TOKEN_KEY, authToken);
      localStorage.setItem(USER_KEY, JSON.stringify(userData));

      // Update state
      setToken(authToken);
      setUser({ ...userData, role });
      setIsAuthenticated(true);

      // Smart redirect logic:
      // 1. If 'from' parameter is provided, use it (user came from a specific page)
      // 2. Otherwise, redirect to role-based default dashboard
      if(role === 'ADM') {
        navigate('/admin/dashboard');
      }
      else if(role === 'DN') {
        navigate('/employer/dashboard');
      }
      else if (from && from !== '/login' && from !== '/register') {
        // Redirect back to the page user came from
        navigate(from);
      }  
      else {
        // Redirect to role-specific default dashboard
        switch (role) {
          case 'ADM': // Admin
            navigate('/admin/dashboard');
            break;
          case 'DN': // Employer (Doanh Nghiệp)
            navigate('/employer/dashboard');
            break;
          case 'UV': // Candidate (Ứng Viên)
            navigate('/candidate/profile');
            break;
          default:
            navigate('/');
        }
      }

      return true;
    } catch (error) {
      console.error('Login error:', error);
      return false;
    }
  };

  /**
   * Logout function
   * Clears all auth data and always redirects to home page
   * 
   * Strategy:
   * - Clear all localStorage (token, user, redirect path)
   * - Reset authentication state
   * - Redirect to home (/) NOT login page
   */
  const logout = () => {
    try {
      // Clear localStorage
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      localStorage.removeItem(REDIRECT_KEY);

      // Reset state
      setToken(null);
      setUser(null);
      setIsAuthenticated(false);

      // Always redirect to home page (/) after logout
      navigate('/', { replace: true });
    } catch (error) {
      console.error('Logout error:', error);
    }
  };

  /**
   * Check if user has specific role
   * @param {string} requiredRole - Role to check (ADM, DN, UV)
   * @returns {boolean}
   */
  const hasRole = (requiredRole) => {
    return user?.role === requiredRole;
  };

  /**
   * Check if user has any of the specified roles
   * @param {Array<string>} roles - Array of roles to check
   * @returns {boolean}
   */
  const hasAnyRole = (roles) => {
    return roles.includes(user?.role);
  };

  /**
   * Update user profile data
   * @param {Object} updatedData - Updated user data
   */
  const updateUser = (updatedData) => {
    try {
      const newUser = { ...user, ...updatedData };
      localStorage.setItem(USER_KEY, JSON.stringify(newUser));
      setUser(newUser);
    } catch (error) {
      console.error('Error updating user:', error);
    }
  };

  /**
   * Get user role display name
   * @returns {string} Vietnamese role name
   */
  const getRoleDisplayName = () => {
    const roleMap = {
      ADM: 'Quản trị viên',
      DN: 'Nhà tuyển dụng',
      UV: 'Ứng viên',
    };
    return roleMap[user?.role] || 'Người dùng';
  };

  // Context value
  const value = {
    // State
    isAuthenticated,
    user,
    token,
    loading,

    // Functions
    login,
    logout,
    hasRole,
    hasAnyRole,
    updateUser,
    saveRedirectPath,
    getRoleDisplayName,

    // Helper getters
    isAdmin: user?.role === 'ADM',
    isEmployer: user?.role === 'DN',
    isCandidate: user?.role === 'UV',
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

/**
 * useAuth Hook
 * Custom hook to use auth context
 * @returns {Object} Auth context value
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
