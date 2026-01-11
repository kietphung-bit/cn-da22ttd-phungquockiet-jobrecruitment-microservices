import { useAuth } from '../contexts/AuthContext';

/**
 * Custom hook cho xác thực
 * Re-exports useAuth cho tiện lợi
 * Có thể mở rộng với các logic liên quan đến xác thực khác
 */
export { useAuth };

/**
 * Hook để kiểm tra xem người dùng đã xác thực hay chưa
 * @returns {boolean} True nếu người dùng đã xác thực
 */
export const useIsAuthenticated = () => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated;
};

/**
 * Hook để lấy người dùng hiện tại
 * @returns {Object|null} Đối tượng người dùng hoặc null
 */
export const useCurrentUser = () => {
  const { user } = useAuth();
  return user;
};

/**
 * Hook để kiểm tra vai trò người dùng
 * @param {string} role - Vai trò cần kiểm tra (ADM, DN, UV)
 * @returns {boolean} True nếu người dùng có vai trò đó
 */
export const useHasRole = (role) => {
  const { hasRole } = useAuth();
  return hasRole(role);
};

/**
 * Hook để yêu cầu xác thực
 * Chuyển hướng đến trang đăng nhập nếu chưa xác thực
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
