import React, { useState } from 'react';
import { Mail, Phone, MapPin, Send } from 'lucide-react';
import { toast } from 'react-hot-toast';

/**
 * ContactPage - Trang liên hệ
 * 
 * Trang này hiển thị form liên hệ và thông tin công ty
 * 
 * Features:
 * - Form liên hệ (Tên, Email, Tin nhắn)
 * - Thông tin công ty (Địa chỉ, Email, Số điện thoại)
 * - Bản đồ (placeholder)
 */
const ContactPage = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    message: ''
  });

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    // Xác thực form đơn giản
    if (!formData.name || !formData.email || !formData.message) {
      toast.error('Vui lòng điền đầy đủ thông tin');
      return;
    }

    // TODO: Gọi API để gửi tin nhắn liên hệ
    toast.success('Tin nhắn của bạn đã được gửi thành công! Chúng tôi sẽ phản hồi sớm nhất có thể.');
    
    // Đặt lại form
    setFormData({
      name: '',
      email: '',
      message: ''
    });
  };

  return (
    <div className="min-h-screen bg-neutral-50">
      {/* Hero Section */}
      <div className="bg-primary text-white py-16">
        <div className="container-custom">
          <h1 className="text-4xl font-bold mb-4">Liên Hệ Với Chúng Tôi</h1>
          <p className="text-lg text-blue-100">
            Chúng tôi luôn sẵn sàng lắng nghe và hỗ trợ bạn
          </p>
        </div>
      </div>

      <div className="container-custom py-12">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
          {/* Contact Form */}
          <div className="bg-white rounded-lg shadow-md p-8">
            <h2 className="text-2xl font-bold text-neutral-900 mb-6">Gửi Tin Nhắn</h2>
            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Name */}
              <div>
                <label htmlFor="name" className="block text-sm font-medium text-neutral-700 mb-2">
                  Họ và Tên <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  id="name"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-colors"
                  placeholder="Nguyễn Văn A"
                  required
                />
              </div>

              {/* Email */}
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-neutral-700 mb-2">
                  Email <span className="text-red-500">*</span>
                </label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-colors"
                  placeholder="example@email.com"
                  required
                />
              </div>

              {/* Message */}
              <div>
                <label htmlFor="message" className="block text-sm font-medium text-neutral-700 mb-2">
                  Tin Nhắn <span className="text-red-500">*</span>
                </label>
                <textarea
                  id="message"
                  name="message"
                  value={formData.message}
                  onChange={handleInputChange}
                  rows={6}
                  className="w-full px-4 py-3 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-colors resize-none"
                  placeholder="Nhập tin nhắn của bạn..."
                  required
                />
              </div>

              {/* Submit Button */}
              <button
                type="submit"
                className="w-full bg-primary text-white py-3 px-6 rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center gap-2 font-semibold"
              >
                <Send className="w-5 h-5" />
                Gửi Tin Nhắn
              </button>
            </form>
          </div>

          {/* Contact Information */}
          <div className="space-y-8">
            <div className="bg-white rounded-lg shadow-md p-8">
              <h2 className="text-2xl font-bold text-neutral-900 mb-6">Thông Tin Liên Hệ</h2>
              <div className="space-y-6">
                {/* Email */}
                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
                    <Mail className="w-6 h-6 text-primary" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-neutral-900 mb-1">Email</h3>
                    <a 
                      href="mailto:info@jobrecruitment.vn" 
                      className="text-neutral-600 hover:text-primary transition-colors"
                    >
                      info@jobrecruitment.vn
                    </a>
                    <br />
                    <a 
                      href="mailto:support@jobrecruitment.vn" 
                      className="text-neutral-600 hover:text-primary transition-colors"
                    >
                      support@jobrecruitment.vn
                    </a>
                  </div>
                </div>

                {/* Phone */}
                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center flex-shrink-0">
                    <Phone className="w-6 h-6 text-green-600" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-neutral-900 mb-1">Điện Thoại</h3>
                    <a 
                      href="tel:+84901234567" 
                      className="text-neutral-600 hover:text-primary transition-colors"
                    >
                      +84 901 234 567
                    </a>
                    <br />
                    <a 
                      href="tel:+84287654321" 
                      className="text-neutral-600 hover:text-primary transition-colors"
                    >
                      +84 28 7654 321
                    </a>
                  </div>
                </div>

                {/* Address */}
                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center flex-shrink-0">
                    <MapPin className="w-6 h-6 text-red-600" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-neutral-900 mb-1">Địa Chỉ</h3>
                    <p className="text-neutral-600">
                      Số nhà 123, tên đường<br />
                      Phường , Quận <br />
                      Thành phố, Việt Nam
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Business Hours */}
            <div className="bg-white rounded-lg shadow-md p-8">
              <h2 className="text-2xl font-bold text-neutral-900 mb-6">Giờ Làm Việc</h2>
              <div className="space-y-3">
                <div className="flex justify-between items-center py-2 border-b border-neutral-200">
                  <span className="text-neutral-700 font-medium">Thứ Hai - Thứ Sáu</span>
                  <span className="text-neutral-600">8:00 - 18:00</span>
                </div>
                <div className="flex justify-between items-center py-2 border-b border-neutral-200">
                  <span className="text-neutral-700 font-medium">Thứ Bảy</span>
                  <span className="text-neutral-600">9:00 - 17:00</span>
                </div>
                <div className="flex justify-between items-center py-2">
                  <span className="text-neutral-700 font-medium">Chủ Nhật</span>
                  <span className="text-red-600 font-semibold">Đóng Cửa</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContactPage;
