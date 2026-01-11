import React from 'react';
import { FileText, AlertTriangle, Users, Briefcase, UserCheck, Shield } from 'lucide-react';

/**
 * TermsOfServicePage - Trang Điều khoản dịch vụ
 * 
 * Trang này hiển thị các điều khoản và điều kiện sử dụng dịch vụ
 */
const TermsOfServicePage = () => {
  return (
    <div className="min-h-screen bg-neutral-50">
      {/* Hero Section */}
      <div className="bg-primary text-white py-16">
        <div className="container-custom">
          <div className="flex items-center gap-4 mb-4">
            <FileText className="w-12 h-12" />
            <h1 className="text-4xl font-bold">Điều Khoản Dịch Vụ</h1>
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
              Chào mừng bạn đến với Nền Tảng Tuyển Dụng Việc Làm. Bằng cách truy cập và sử dụng dịch vụ 
              của chúng tôi, bạn đồng ý tuân thủ các điều khoản và điều kiện được nêu dưới đây. Vui lòng 
              đọc kỹ trước khi sử dụng dịch vụ.
            </p>
          </section>

          {/* Section 1 */}
          <section className="mb-10">
            <div className="flex items-center gap-3 mb-4">
              <Shield className="w-6 h-6 text-primary" />
              <h2 className="text-2xl font-bold text-neutral-900">1. Tính Chất Nền Tảng</h2>
            </div>
            <div className="prose prose-neutral max-w-none">
              <p className="text-neutral-700 leading-relaxed mb-4">
                Nền tảng của chúng tôi hoạt động như một trung gian kết nối giữa nhà tuyển dụng và ứng viên. 
                Chúng tôi:
              </p>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Không phải là nhà tuyển dụng hay đại diện của bất kỳ công ty nào</li>
                <li>Không chịu trách nhiệm về nội dung tin tuyển dụng do nhà tuyển dụng đăng</li>
                <li>Không đảm bảo tính chính xác của thông tin được cung cấp bởi người dùng</li>
                <li>Không can thiệp vào quá trình tuyển dụng giữa nhà tuyển dụng và ứng viên</li>
                <li>Không chịu trách nhiệm về các tranh chấp phát sinh từ quan hệ tuyển dụng</li>
              </ul>
            </div>
          </section>

          {/* Section 2 */}
          <section className="mb-10">
            <div className="flex items-center gap-3 mb-4">
              <Users className="w-6 h-6 text-primary" />
              <h2 className="text-2xl font-bold text-neutral-900">2. Trách Nhiệm Người Dùng</h2>
            </div>
            <div className="prose prose-neutral max-w-none">
              <p className="text-neutral-700 leading-relaxed mb-4">
                Khi sử dụng dịch vụ, bạn cam kết:
              </p>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Cung cấp thông tin chính xác và trung thực</li>
                <li>Không đăng nội dung vi phạm pháp luật</li>
                <li>Không spam hoặc gửi tin nhắn rác</li>
                <li>Không sao chép hoặc sử dụng dữ liệu của người khác trái phép</li>
                <li>Bảo mật thông tin đăng nhập của bạn</li>
                <li>Thông báo ngay cho chúng tôi nếu phát hiện hành vi gian lận</li>
                <li>Tuân thủ mọi quy định của pháp luật Việt Nam</li>
              </ul>
            </div>
          </section>

          {/* Section 3 */}
          <section className="mb-10">
            <div className="flex items-center gap-3 mb-4">
              <Briefcase className="w-6 h-6 text-primary" />
              <h2 className="text-2xl font-bold text-neutral-900">3. Trách Nhiệm Nhà Tuyển Dụng</h2>
            </div>
            <div className="prose prose-neutral max-w-none">
              <p className="text-neutral-700 leading-relaxed mb-4">
                Nhà tuyển dụng cam kết:
              </p>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Đăng tin tuyển dụng chính xác và trung thực</li>
                <li>Không đăng tin tuyển dụng lừa đảo hoặc gian lận</li>
                <li>Không yêu cầu ứng viên nộp phí hoặc tiền đặt cọc</li>
                <li>Không phân biệt đối xử với ứng viên</li>
                <li>Bảo mật thông tin cá nhân của ứng viên</li>
                <li>Tuân thủ Luật Lao động Việt Nam</li>
                <li>Chịu trách nhiệm về nội dung tin đăng của mình</li>
              </ul>
              
              <h3 className="text-lg font-semibold text-neutral-800 mt-6 mb-2">Nội dung cấm đăng:</h3>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Việc làm yêu cầu hoạt động bất hợp pháp</li>
                <li>Việc làm có nội dung khiêu dâm hoặc bạo lực</li>
                <li>Việc làm đa cấp hoặc tài chính lừa đảo</li>
                <li>Việc làm không trả lương hoặc trả lương thấp hơn mức tối thiểu</li>
              </ul>
            </div>
          </section>

          {/* Section 4 */}
          <section className="mb-10">
            <div className="flex items-center gap-3 mb-4">
              <UserCheck className="w-6 h-6 text-primary" />
              <h2 className="text-2xl font-bold text-neutral-900">4. Trách Nhiệm Ứng Viên</h2>
            </div>
            <div className="prose prose-neutral max-w-none">
              <p className="text-neutral-700 leading-relaxed mb-4">
                Ứng viên cam kết:
              </p>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Cung cấp thông tin CV chính xác và trung thực</li>
                <li>Không giả mạo bằng cấp hoặc kinh nghiệm làm việc</li>
                <li>Không đăng tin tìm việc có nội dung không phù hợp</li>
                <li>Tôn trọng nhà tuyển dụng trong quá trình ứng tuyển</li>
                <li>Thông báo nếu không còn quan tâm đến vị trí đã ứng tuyển</li>
                <li>Không lợi dụng thông tin của nhà tuyển dụng cho mục đích xấu</li>
              </ul>
            </div>
          </section>

          {/* Section 5 */}
          <section className="mb-10">
            <div className="flex items-center gap-3 mb-4">
              <AlertTriangle className="w-6 h-6 text-primary" />
              <h2 className="text-2xl font-bold text-neutral-900">5. Kiểm Duyệt Nội Dung</h2>
            </div>
            <div className="prose prose-neutral max-w-none">
              <h3 className="text-lg font-semibold text-neutral-800 mt-4 mb-2">Cơ chế kiểm duyệt:</h3>
              <p className="text-neutral-700 leading-relaxed mb-4">
                Chúng tôi áp dụng cơ chế <strong>kiểm duyệt sau (Post-Moderation)</strong>:
              </p>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Tin tuyển dụng và tin tìm việc được đăng ngay lập tức sau khi tạo</li>
                <li>Admin có thể xem xét và gỡ bỏ nội dung vi phạm sau khi phát hiện</li>
                <li>Người dùng có thể báo cáo nội dung không phù hợp</li>
                <li>Chúng tôi không kiểm tra trước nội dung trước khi đăng</li>
              </ul>

              <h3 className="text-lg font-semibold text-neutral-800 mt-6 mb-2">Quyền của Admin:</h3>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Xóa tin đăng vi phạm điều khoản dịch vụ</li>
                <li>Từ chối hoặc xóa tin tuyển dụng không phù hợp</li>
                <li>Khóa tài khoản người dùng vi phạm nghiêm trọng</li>
                <li>Yêu cầu chỉnh sửa nội dung không phù hợp</li>
              </ul>
            </div>
          </section>

          {/* Section 6 */}
          <section className="mb-10">
            <div className="flex items-center gap-3 mb-4">
              <Shield className="w-6 h-6 text-primary" />
              <h2 className="text-2xl font-bold text-neutral-900">6. Sở Hữu Trí Tuệ</h2>
            </div>
            <div className="prose prose-neutral max-w-none">
              <p className="text-neutral-700 leading-relaxed mb-4">
                Tất cả nội dung trên nền tảng thuộc quyền sở hữu của:
              </p>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Nền tảng (giao diện, logo, thiết kế)</li>
                <li>Người dùng (CV, tin tuyển dụng, tin tìm việc)</li>
              </ul>
              <p className="text-neutral-700 leading-relaxed mt-4">
                Bạn không được sao chép, phân phối hoặc sử dụng nội dung của người khác mà không có sự đồng ý.
              </p>
            </div>
          </section>

          {/* Section 7 */}
          <section className="mb-10">
            <div className="flex items-center gap-3 mb-4">
              <FileText className="w-6 h-6 text-primary" />
              <h2 className="text-2xl font-bold text-neutral-900">7. Giới Hạn Trách Nhiệm</h2>
            </div>
            <div className="prose prose-neutral max-w-none">
              <p className="text-neutral-700 leading-relaxed mb-4">
                Chúng tôi không chịu trách nhiệm về:
              </p>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Tính chính xác của thông tin do người dùng cung cấp</li>
                <li>Kết quả tuyển dụng giữa nhà tuyển dụng và ứng viên</li>
                <li>Các tranh chấp phát sinh từ hợp đồng lao động</li>
                <li>Thiệt hại gián tiếp do sử dụng dịch vụ</li>
                <li>Gián đoạn dịch vụ do sự cố kỹ thuật hoặc bảo trì</li>
              </ul>
            </div>
          </section>

          {/* Section 8 */}
          <section className="mb-10">
            <div className="flex items-center gap-3 mb-4">
              <AlertTriangle className="w-6 h-6 text-primary" />
              <h2 className="text-2xl font-bold text-neutral-900">8. Chấm Dứt Dịch Vụ</h2>
            </div>
            <div className="prose prose-neutral max-w-none">
              <p className="text-neutral-700 leading-relaxed mb-4">
                Chúng tôi có quyền:
              </p>
              <ul className="list-disc list-inside space-y-2 text-neutral-700 ml-4">
                <li>Tạm ngưng hoặc chấm dứt tài khoản của bạn nếu vi phạm điều khoản</li>
                <li>Thay đổi hoặc ngừng cung cấp dịch vụ bất cứ lúc nào</li>
                <li>Thay đổi điều khoản dịch vụ (sẽ thông báo trước)</li>
              </ul>
            </div>
          </section>

          {/* Section 9 */}
          <section className="mb-10">
            <div className="flex items-center gap-3 mb-4">
              <FileText className="w-6 h-6 text-primary" />
              <h2 className="text-2xl font-bold text-neutral-900">9. Luật Áp Dụng</h2>
            </div>
            <div className="prose prose-neutral max-w-none">
              <p className="text-neutral-700 leading-relaxed">
                Các điều khoản này được điều chỉnh bởi pháp luật Việt Nam. Mọi tranh chấp phát sinh sẽ được 
                giải quyết tại Tòa án có thẩm quyền tại Việt Nam.
              </p>
            </div>
          </section>

          {/* Disclaimer */}
          <section className="bg-yellow-50 border-l-4 border-yellow-500 p-6 rounded-r-lg mb-10">
            <div className="flex gap-3">
              <AlertTriangle className="w-6 h-6 text-yellow-600 flex-shrink-0 mt-1" />
              <div>
                <h3 className="font-bold text-neutral-900 mb-2">Tuyên bố quan trọng:</h3>
                <p className="text-neutral-700 leading-relaxed">
                  Nền tảng chỉ đóng vai trò trung gian kết nối. Người dùng hoàn toàn chịu trách nhiệm về 
                  nội dung và hành vi của mình trên nền tảng. Chúng tôi khuyến khích bạn kiểm tra kỹ thông tin 
                  trước khi ứng tuyển hoặc tuyển dụng.
                </p>
              </div>
            </div>
          </section>

          {/* Contact Section */}
          <section className="bg-blue-50 rounded-lg p-6 mt-12">
            <h2 className="text-xl font-bold text-neutral-900 mb-4">Liên Hệ</h2>
            <p className="text-neutral-700 leading-relaxed mb-4">
              Nếu bạn có bất kỳ câu hỏi nào về Điều khoản dịch vụ, vui lòng liên hệ với chúng tôi:
            </p>
            <ul className="space-y-2 text-neutral-700">
              <li><strong>Email:</strong> support@jobrecruitment.vn</li>
              <li><strong>Điện thoại:</strong> +84 901 234 567</li>
              <li><strong>Địa chỉ:</strong> Số nhà 123, tên đường, Quận 1, Thành phố, Việt Nam</li>
            </ul>
          </section>
        </div>
      </div>
    </div>
  );
};

export default TermsOfServicePage;
