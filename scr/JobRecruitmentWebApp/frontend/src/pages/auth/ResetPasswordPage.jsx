import { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { Lock, Eye, EyeOff, CheckCircle, AlertCircle } from 'lucide-react';

/**
 * ResetPasswordPage Component
 * Xử lý đặt lại mật khẩu với token
 * 
 * Tính năng:
 * - Xác thực token từ URL
 * - Nhập mật khẩu mới và xác nhận
 * - Xác thực độ mạnh mật khẩu
 * - Thông báo thành công/lỗi
 * 
 * Lưu ý: Đây là phần giao diện người dùng cơ bản.
 * Cần tạo endpoint backend để thực hiện đặt lại mật khẩu thực sự
 */
const ResetPasswordPage = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [tokenValid, setTokenValid] = useState(true);

  // Xác thực token khi component được mount
  useEffect(() => {
    if (!token) {
      setTokenValid(false);
      setError('Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.');
    } else {
      // TODO: Xác thực token với backend
      // Hiện tại, giả sử token hợp lệ
      setTokenValid(true);
    }
  }, [token]);

  const validatePassword = () => {
    if (password.length < 6) {
      setError('Mật khẩu phải có ít nhất 6 ký tự');
      return false;
    }
    if (password !== confirmPassword) {
      setError('Mật khẩu xác nhận không khớp');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!validatePassword()) {
      return;
    }

    setLoading(true);

    try {
      // TODO: Thực hiện gọi API backend để đặt lại mật khẩu
      // Ví dụ: await authService.resetPassword(token, password);
      
      // Giả lập gọi API
      await new Promise(resolve => setTimeout(resolve, 1500));

      // Hiện tại, luôn hiển thị thành công
      setSuccess(true);

      // Chuyển hướng đến trang đăng nhập sau 3 giây
      setTimeout(() => {
        navigate('/login', { 
          state: { 
            message: 'Mật khẩu đã được đặt lại thành công. Vui lòng đăng nhập.' 
          } 
        });
      }, 3000);
      
    } catch (err) {
      console.error('Reset password error:', err);
      setError(
        err.response?.data?.message || 
        'Không thể đặt lại mật khẩu. Vui lòng thử lại hoặc yêu cầu link mới.'
      );
    } finally {
      setLoading(false);
    }
  };

  // Nếu token không hợp lệ, hiển thị trang lỗi
  if (!tokenValid) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-primary-50 to-primary-100 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-md w-full space-y-8">
          <div className="bg-white shadow-xl rounded-lg p-8 text-center">
            <div className="mx-auto w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mb-4">
              <AlertCircle className="w-10 h-10 text-red-600" />
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              Link không hợp lệ
            </h2>
            <p className="text-gray-600 mb-6">
              {error}
            </p>
            <div className="space-y-3">
              <Link
                to="/forgot-password"
                className="block w-full bg-primary text-white py-3 px-4 rounded-lg font-medium hover:bg-primary-700 transition-colors"
              >
                Yêu cầu link mới
              </Link>
              <Link
                to="/login"
                className="block w-full text-primary-600 hover:text-primary-700 font-medium"
              >
                Quay lại đăng nhập
              </Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-primary-100 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        {/* Header */}
        <div className="text-center">
          <h2 className="text-3xl font-bold text-gray-900">
            Đặt lại mật khẩu
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            Nhập mật khẩu mới cho tài khoản của bạn
          </p>
        </div>

        {/* Form Card */}
        <div className="bg-white shadow-xl rounded-lg p-8">
          {!success ? (
            <form onSubmit={handleSubmit} className="space-y-6">
              {/* New Password Input */}
              <div>
                <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
                  Mật khẩu mới
                </label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    id="password"
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="w-full pl-10 pr-12 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors"
                    placeholder="Tối thiểu 6 ký tự"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors"
                  >
                    {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
              </div>

              {/* Confirm Password Input */}
              <div>
                <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-2">
                  Xác nhận mật khẩu
                </label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    id="confirmPassword"
                    type={showConfirmPassword ? 'text' : 'password'}
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className="w-full pl-10 pr-12 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors"
                    placeholder="Nhập lại mật khẩu"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors"
                  >
                    {showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
              </div>

              {/* Password Requirements */}
              <div className="bg-gray-50 rounded-lg p-4">
                <p className="text-sm font-medium text-gray-700 mb-2">Yêu cầu mật khẩu:</p>
                <ul className="text-sm text-gray-600 space-y-1">
                  <li className={password.length >= 6 ? 'text-green-600' : ''}>
                    • Tối thiểu 6 ký tự
                  </li>
                  <li className={password === confirmPassword && password ? 'text-green-600' : ''}>
                    • Mật khẩu xác nhận khớp
                  </li>
                </ul>
              </div>

              {/* Error Message */}
              {error && (
                <div className="bg-red-50 border-l-4 border-red-400 p-4 rounded-r-lg">
                  <div className="flex">
                    <AlertCircle className="h-5 w-5 text-red-400 mr-3 flex-shrink-0" />
                    <p className="text-sm text-red-700">{error}</p>
                  </div>
                </div>
              )}

              {/* Submit Button */}
              <button
                type="submit"
                disabled={loading}
                className="w-full bg-primary text-white py-3 px-4 rounded-lg font-medium hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all transform hover:scale-[1.02] active:scale-[0.98]"
              >
                {loading ? (
                  <span className="flex items-center justify-center">
                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Đang xử lý...
                  </span>
                ) : (
                  'Đặt Lại Mật Khẩu'
                )}
              </button>
            </form>
          ) : (
            // Success Message
            <div className="text-center space-y-4">
              <div className="mx-auto w-16 h-16 bg-green-100 rounded-full flex items-center justify-center">
                <CheckCircle className="w-10 h-10 text-green-600" />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-gray-900 mb-2">
                  Thành công!
                </h3>
                <p className="text-gray-600">
                  Mật khẩu của bạn đã được đặt lại thành công.
                </p>
                <p className="text-sm text-gray-500 mt-2">
                  Bạn sẽ được chuyển đến trang đăng nhập trong giây lát...
                </p>
              </div>
              <Link
                to="/login"
                className="inline-block mt-4 text-primary-600 hover:text-primary-700 font-medium"
              >
                Đăng nhập ngay
              </Link>
            </div>
          )}
        </div>

        {/* Back to Login */}
        {!success && (
          <div className="text-center">
            <Link
              to="/login"
              className="text-sm text-gray-500 hover:text-gray-700"
            >
              ← Quay lại đăng nhập
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default ResetPasswordPage;
