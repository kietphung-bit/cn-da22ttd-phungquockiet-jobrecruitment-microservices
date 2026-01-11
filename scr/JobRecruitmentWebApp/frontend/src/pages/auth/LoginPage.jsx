import { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { LogIn, Mail, Lock, Eye, EyeOff, AlertCircle } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import authService from '../../services/auth.service';

/**
 * LoginPage Component
 * 
 * Tính năng:
 * - Xác thực với API backend
 * - Chuyển hướng thông minh sau khi đăng nhập (trở về trang trước hoặc trang dashboard theo vai trò)
 * - Quản lý token JWT
 * - Đa ngôn ngữ tiếng Việt
 * - Xác thực form và xử lý lỗi
 * 
 * Chiến lược chuyển hướng thông minh sau khi đăng nhập:
 * 1. Nếu người dùng đến từ một trang cụ thể (location.state.from), chuyển hướng trở lại đó
 * 2. Nếu không, chuyển hướng đến dashboard dựa trên vai trò (UV → /candidate/profile, DN → /employer/dashboard, ADM → /admin/dashboard)
 */
const LoginPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  
  const { login } = useAuth();

  // Kiểm tra thông báo thành công từ đăng ký
  useEffect(() => {
    if (location.state?.message) {
      setSuccessMessage(location.state.message);
      if (location.state?.username) {
        setEmail(location.state.username);
      }
      // Xóa state sau khi hiển thị thông báo
      window.history.replaceState({}, document.title);
    }
  }, [location]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');
    setLoading(true);

    try {
      // Gọi API xác thực backend
      const response = await authService.login(email, password);
      
      // Phản hồi đã được axios interceptor xử lý
      // Dữ liệu gồm: { token, userCode, username, roleCode, candidateName/companyName... }
      const { token, roleCode, username, userCode } = response;
      
      // Xây dựng đối tượng dữ liệu người dùng (bao gồm vai trò trong userData)
      const userData = {
        username: username,
        email: email,
        userCode: userCode,
        role: roleCode, // Bao gồm vai trò ở đây
      };

      // Thêm các trường theo vai trò nếu tồn tại
      if (response.candidateName) {
        userData.candidateName = response.candidateName;
        userData.candidateCode = response.candidateCode || userCode;
      } else if (response.companyName) {
        userData.companyName = response.companyName;
        userData.companyCode = response.companyCode || userCode;
      }

      // Lấy vị trí 'from' để chuyển hướng thông minh
      const from = location.state?.from?.pathname || null;

      // Gọi login từ AuthContext (truyền roleCode riêng cho logic chuyển hướng)
      login(token, roleCode, userData, from);
      
    } catch (err) {
      // Xử lý các loại lỗi khác nhau
      const errorMessage = err.response?.data?.message || '';
      
      // Kiểm tra lỗi tài khoản đang chờ duyệt hoặc bị khóa
      if (errorMessage.includes('chờ duyệt') || errorMessage.includes('PENDING')) {
        setError('Tài khoản của bạn đang chờ Admin phê duyệt. Vui lòng quay lại sau.');
      } else if (errorMessage.includes('bị khóa') || errorMessage.includes('BLOCKED')) {
        setError('Tài khoản đã bị khóa. Vui lòng liên hệ Admin để biết thêm chi tiết.');
      } else if (err.response?.status === 401) {
        setError('Email hoặc mật khẩu không chính xác');
      } else if (err.response?.status === 429) {
        setError('Quá nhiều lần thử. Vui lòng thử lại sau.');
      } else if (errorMessage) {
        setError(errorMessage);
      } else {
        setError('Đăng nhập thất bại. Vui lòng thử lại.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-custom py-16">
      <div className="max-w-md mx-auto space-y-8">
        {/* Success Message from Registration */}
        {successMessage && (
          <div className="bg-green-50 border-l-4 border-green-400 p-4 rounded-r-lg">
            <div className="flex">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-green-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3">
                <p className="text-sm text-green-700 font-medium">
                  {successMessage}
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Header */}
        <div className="text-center">
          <div className="mx-auto h-16 w-16 bg-primary rounded-full flex items-center justify-center">
            <LogIn className="h-8 w-8 text-white" />
          </div>
          <h2 className="mt-6 text-3xl font-bold text-neutral-900">
            Đăng nhập
          </h2>
          <p className="mt-2 text-sm text-neutral-600">
            Chào mừng bạn quay trở lại
          </p>
        </div>

        {/* Login Form */}
        <div className="bg-white rounded-xl shadow-lg p-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Error Message */}
            {error && (
              <div className="bg-red-50 border-l-4 border-red-400 p-4 rounded-r-lg">
                <div className="flex">
                  <AlertCircle className="h-5 w-5 text-red-400 mr-3 flex-shrink-0" />
                  <p className="text-sm text-red-600">{error}</p>
                </div>
              </div>
            )}

            {/* Email Input */}
            <div>
              <label htmlFor="email" className="block text-sm font-semibold text-neutral-700 mb-2">
                Email
              </label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-neutral-400 w-5 h-5" />
                <input
                  id="email"
                  name="email"
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="example@email.com"
                  className="w-full pl-11 pr-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                />
              </div>
            </div>

            {/* Password Input */}
            <div>
              <label htmlFor="password" className="block text-sm font-semibold text-neutral-700 mb-2">
                Mật khẩu
              </label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-neutral-400 w-5 h-5" />
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-11 pr-12 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 text-neutral-400 hover:text-neutral-600"
                >
                  {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            {/* Remember Me & Forgot Password */}
            <div className="flex items-center justify-between">
              <label className="flex items-center">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className="w-4 h-4 text-primary border-neutral-300 rounded focus:ring-2 focus:ring-primary"
                />
                <span className="ml-2 text-sm text-neutral-700">Ghi nhớ đăng nhập</span>
              </label>
              <Link to="/forgot-password" className="text-sm text-primary hover:text-primary-600">
                Quên mật khẩu?
              </Link>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading}
              className="w-full px-6 py-3 bg-primary text-white rounded-lg font-semibold hover:bg-primary-600 shadow-md hover:shadow-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {loading ? (
                <>
                  <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  Đang đăng nhập...
                </>
              ) : (
                <>
                  <LogIn className="w-5 h-5" />
                  Đăng nhập
                </>
              )}
            </button>

            {/* Register Link */}
            <div className="text-center">
              <p className="text-sm text-neutral-600">
                Chưa có tài khoản?{' '}
                <Link to="/register" className="text-primary font-semibold hover:text-primary-600">
                  Đăng ký ngay
                </Link>
              </p>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
