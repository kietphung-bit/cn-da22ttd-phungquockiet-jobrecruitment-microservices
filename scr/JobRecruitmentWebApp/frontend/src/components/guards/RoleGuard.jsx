import { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

/**
 * RoleGuard Component
 * 
 * Mục đích:
 * - Ngăn người dùng truy cập các tuyến đường không phù hợp với vai trò của họ
 * - Admin/Employer: Chuyển hướng khỏi các trang công cộng đến bảng điều khiển của họ
 * - Candidate: Không hạn chế trên các trang công cộng
 * 
 * Sử dụng:
 * Bao bọc component này xung quanh các tuyến đường của bạn trong AppRoutes.jsx
 * 
 * Ví dụ:
 * <RoleGuard>
 *   <Routes>
 *     <Route path="/" element={<HomePage />} />
 *     ...
 *   </Routes>
 * </RoleGuard>
 * 
 * Quy tắc kiểm soát truy cập:
 * - Admin (ADM): Chỉ được truy cập các tuyến đường /admin/* (chuyển hướng khỏi các trang công cộng)
 * - Employer (DN): Chỉ được truy cập các tuyến đường /employer/* (chuyển hướng khỏi các trang công cộng)
 * - Candidate (UV): Có thể truy cập các trang công cộng + các tuyến đường /candidate/*
 * 
 * Các trang công cộng sẽ bị hạn chế đối với Admin/Employer:
 * - /jobs, /jobs/:id - Trang danh sách việc làm và chi tiết
 * - /companies, /companies/:id - Trang danh sách công ty và chi tiết
 * - Các trang duyệt công cộng khác
 * 
 * Các trang được phép truy cập (Không hạn chế):
 * - / - Trang chủ (trang chào mừng)
 * - /login, /register - Trang xác thực
 * - /about, /contact - Trang thông tin tĩnh
 * 
 * Ghi chú triển khai:
 * - Sử dụng useEffect để theo dõi thay đổi đường dẫn
 * - Thực hiện chuyển hướng với { replace: true } để tránh vấn đề nút quay lại
 * - Chỉ chạy khi người dùng đã xác thực
 * - Chuyển hướng im lặng (không hiển thị thông báo lỗi)
 */
const RoleGuard = ({ children }) => {
  const { user, isAuthenticated } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    // Chỉ áp dụng guard nếu người dùng đã xác thực
    if (!isAuthenticated || !user) {
      return;
    }

    const currentPath = location.pathname;

    // Chuyển hướng người dùng đã xác thực khỏi các trang đăng nhập/đăng ký đến bảng điều khiển phù hợp với vai trò của họ
    if (currentPath === '/login' || currentPath === '/register') {
      console.log(`[RoleGuard] Authenticated user attempting to access ${currentPath}`);
      
      // Chuyển hướng đến trang phù hợp với vai trò
      if (user.role === 'ADM') {
        console.log('[RoleGuard] Redirecting Admin to /admin/dashboard');
        navigate('/admin/dashboard', { replace: true });
        return;
      }
      
      if (user.role === 'DN') {
        console.log('[RoleGuard] Redirecting Employer to /employer/dashboard');
        navigate('/employer/dashboard', { replace: true });
        return;
      }
      
      // Ứng viên: chuyển hướng đến trang chủ
      console.log('[RoleGuard] Redirecting Candidate to homepage');
      navigate('/', { replace: true });
      return;
    }

    // Định nghĩa các đường dẫn bị hạn chế cho Admin và Employer
    const publicBrowsingPages = [
      '/jobs',           // Trang danh sách việc làm
      '/companies',      // Trang danh sách công ty
      '/search',         // Trang kết quả tìm kiếm
      '/about',          // Trang giới thiệu
      '/contact',        // Contact page
    ];

    // Kiểm tra xem đường dẫn hiện tại có bắt đầu bằng bất kỳ tiền tố bị hạn chế nào không
    const isRestrictedPath = publicBrowsingPages.some(prefix => 
      currentPath.startsWith(prefix)
    );

    // Admin (ADM): Chuyển hướng khỏi các trang duyệt công cộng
    if (user.role === 'ADM' && isRestrictedPath) {
      console.warn(`[RoleGuard] Admin attempting to access public page: ${currentPath}`);
      console.log('[RoleGuard] Redirecting to admin dashboard...');
      navigate('/admin/dashboard', { replace: true });
      return;
    }

    // Employer (DN): Chuyển hướng khỏi các trang duyệt công cộng
    if (user.role === 'DN' && isRestrictedPath) {
      console.warn(`[RoleGuard] Employer attempting to access public page: ${currentPath}`);
      console.log('[RoleGuard] Redirecting to employer dashboard...');
      navigate('/employer/dashboard', { replace: true });
      return;
    }

    // Ứng viên (UV): Không hạn chế trên các trang công cộng
    // Họ có thể duyệt tự do

  }, [location.pathname, user, isAuthenticated, navigate]);

  // Hiển thị children (routes) bình thường
  return children;
};

export default RoleGuard;
