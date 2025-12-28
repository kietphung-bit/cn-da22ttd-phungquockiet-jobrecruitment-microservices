import { useAuth } from '../contexts/AuthContext';

/**
 * Custom hook for authentication
 * Re-exports useAuth for convenience
 * Can be extended with additional auth-related logic
 */
export { useAuth };

/**
 * Hook to check if user is authenticated
 * @returns {boolean} True if user is authenticated
 */
export const useIsAuthenticated = () => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated;
};

/**
 * Hook to get current user
 * @returns {Object|null} User object or null
 */
export const useCurrentUser = () => {
  const { user } = useAuth();
  return user;
};

/**
 * Hook to check user role
 * @param {string} role - Role to check (ADM, DN, UV)
 * @returns {boolean} True if user has the role
 */
export const useHasRole = (role) => {
  const { hasRole } = useAuth();
  return hasRole(role);
};

/**
 * Hook to require authentication
 * Redirects to login if not authenticated
 */
export const useRequireAuth = () => {
  const { isAuthenticated, saveRedirectPath } = useAuth();
  const location = window.location.pathname;

  if (!isAuthenticated) {
    saveRedirectPath(location);
    window.location.href = '/login';
  }

  return isAuthenticated;
};
