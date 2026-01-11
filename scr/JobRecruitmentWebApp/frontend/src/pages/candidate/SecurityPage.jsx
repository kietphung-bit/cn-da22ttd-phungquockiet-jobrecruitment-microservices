import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import authService from '../../services/auth.service';
import { useAuth } from '../../contexts/AuthContext';
import { Lock, LogOut, AlertTriangle, Eye, EyeOff } from 'lucide-react';

/**
 * SecurityPage - Trang quản lý bảo mật tài khoản
 * 
 * Chức năng:
 * 1. Đổi mật khẩu (Change Password)
 * 2. Đăng xuất khỏi tất cả thiết bị (Logout All Sessions)
 * 
 * Security Features:
 * - Old password verification
 * - Password strength validation (min 6 chars)
 * - Password confirmation matching
 * - Token invalidation on logout all
 * 
 * Route: /candidate/security
 */

// Xác thực schema
const changePasswordSchema = yup.object().shape({
  oldPassword: yup
    .string()
    .required('Vui lòng nhập mật khẩu cũ'),
  newPassword: yup
    .string()
    .required('Vui lòng nhập mật khẩu mới')
    .min(6, 'Mật khẩu mới phải có ít nhất 6 ký tự')
    .notOneOf([yup.ref('oldPassword')], 'Mật khẩu mới phải khác mật khẩu cũ'),
  confirmPassword: yup
    .string()
    .required('Vui lòng xác nhận mật khẩu mới')
    .oneOf([yup.ref('newPassword')], 'Xác nhận mật khẩu không khớp'),
});

