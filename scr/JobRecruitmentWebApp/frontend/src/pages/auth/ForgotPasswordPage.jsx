import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Mail, ArrowLeft, CheckCircle, AlertCircle } from 'lucide-react';

/**
 * ForgotPasswordPage Component
 * Xử lý yêu cầu đặt lại mật khẩu
 * 
 * Tính năng:
 * - Nhập email để đặt lại mật khẩu
 * - Xác thực
 * - Thông báo thành công/lỗi
 * 
 * Lưu ý: Đây chỉ là phần giao diện, chưa có chức năng backend thực sự
 * Cần tạo endpoint backend để thực hiện đặt lại mật khẩu thực tế
 */
const ForgotPasswordPage = () => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {      
      // Gọi API gửi email đặt lại mật khẩu
      // await authService.forgotPassword(email);
      await new Promise(resolve => setTimeout(resolve, 1500));

      // Hiện tại, luôn hiển thị thành công
      setSuccess(true);
      
    } catch (err) {
      setError(
        err.response?.data?.message || 
        'Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-custom py-16">
      <div className="max-w-md mx-auto space-y-8">
        {/* Back to Login Link */}
        <div>
          <Link
            to="/login"
            className="inline-flex items-center text-sm text-primary hover:text-primary-600 font-medium"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Quay lại đăng nhập
          </Link>
        </div>

        {/* Header */}
        <div className="text-center">
          <div className="mx-auto h-16 w-16 bg-primary rounded-full flex items-center justify-center">
            <Mail className="h-8 w-8 text-white" />
          </div>
          <h2 className="mt-6 text-3xl font-bold text-neutral-900">
            Quên Mật Khẩu?
          </h2>
          <p className="mt-2 text-sm text-neutral-600">
            Nhập email của bạn và chúng tôi sẽ gửi hướng dẫn đặt lại mật khẩu
          </p>
        </div>

        {/* Form Card */}
        <div className="bg-white rounded-xl shadow-lg p-8">
          {!success ? (
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
                  Địa chỉ email
                </label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
                  <input
                    id="email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full pl-11 pr-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                    placeholder="example@email.com"
                    required
                    autoComplete="email"
                  />
                </div>
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
                    Đang gửi...
                  </>
                ) : (
                  'Gửi link đặt lại mật khẩu'
                )}
              </button>

              {/* Back to Login */}
              <div className="text-center">
                <p className="text-sm text-neutral-600">
                  Nhớ mật khẩu?{' '}
                  <Link to="/login" className="text-primary font-semibold hover:text-primary-600">
                    Đăng nhập ngay
                  </Link>
                </p>
              </div>
            </form>
          ) : (
            // Success Message
            <div className="text-center space-y-4">
              <div className="mx-auto w-16 h-16 bg-green-100 rounded-full flex items-center justify-center">
                <CheckCircle className="w-10 h-10 text-green-600" />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-neutral-900 mb-2">
                  Email đã được gửi!
                </h3>
                <p className="text-neutral-600">
                  Chúng tôi đã gửi hướng dẫn đặt lại mật khẩu đến email <strong>{email}</strong>
                </p>
                <p className="text-sm text-neutral-500 mt-2">
                  Vui lòng kiểm tra hộp thư đến (hoặc thư rác) và làm theo hướng dẫn.
                </p>
              </div>

              {/* Additional Help */}
              <div className="pt-4 border-t border-neutral-200">
                <p className="text-sm text-neutral-600">
                  Không nhận được email?{' '}
                  <button
                    onClick={() => setSuccess(false)}
                    className="text-primary hover:text-primary-600 font-medium"
                  >
                    Gửi lại
                  </button>
                </p>
              </div>

              <Link
                to="/login"
                className="inline-block mt-4 text-primary hover:text-primary-600 font-semibold"
              >
                Quay lại đăng nhập
              </Link>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ForgotPasswordPage;
