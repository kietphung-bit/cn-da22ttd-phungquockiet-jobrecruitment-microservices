import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock, User, Building2, MapPin, Globe, FileText, Phone, Calendar, Users, Eye, EyeOff } from 'lucide-react';
import authService from '../../services/auth.service';
import { useAuth } from '../../contexts/AuthContext';

/**
 * RegisterPage Component
 * Xử lý đăng ký người dùng cho cả Ứng viên (UV) và Nhà tuyển dụng (DN)
 * Tính năng:
 * - Lựa chọn vai trò (Ứng viên hoặc Nhà tuyển dụng)
 * - Các trường form động dựa trên vai trò
 * - Xác thực form với quy tắc tiếng Việt
 * - Xử lý và hiển thị lỗi từ backend
 * - Chuyển hướng sau khi đăng ký thành công
 */
const RegisterPage = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  
  // Trạng thái form
  const [userType, setUserType] = useState('candidate'); // 'candidate' hoặc 'employer'
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Các trường chung
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  // Các trường ứng viên
  const [candidateName, setCandidateName] = useState('');
  const [candidatePhone, setCandidatePhone] = useState('');
  const [candidateBirthdate, setCandidateBirthdate] = useState('');
  const [candidateGender, setCandidateGender] = useState('MALE');

  // Các trường nhà tuyển dụng
  const [companyName, setCompanyName] = useState('');
  const [companyAddress, setCompanyAddress] = useState('');
  const [companyWebsite, setCompanyWebsite] = useState('');
  const [companyDescription, setCompanyDescription] = useState('');

  // Hiển thị/ẩn mật khẩu
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  /**
   * Xác thực các trường form trước khi gửi
   */
  const validateForm = () => {
    // Xác thực chung
    if (!username || !password || !confirmPassword) {
      setError('Vui lòng điền đầy đủ thông tin bắt buộc');
      return false;
    }

    // Xác thực email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(username)) {
      setError('Email không hợp lệ');
      return false;
    }

    // Xác thực mật khẩu
    if (password.length < 6) {
      setError('Mật khẩu phải có ít nhất 6 ký tự');
      return false;
    }

    if (password !== confirmPassword) {
      setError('Mật khẩu xác nhận không khớp');
      return false;
    }

    // Xác thực riêng cho ứng viên
    if (userType === 'candidate') {
      if (!candidateName || !candidatePhone || !candidateBirthdate) {
        setError('Vui lòng điền đầy đủ thông tin ứng viên');
        return false;
      }

      // Xác thực tên (chỉ chữ cái và khoảng trắng)
      const nameRegex = /^[a-zA-ZÀ-ỹ\s]+$/;
      if (!nameRegex.test(candidateName)) {
        setError('Họ tên chỉ được chứa chữ cái và khoảng trắng');
        return false;
      }

      // Xác thực số điện thoại (10-11 chữ số)
      const phoneRegex = /^[0-9]{10,11}$/;
      if (!phoneRegex.test(candidatePhone)) {
        setError('Số điện thoại phải có 10-11 chữ số');
        return false;
      }

      // Xác thực tuổi (phải >= 18)
      const birthDate = new Date(candidateBirthdate);
      const today = new Date();
      const age = today.getFullYear() - birthDate.getFullYear();
      const monthDiff = today.getMonth() - birthDate.getMonth();
      const actualAge = monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate()) 
        ? age - 1 
        : age;

      if (actualAge < 18) {
        setError('Ứng viên phải từ 18 tuổi trở lên');
        return false;
      }
    }

    // Xác thực riêng cho nhà tuyển dụng
    if (userType === 'employer') {
      if (!companyName || !companyAddress) {
        setError('Vui lòng điền đầy đủ thông tin công ty');
        return false;
      }

      // Xác thực tên công ty (chỉ chữ cái và khoảng trắng)
      const nameRegex = /^[a-zA-ZÀ-ỹ\s]+$/;
      if (!nameRegex.test(companyName)) {
        setError('Tên công ty chỉ được chứa chữ cái và khoảng trắng');
        return false;
      }
    }

    return true;
  };

  /**
   * Xử lý gửi form đăng ký
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Xác thực form
    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      let response;
      let userData;

      if (userType === 'candidate') {
        // Đăng ký với tư cách ứng viên
        response = await authService.registerCandidate({
          username,
          password,
          candidateName,
          candidateEmail: username, // Sử dụng username làm email liên hệ
          candidatePhone,
          candidateBirthdate,
          candidateGender,
        });

        userData = response.data;
      } else {
        // Đăng ký với tư cách nhà tuyển dụng
        response = await authService.registerEmployer({
          username,
          password,
          companyName,
          companyEmail: username, // Sử dụng username làm email liên hệ
          companyAddress,
          companyWebsite,
          companyDescription,
        });

        userData = response.data;
      }

      // Hiển thị thông báo thành công
      setSuccess('Đăng ký thành công! Đang chuyển đến trang đăng nhập...');

      // Chuyển hướng đến trang đăng nhập sau 2 giây
      setTimeout(() => {
        navigate('/login', { 
          state: { 
            message: 'Đăng ký thành công! Vui lòng đăng nhập.',
            username: username 
          } 
        });
      }, 2000);

    } catch (err) {
      console.error('Registration error:', err);
      setError(
        err.response?.data?.message || 
        'Đăng ký thất bại. Vui lòng thử lại.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl mx-auto">
        {/* Header */}
        <div className="text-center mb-8">
          <h2 className="text-3xl font-bold text-gray-900">Đăng ký tài khoản</h2>
          <p className="mt-2 text-gray-600">
            Tạo tài khoản mới để trải nghiệm dịch vụ tuyển dụng
          </p>
        </div>

        {/* Registration Form */}
        <div className="bg-white shadow-lg rounded-lg p-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* User Type Selection */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-3">
                Bạn đang đăng ký với tư cách *
              </label>
              <div className="grid grid-cols-2 gap-4">
                <button
                  type="button"
                  onClick={() => setUserType('candidate')}
                  className={`p-4 border-2 rounded-lg flex items-center justify-center space-x-2 transition-all ${
                    userType === 'candidate'
                      ? 'border-primary-500 bg-primary-50 text-primary-700'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <User className="w-5 h-5" />
                  <span className="font-medium">Ứng viên</span>
                </button>
                <button
                  type="button"
                  onClick={() => setUserType('employer')}
                  className={`p-4 border-2 rounded-lg flex items-center justify-center space-x-2 transition-all ${
                    userType === 'employer'
                      ? 'border-primary-500 bg-primary-50 text-primary-700'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <Building2 className="w-5 h-5" />
                  <span className="font-medium">Nhà tuyển dụng</span>
                </button>
              </div>
            </div>

            

            {/* Candidate-specific Fields */}
            {userType === 'candidate' && (
              <>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Họ và tên *
                  </label>
                  <div className="relative">
                    <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input
                      type="text"
                      value={candidateName}
                      onChange={(e) => setCandidateName(e.target.value)}
                      className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                      placeholder="Nguyễn Văn A"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Giới tính *
                  </label>
                  <div className="relative">
                    <Users className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <select
                      value={candidateGender}
                      onChange={(e) => setCandidateGender(e.target.value)}
                      className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                      required
                    >
                      <option value="MALE">Nam</option>
                      <option value="FEMALE">Nữ</option>
                      <option value="OTHER">Khác</option>
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Ngày sinh *
                  </label>
                  <div className="relative">
                    <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input
                      type="date"
                      value={candidateBirthdate}
                      onChange={(e) => setCandidateBirthdate(e.target.value)}
                      className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Số điện thoại *
                  </label>
                  <div className="relative">
                    <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input
                      type="tel"
                      value={candidatePhone}
                      onChange={(e) => setCandidatePhone(e.target.value)}
                      className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                      placeholder="0901234567"
                      required
                    />
                  </div>
                </div>                        
              </>
            )}

            {/* Common Fields */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Email đăng nhập *
              </label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="email"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                  placeholder="email@example.com"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Mật khẩu *
              </label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-10 pr-12 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                  placeholder="Tối thiểu 6 ký tự"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Xác nhận mật khẩu *
              </label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type={showConfirmPassword ? 'text' : 'password'}
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="w-full pl-10 pr-12 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                  placeholder="Nhập lại mật khẩu"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  {showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            {/* Employer-specific Fields */}
            {userType === 'employer' && (
              <>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Tên công ty *
                  </label>
                  <div className="relative">
                    <Building2 className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input
                      type="text"
                      value={companyName}
                      onChange={(e) => setCompanyName(e.target.value)}
                      className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                      placeholder="Công Ty TNHH ABC"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Địa chỉ công ty *
                  </label>
                  <div className="relative">
                    <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input
                      type="text"
                      value={companyAddress}
                      onChange={(e) => setCompanyAddress(e.target.value)}
                      className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                      placeholder="123 Đường ABC, Quận 1, TP.HCM"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Website công ty
                  </label>
                  <div className="relative">
                    <Globe className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input
                      type="url"
                      value={companyWebsite}
                      onChange={(e) => setCompanyWebsite(e.target.value)}
                      className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                      placeholder="https://company.com"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Mô tả công ty
                  </label>
                  <div className="relative">
                    <FileText className="absolute left-3 top-3 w-5 h-5 text-gray-400" />
                    <textarea
                      value={companyDescription}
                      onChange={(e) => setCompanyDescription(e.target.value)}
                      className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 min-h-[100px]"
                      placeholder="Mô tả ngắn về công ty..."
                    />
                  </div>
                </div>
              </>
            )}

            {/* Error Message */}
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
                {error}
              </div>
            )}

            {/* Success Message */}
            {success && (
              <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg">
                {success}
              </div>
            )}

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-primary-600 text-white py-3 px-4 rounded-lg font-medium hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {loading ? 'Đang xử lý...' : 'Đăng Ký'}
            </button>
          </form>

          {/* Login Link */}
          <div className="mt-6 text-center">
            <p className="text-gray-600">
              Đã có tài khoản?{' '}
              <Link
                to="/login"
                className="text-primary-600 hover:text-primary-700 font-medium"
              >
                Đăng nhập ngay
              </Link>
            </p>
          </div>

          {/* Back to Home */}
          {/* <div className="mt-4 text-center">
            <Link
              to="/"
              className="text-gray-500 hover:text-gray-700 text-sm"
            >
              ← Quay lại trang chủ
            </Link>
          </div> */}
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
