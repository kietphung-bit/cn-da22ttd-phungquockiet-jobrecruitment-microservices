import React from 'react';
import { Shield, Lock, Eye, Database, Bell, UserCheck } from 'lucide-react';

/**
 * PrivacyPolicyPage - Trang Chính sách bảo mật
 * 
 * Trang này hiển thị chính sách bảo mật của nền tảng
 */
const PrivacyPolicyPage = () => {
  return (
    <div className="min-h-screen bg-neutral-50">
      {/* Hero Section */}
      <div className="bg-primary text-white py-16">
        <div className="container-custom">
          <div className="flex items-center gap-4 mb-4">
            <Shield className="w-12 h-12" />
            <h1 className="text-4xl font-bold">Chính Sách Bảo Mật</h1>
          </div>
          <p className="text-lg text-blue-100">
            Cập nhật lần cuối: Tháng 1, 2026
          </p>
        </div>
      </div>

      {/* Content */}
      <div className="container-custom py-12">
        <div className="max-w-4xl mx-auto bg-white rounded-lg shadow-md p-8 md:p-12">
          {/* Introduction */}
          <section className="mb-12">
            <p className="text-lg text-neutral-700 leading-relaxed">
              Chào mừng bạn đến với Nền Tảng Tuyển Dụng Việc Làm. Chúng tôi cam kết bảo vệ quyền riêng tư 
              và thông tin cá nhân của bạn. Chính sách bảo mật này giải thích cách chúng tôi thu thập, 
              sử dụng, lưu trữ và bảo vệ thông tin của bạn khi sử dụng dịch vụ của chúng tôi.
            </p>
          </section>

          {/* Section 1 */}
          <section className="mb-10">
            <div className="flex items-center gap-3 mb-4">
              <Database className="w-6 h-6 text-primary" />
              <h2 className="text-2xl font-bold text-neutral-900">1. Thông Tin Chúng Tôi Thu Thập</h2>
            </div>
            <div className="prose prose-neutral max-w-none">
              <h3 className="text-lg font-semibold text-neutral-800 mt-4 mb-2">1.1. Thông Tin Cá Nhân</h3>
              <p className="text-neutral-700 leading-relaxed mb-4">
                Khi bạn đăng ký tài khoản, chúng tôi có thể thu thập các thông tin sau:
              </p>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Họ và tên đầy đủ</li>
                <li>Địa chỉ email</li>
                <li>Số điện thoại</li>
                <li>Ngày sinh</li>
                <li>Giới tính</li>
                <li>Địa chỉ cư trú</li>
              </ul>

              <h3 className="text-lg font-semibold text-neutral-800 mt-6 mb-2">1.2. Thông Tin Nghề Nghiệp</h3>
              <p className="text-neutral-700 leading-relaxed mb-4">
                Đối với ứng viên tìm việc:
              </p>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Học vấn và bằng cấp</li>
                <li>Kinh nghiệm làm việc</li>
                <li>Kỹ năng chuyên môn</li>
                <li>CV và thư xin việc</li>
                <li>Mức lương mong muốn</li>
              </ul>

              <h3 className="text-lg font-semibold text-neutral-800 mt-6 mb-2">1.3. Thông Tin Doanh Nghiệp</h3>
              <p className="text-neutral-700 leading-relaxed mb-4">
                Đối với nhà tuyển dụng:
              </p>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Tên công ty và mã số thuế</li>
                <li>Địa chỉ văn phòng</li>
                <li>Thông tin người đại diện</li>
                <li>Lĩnh vực hoạt động</li>
              </ul>
            </div>
          </section>

          {/* Section 2 */}
          <section className="mb-10">
            <div className="flex items-center gap-3 mb-4">
              <Eye className="w-6 h-6 text-primary" />
              <h2 className="text-2xl font-bold text-neutral-900">2. Cách Chúng Tôi Sử Dụng Thông Tin</h2>
            </div>
            <div className="prose prose-neutral max-w-none">
              <p className="text-neutral-700 leading-relaxed mb-4">
                Chúng tôi sử dụng thông tin của bạn cho các mục đích sau:
              </p>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Cung cấp và duy trì dịch vụ tuyển dụng</li>
                <li>Kết nối ứng viên với nhà tuyển dụng phù hợp</li>
                <li>Gửi thông báo về công việc mới và cơ hội việc làm</li>
                <li>Cải thiện trải nghiệm người dùng</li>
                <li>Phân tích và thống kê sử dụng dịch vụ</li>
                <li>Phát hiện và ngăn chặn gian lận</li>
                <li>Tuân thủ các yêu cầu pháp lý</li>
              </ul>
            </div>
          </section>

          {/* Section 3 */}
          <section className="mb-10">
            <div className="flex items-center gap-3 mb-4">
              <Lock className="w-6 h-6 text-primary" />
              <h2 className="text-2xl font-bold text-neutral-900">3. Bảo Mật Thông Tin</h2>
            </div>
            <div className="prose prose-neutral max-w-none">
              <p className="text-neutral-700 leading-relaxed mb-4">
                Chúng tôi áp dụng các biện pháp bảo mật sau để bảo vệ thông tin của bạn:
              </p>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Mã hóa dữ liệu bằng SSL/TLS</li>
                <li>Lưu trữ mật khẩu dưới dạng băm (hashing)</li>
                <li>Giới hạn quyền truy cập thông tin</li>
                <li>Sao lưu dữ liệu định kỳ</li>
                <li>Giám sát hệ thống 24/7</li>
                <li>Đào tạo nhân viên về bảo mật thông tin</li>
              </ul>
            </div>
          </section>

          {/* Section 4 */}
          <section className="mb-10">
            <div className="flex items-center gap-3 mb-4">
              <UserCheck className="w-6 h-6 text-primary" />
              <h2 className="text-2xl font-bold text-neutral-900">4. Chia Sẻ Thông Tin</h2>
            </div>
            <div className="prose prose-neutral max-w-none">
              <p className="text-neutral-700 leading-relaxed mb-4">
                Chúng tôi chỉ chia sẻ thông tin của bạn trong các trường hợp sau:
              </p>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Với sự đồng ý của bạn</li>
                <li>Với nhà tuyển dụng khi bạn ứng tuyển vào vị trí của họ</li>
                <li>Với đối tác cung cấp dịch vụ (thanh toán, phân tích, email)</li>
                <li>Khi được yêu cầu bởi cơ quan chức năng</li>
                <li>Để bảo vệ quyền lợi của chúng tôi và người dùng khác</li>
              </ul>
            </div>
          </section>

          {/* Section 5 */}
          <section className="mb-10">
            <div className="flex items-center gap-3 mb-4">
              <Bell className="w-6 h-6 text-primary" />
              <h2 className="text-2xl font-bold text-neutral-900">5. Quyền Của Bạn</h2>
            </div>
            <div className="prose prose-neutral max-w-none">
              <p className="text-neutral-700 leading-relaxed mb-4">
                Bạn có các quyền sau đối với thông tin cá nhân của mình:
              </p>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Truy cập và xem thông tin cá nhân</li>
                <li>Chỉnh sửa hoặc cập nhật thông tin</li>
                <li>Xóa tài khoản và dữ liệu</li>
                <li>Từ chối nhận email marketing</li>
                <li>Yêu cầu sao chép dữ liệu của bạn</li>
                <li>Khiếu nại về việc xử lý dữ liệu</li>
              </ul>
            </div>
          </section>

          {/* Section 6 */}
          <section className="mb-10">
            <div className="flex items-center gap-3 mb-4">
              <Shield className="w-6 h-6 text-primary" />
              <h2 className="text-2xl font-bold text-neutral-900">6. Cookie và Công Nghệ Theo Dõi</h2>
            </div>
            <div className="prose prose-neutral max-w-none">
              <p className="text-neutral-700 leading-relaxed mb-4">
                Chúng tôi sử dụng cookie và các công nghệ tương tự để:
              </p>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Ghi nhớ thông tin đăng nhập của bạn</li>
                <li>Phân tích cách bạn sử dụng dịch vụ</li>
                <li>Cá nhân hóa nội dung và quảng cáo</li>
                <li>Cải thiện hiệu suất trang web</li>
              </ul>
              <p className="text-neutral-700 leading-relaxed mt-4">
                Bạn có thể quản lý cookie thông qua cài đặt trình duyệt của mình.
              </p>
            </div>
          </section>

          {/* Contact Section */}
          <section className="bg-blue-50 rounded-lg p-6 mt-12">
            <h2 className="text-xl font-bold text-neutral-900 mb-4">Liên Hệ</h2>
            <p className="text-neutral-700 leading-relaxed mb-4">
              Nếu bạn có bất kỳ câu hỏi nào về Chính sách bảo mật này, vui lòng liên hệ với chúng tôi:
            </p>
            <ul className="space-y-2 text-neutral-700">
              <li><strong>Email:</strong> privacy@jobrecruitment.vn</li>
              <li><strong>Điện thoại:</strong> +84 901 234 567</li>
              <li><strong>Địa chỉ:</strong> Số nhà 123, tên đường, Quận 1, Thành phố, Việt Nam</li>
            </ul>
          </section>
        </div>
      </div>
    </div>
  );
};

export default PrivacyPolicyPage;
