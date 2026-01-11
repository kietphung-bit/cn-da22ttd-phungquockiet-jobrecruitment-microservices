import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { Loader2 } from 'lucide-react';

/**
 * PrivateRoute Component
 * Wrapper cho các protected routes yêu cầu xác thực
 * 
 * Tính năng:
 * - Kiểm tra nếu người dùng đã xác thực
 * - Lưu đường dẫn hiện tại để chuyển hướng sau khi đăng nhập
 * - Chuyển hướng đến trang đăng nhập nếu chưa xác thực
 * - Kiểm soát truy cập dựa trên vai trò (tuỳ chọn)
 * 
 * @param {Object} props
 * @param {React.ReactNode} props.children - Các thành phần con sẽ được render nếu đã xác thực
 * @param {string|Array<string>} props.allowedRoles - Vai trò (hoặc mảng vai trò) được phép truy cập (tuỳ chọn)
 */
const PrivateRoute = ({ children, allowedRoles }) => {
  const { isAuthenticated, loading, user, saveRedirectPath } = useAuth();
  const location = useLocation();

  // Hiển thị loading spinner trong khi kiểm tra trạng thái xác thực
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

  // Nếu chưa xác thực, lưu đường dẫn hiện tại và chuyển hướng đến trang đăng nhập
  if (!isAuthenticated) {
    // Lưu đường dẫn hiện tại để chuyển hướng sau khi đăng nhập
    saveRedirectPath(location.pathname + location.search);
    
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Nếu allowedRoles được chỉ định, kiểm tra vai trò người dùng
  if (allowedRoles) {
    const roles = Array.isArray(allowedRoles) ? allowedRoles : [allowedRoles];
    
    if (!roles.includes(user?.role)) {
      // Người dùng không có vai trò cần thiết - hiển thị 403 Forbidden
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

  // Người dùng đã xác thực và có vai trò cần thiết (nếu được chỉ định)
  return children;
};

export default PrivateRoute;