const SecurityPage = () => {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [isChangingPassword, setIsChangingPassword] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [showOldPassword, setShowOldPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm({
    resolver: yupResolver(changePasswordSchema),
  });

  /**
   * Xử lý gửi biểu mẫu đổi mật khẩu
   */
  const onChangePassword = async (data) => {
    try {
      setIsChangingPassword(true);
      
      await authService.changePassword({
        oldPassword: data.oldPassword,
        newPassword: data.newPassword,
        confirmPassword: data.confirmPassword,
      });

      // Đặt lại form
      reset();

      // Hiển thị thông báo thành công
      toast.success('Thành công! Vui lòng đăng nhập lại.', {
        position: 'top-right',
        autoClose: 2000,
      });

      // Đăng xuất ngay lập tức
      // Xóa tất cả dữ liệu xác thực khỏi localStorage
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_user');
      localStorage.removeItem('auth_redirect');
      
      // Điều hướng đến trang đăng nhập (trạng thái sẽ được xóa bởi context khi render lại)
      navigate('/login', { replace: true });
      
      // Tải lại trang để đảm bảo trạng thái được đặt lại hoàn toàn
      window.location.reload();
    } catch (error) {
      console.error('Change password error:', error);
      
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          'Đổi mật khẩu thất bại. Vui lòng kiểm tra lại mật khẩu cũ.';
      
      toast.error(errorMessage, {
        position: 'top-right',
        autoClose: 5000,
      });
    } finally {
      setIsChangingPassword(false);
    }
  };

  /**
   * Xử lý đăng xuất khỏi tất cả thiết bị
   */
  const handleLogoutAllSessions = async () => {
    if (!window.confirm(
      'Bạn có chắc chắn muốn đăng xuất khỏi tất cả thiết bị? ' +
      'Bạn sẽ phải đăng nhập lại trên tất cả thiết bị.'
    )) {
      return;
    }

    try {
      setIsLoggingOut(true);
      
      await authService.logoutAllSessions();

      // Hiển thị thông báo thành công
      toast.success('Thành công! Vui lòng đăng nhập lại.', {
        position: 'top-right',
        autoClose: 2000,
      });

      // Đăng xuất ngay lập tức
      // Xóa tất cả dữ liệu xác thực khỏi localStorage
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_user');
      localStorage.removeItem('auth_redirect');
      
      // Điều hướng đến trang đăng nhập (trạng thái sẽ được xóa bởi context khi render lại)
      navigate('/login', { replace: true });
      
      // Tải lại trang để đảm bảo trạng thái được đặt lại hoàn toàn
      window.location.reload();
    } catch (error) {
      console.error('Logout all sessions error:', error);
      
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          'Đăng xuất thất bại. Vui lòng thử lại.';
      
      toast.error(errorMessage, {
        position: 'top-right',
        autoClose: 5000,
      });
    } finally {
      setIsLoggingOut(false);
    }
  };

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      {/* Page Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-800 flex items-center gap-3">
          <Lock className="w-8 h-8 text-blue-600" />
          Bảo mật tài khoản
        </h1>
        <p className="text-gray-600 mt-2">
          Quản lý mật khẩu và bảo mật tài khoản của bạn
        </p>
      </div>

      {/* Change Password Section */}
      <div className="bg-white rounded-lg shadow-md p-6 mb-6">
        <h2 className="text-xl font-semibold text-gray-800 mb-4 flex items-center gap-2">
          <Lock className="w-5 h-5 text-blue-600" />
          Đổi mật khẩu
        </h2>

        <form onSubmit={handleSubmit(onChangePassword)} className="space-y-4">
          {/* Old Password */}
          <div>
            <label htmlFor="oldPassword" className="block text-sm font-medium text-gray-700 mb-1">
              Mật khẩu cũ <span className="text-red-500">*</span>
            </label>
            <div className="relative">
              <input
                type={showOldPassword ? 'text' : 'password'}
                id="oldPassword"
                {...register('oldPassword')}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors.oldPassword ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="Nhập mật khẩu hiện tại"
              />
              <button
                type="button"
                onClick={() => setShowOldPassword(!showOldPassword)}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-gray-700"
              >
                {showOldPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
              </button>
            </div>
            {errors.oldPassword && (
              <p className="text-red-500 text-sm mt-1">{errors.oldPassword.message}</p>
            )}
          </div>

          {/* New Password */}
          <div>
            <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700 mb-1">
              Mật khẩu mới <span className="text-red-500">*</span>
            </label>
            <div className="relative">
              <input
                type={showNewPassword ? 'text' : 'password'}
                id="newPassword"
                {...register('newPassword')}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors.newPassword ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="Nhập mật khẩu mới (tối thiểu 6 ký tự)"
              />
              <button
                type="button"
                onClick={() => setShowNewPassword(!showNewPassword)}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-gray-700"
              >
                {showNewPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
              </button>
            </div>
            {errors.newPassword && (
              <p className="text-red-500 text-sm mt-1">{errors.newPassword.message}</p>
            )}
          </div>

          {/* Confirm Password */}
          <div>
            <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-1">
              Xác nhận mật khẩu mới <span className="text-red-500">*</span>
            </label>
            <div className="relative">
              <input
                type={showConfirmPassword ? 'text' : 'password'}
                id="confirmPassword"
                {...register('confirmPassword')}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors.confirmPassword ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="Nhập lại mật khẩu mới"
              />
              <button
                type="button"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-gray-700"
              >
                {showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
              </button>
            </div>
            {errors.confirmPassword && (
              <p className="text-red-500 text-sm mt-1">{errors.confirmPassword.message}</p>
            )}
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            disabled={isChangingPassword}
            className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 
                     disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors
                     flex items-center justify-center gap-2"
          >
            {isChangingPassword ? (
              <>
                <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                <span>Đang xử lý...</span>
              </>
            ) : (
              <>
                <Lock className="w-5 h-5" />
                <span>Đổi mật khẩu</span>
              </>
            )}
          </button>
        </form>
      </div>

      {/* Danger Zone - Logout All Sessions */}
      <div className="bg-red-50 border-2 border-red-200 rounded-lg p-6">
        <h2 className="text-xl font-semibold text-red-800 mb-3 flex items-center gap-2">
          <AlertTriangle className="w-5 h-5" />
          Vùng nguy hiểm
        </h2>
        
        <div className="mb-4">
          <h3 className="font-medium text-gray-800 mb-2">
            Đăng xuất khỏi tất cả thiết bị
          </h3>
          <p className="text-sm text-gray-600 mb-4">
            Thao tác này sẽ đăng xuất tài khoản của bạn khỏi tất cả thiết bị đang đăng nhập. 
            Bạn sẽ cần đăng nhập lại trên từng thiết bị. Sử dụng tính năng này nếu bạn nghi ngờ 
            tài khoản của mình bị truy cập trái phép.
          </p>
          <button
            onClick={handleLogoutAllSessions}
            disabled={isLoggingOut}
            className="bg-red-600 text-white py-2 px-6 rounded-lg hover:bg-red-700 
                     disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors
                     flex items-center gap-2"
          >
            {isLoggingOut ? (
              <>
                <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                <span>Đang xử lý...</span>
              </>
            ) : (
              <>
                <LogOut className="w-5 h-5" />
                <span>Đăng xuất khỏi tất cả thiết bị</span>
              </>
            )}
          </button>
        </div>
      </div>

      {/* Security Tips */}
      <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h3 className="font-semibold text-blue-800 mb-2">💡 Mẹo bảo mật</h3>
        <ul className="text-sm text-blue-700 space-y-1 list-disc list-inside">
          <li>Sử dụng mật khẩu mạnh với ít nhất 6 ký tự</li>
          <li>Không sử dụng lại mật khẩu từ các tài khoản khác</li>
          <li>Thay đổi mật khẩu định kỳ (3-6 tháng một lần)</li>
          <li>Không chia sẻ mật khẩu với bất kỳ ai</li>
          <li>Đăng xuất khỏi tất cả thiết bị nếu nghi ngờ tài khoản bị xâm nhập</li>
        </ul>
      </div>
    </div>
  );
};

export default SecurityPage;
