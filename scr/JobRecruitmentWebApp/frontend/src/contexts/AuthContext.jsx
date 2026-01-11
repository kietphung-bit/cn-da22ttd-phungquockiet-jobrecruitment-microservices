import React, { createContext, useState, useContext, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

/**
 * AuthContext
 * Quản lý trạng thái xác thực và cung cấp các hàm liên quan đến xác thực
 * 
 * Tính năng chính:
 * - Chức năng Đăng nhập/Đăng xuất
 * - Quản lý token trong localStorage
 * - Lưu trữ dữ liệu người dùng
 * - Chuyển hướng thông minh (trở về trang ban đầu sau khi đăng nhập)
 * - Kiểm soát truy cập dựa trên vai trò
 */

// Tạo Context
const AuthContext = createContext(null);

// Lưu key vào localStorage
const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';
const REDIRECT_KEY = 'auth_redirect';

/**
 * AuthProvider Component
 * Bao bọc ứng dụng và cung cấp context xác thực
 */
export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const location = useLocation();

  // Khởi tạo trạng thái xác thực từ localStorage khi component mount
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
        // Xóa dữ liệu bị hỏng
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
      } finally {
        setLoading(false);
      }
    };

    initAuth();
  }, []);

  /**
   * Lưu đường dẫn hiện tại để chuyển hướng sau khi đăng nhập
   * @param {string} path - Đường dẫn để chuyển hướng sau khi đăng nhập
   */
  const saveRedirectPath = (path) => {
    // Không lưu các trang xác thực làm đường dẫn chuyển hướng
    const excludedPaths = ['/login', '/register', '/logout'];
    if (!excludedPaths.includes(path)) {
      localStorage.setItem(REDIRECT_KEY, path);
    }
  };

  /**
   * Lấy và xóa đường dẫn chuyển hướng đã lưu
   * @returns {string} Đường dẫn đã lưu hoặc mặc định '/'
   */
  const getRedirectPath = () => {
    const path = localStorage.getItem(REDIRECT_KEY);
    localStorage.removeItem(REDIRECT_KEY);
    return path || '/';
  };

  /**
   * Hàm đăng nhập với chuyển hướng nghiêm ngặt dựa trên vai trò
   * @param {string} authToken - JWT token từ backend
   * @param {string} role - Vai trò người dùng (ADM, DN, UV)
   * @param {Object} userData - Dữ liệu hồ sơ người dùng
   * @param {string} from - Đường dẫn người dùng đến từ (tùy chọn, chỉ dùng cho Ứng viên)
   * 
   * Quy tắc Chuyển hướng nghiêm ngặt (KHÔNG KIỂM TRA LỊCH SỬ cho Admin/Employer):
   * 
   * 1. Admin (ADM): bắt buộc chuyển đến /admin/dashboard
   *    - Bỏ qua hoàn toàn đường dẫn trước đó
   *    - Luôn chuyển đến trang tổng quan admin
   * 
   * 2. Employer (DN): bắt buộc chuyển đến /employer/dashboard
   *    - Bỏ qua hoàn toàn đường dẫn trước đó
   *    - Luôn chuyển đến trang tổng quan nhà tuyển dụng
   * 
   * 3. Candidate (UV): chuyển hướng thông minh dựa trên đường dẫn trước đó
   *    - Nếu đường dẫn trước đó là trang AUTH (/login, /register, /forgot-password):
   *      → Chuyển đến Trang chủ (/)
   *    - Nếu đường dẫn trước đó là trang NỘI DUNG (ví dụ, /jobs/123, /companies):
   *      → Chuyển trở lại đường dẫn đó
   *    - Nếu không có đường dẫn trước đó hoặc không hợp lệ:
   *      → Mặc định chuyển đến Trang chủ (/)
   * 
   * Ngăn chặn chuyển hướng sai vai trò:
   * - Admin/Employer bị kẹt trên các trang công khai
   * - Ứng viên gặp lỗi 403 trên các tuyến được bảo vệ theo vai trò
   * - Vòng lặp chuyển hướng
   */
  const login = (authToken, role, userData, from = null) => {
    try {
      // Lưu vào localStorage
      localStorage.setItem(TOKEN_KEY, authToken);
      localStorage.setItem(USER_KEY, JSON.stringify(userData));

      // Cập nhật trạng thái
      setToken(authToken);
      setUser({ ...userData, role });
      setIsAuthenticated(true);

      // Chuyển hướng nghiêm ngặt dựa trên vai trò
      if (role === 'ADM') {
        // Admin: Bắt buộc chuyển đến trang tổng quan admin, không kiểm tra lịch sử
        navigate('/admin/dashboard', { replace: true });
        return true;
      }

      if (role === 'DN') {
        // Employer: Bắt buộc chuyển đến trang tổng quan nhà tuyển dụng, không kiểm tra lịch sử
        navigate('/employer/dashboard', { replace: true });
        return true;
      }

      // Ứng viên (UV): chuyển hướng thông minh dựa trên đường dẫn trước đó
      // Sử dụng tham số 'from' được truyền từ LoginPage (đã chứa location.state?.from?.pathname)
      const previousPath = from || '/';

      // Định nghĩa các trang xác thực (nơi không nên quay lại)
      const authRoutes = ['/login', '/register', '/forgot-password', '/reset-password'];

      // Kiểm tra nếu đường dẫn trước đó là trang xác thực
      const isAuthRoute = authRoutes.some(route => previousPath.includes(route));

      // Logic chuyển hướng cho ứng viên
      if (isAuthRoute) {
        // Nếu đến từ trang xác thực → chuyển đến trang chủ
        navigate('/', { replace: true });
      } else {
        // Nếu đến từ trang nội dung → quay lại trang đó
        navigate(previousPath, { replace: true });
      }

      return true;
    } catch (error) {
      return false;
    }
  };

  /**
   * Kiểm tra xem một đường dẫn có phù hợp với vai trò hay không
   * @param {string} path - Đường dẫn cần kiểm tra
   * @param {string} userRole - Vai trò của người dùng
   * @returns {boolean} True nếu người dùng có thể truy cập đường dẫn, ngược lại False
   */
  const isRoleAppropriatePath = (path, userRole) => {
    if (!path) return true;

    // Admin chỉ nên truy cập các tuyến admin
    if (userRole === 'ADM') {
      return path.startsWith('/admin') || path === '/';
    }

    // Employer chỉ nên truy cập các tuyến employer
    if (userRole === 'DN') {
      return path.startsWith('/employer') || path === '/';
    }

    // Ứng viên chỉ nên truy cập các tuyến công khai và ứng viên
    if (userRole === 'UV') {
      return !path.startsWith('/admin') && !path.startsWith('/employer');
    }

    return true;
  };

  /**
   * Hàm đăng xuất
   * Xóa tất cả dữ liệu xác thực và luôn chuyển hướng về trang chủ
   * 
   * Chiến lược:
   * - Xóa tất cả localStorage (token, user, đường dẫn chuyển hướng)
   * - Đặt lại trạng thái xác thực
   * - Chuyển hướng về trang chủ (/) KHÔNG phải trang đăng nhập
   */
  const logout = () => {
    try {
      // Xóa localStorage
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      localStorage.removeItem(REDIRECT_KEY);

      // Đặt lại trạng thái
      setToken(null);
      setUser(null);
      setIsAuthenticated(false);

      // Luôn chuyển hướng về trang chủ (/) sau khi đăng xuất
      navigate('/', { replace: true });
    } catch (error) {
      // Lỗi im lặng
    }
  };

  /**
   * Kiểm tra xem người dùng có vai trò cụ thể hay không
   * @param {string} requiredRole - Vai trò cần kiểm tra (ADM, DN, UV)
   * @returns {boolean}
   */
  const hasRole = (requiredRole) => {
    return user?.role === requiredRole;
  };

  /**
   * Kiểm tra xem người dùng có bất kỳ vai trò nào trong số các vai trò được chỉ định hay không
   * @param {Array<string>} roles - Mảng các vai trò cần kiểm tra
   * @returns {boolean}
   */
  const hasAnyRole = (roles) => {
    return roles.includes(user?.role);
  };

  /**
   * Cập nhật dữ liệu hồ sơ người dùng
   * @param {Object} updatedData - Dữ liệu người dùng đã cập nhật
   */
  const updateUser = (updatedData) => {
    try {
      const newUser = { ...user, ...updatedData };
      localStorage.setItem(USER_KEY, JSON.stringify(newUser));
      setUser(newUser);
    } catch (error) {
      // Lỗi im lặng
    }
  };

  /**
   * Lấy tên hiển thị vai trò người dùng
   * @returns {string} Tên vai trò bằng tiếng Việt
   */
  const getRoleDisplayName = () => {
    const roleMap = {
      ADM: 'Quản trị viên',
      DN: 'Nhà tuyển dụng',
      UV: 'Ứng viên',
    };
    return roleMap[user?.role] || 'Người dùng';
  };

  // Giá trị context
  const value = {
    // Trạng thái
    isAuthenticated,
    user,
    token,
    loading,

    // Hàm
    login,
    logout,
    hasRole,
    hasAnyRole,
    updateUser,
    saveRedirectPath,
    getRoleDisplayName,
    isRoleAppropriatePath,

    // Helper getter
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
 * Custom hook để sử dụng auth context
 * @returns {Object} Giá trị context xác thực
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
