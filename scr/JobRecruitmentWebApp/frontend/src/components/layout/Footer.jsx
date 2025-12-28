import React from 'react';
import { Link } from 'react-router-dom';
import { Facebook, Twitter, Linkedin, Mail, Phone, MapPin } from 'lucide-react';

/**
 * Footer Component
 * 4 Columns: Website Info/Logo, Contact Links, For Candidates, For Employers
 * Bottom: Copyright text
 */
const Footer = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-neutral-900 text-white mt-auto">
      <div className="container-custom py-12">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          {/* Column 1: Website Info/Logo */}
          <div>
            <div className="flex items-center space-x-2 mb-4">
              <div className="w-10 h-10 bg-primary rounded-full flex items-center justify-center">
                <span className="text-white font-bold text-xl">JR</span>
              </div>
              <span className="text-xl font-bold">Tuyển Dụng Việc Làm</span>
            </div>
            <p className="text-neutral-400 mb-4 text-sm">
              Kết nối ứng viên tài năng với nhà tuyển dụng uy tín. Công việc mơ ước của bạn chỉ cách một cú nhấp chuột.
            </p>
            <div className="flex space-x-4">
              <a
                href="#"
                className="text-neutral-400 hover:text-primary transition-colors"
                aria-label="Facebook"
              >
                <Facebook className="w-5 h-5" />
              </a>
              <a
                href="#"
                className="text-neutral-400 hover:text-primary transition-colors"
                aria-label="Twitter"
              >
                <Twitter className="w-5 h-5" />
              </a>
              <a
                href="#"
                className="text-neutral-400 hover:text-primary transition-colors"
                aria-label="LinkedIn"
              >
                <Linkedin className="w-5 h-5" />
              </a>
            </div>
          </div>

          {/* Column 2: Contact Links */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Liên Hệ</h3>
            <ul className="space-y-3">
              <li>
                <a
                  href="mailto:info@jobrecruitment.com"
                  className="text-neutral-400 hover:text-white transition-colors flex items-center text-sm"
                >
                  <Mail className="w-4 h-4 mr-2" />
                  info@jobrecruitment.com
                </a>
              </li>
              <li>
                <a
                  href="tel:+1234567890"
                  className="text-neutral-400 hover:text-white transition-colors flex items-center text-sm"
                >
                  <Phone className="w-4 h-4 mr-2" />
                  +1 (234) 567-890
                </a>
              </li>
              <li>
                <a
                  href="#"
                  className="text-neutral-400 hover:text-white transition-colors flex items-start text-sm"
                >
                  <MapPin className="w-4 h-4 mr-2 mt-1 flex-shrink-0" />
                  <span>123 Business Street, Suite 100<br />City, State 12345</span>
                </a>
              </li>
            </ul>
          </div>

          {/* Column 3: For Candidates */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Dành Cho Ứng Viên</h3>
            <ul className="space-y-3">
              <li>
                <Link
                  to="/jobs"
                  className="text-neutral-400 hover:text-white transition-colors text-sm"
                >
                  Xem việc làm
                </Link>
              </li>
              <li>
                <Link
                  to="/companies"
                  className="text-neutral-400 hover:text-white transition-colors text-sm"
                >
                  Công ty
                </Link>
              </li>
              <li>
                <Link
                  to="/profile"
                  className="text-neutral-400 hover:text-white transition-colors text-sm"
                >
                  Hồ sơ của tôi
                </Link>
              </li>
              <li>
                <Link
                  to="/applied-jobs"
                  className="text-neutral-400 hover:text-white transition-colors text-sm"
                >
                  Đăng ký tài khoản
                </Link>
              </li>
            </ul>
          </div>

          {/* Column 4: For Employers */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Dành Cho Nhà Tuyển Dụng</h3>
            <ul className="space-y-3">
              <li>
                <Link
                  to="/employer/dashboard"
                  className="text-neutral-400 hover:text-white transition-colors text-sm"
                >
                  Đăng tin tuyển dụng
                </Link>
              </li>
              <li>
                <Link
                  to="/employer/jobs"
                  className="text-neutral-400 hover:text-white transition-colors text-sm"
                >
                  Quản lý tuyển dụng
                </Link>
              </li>
              <li>
                <Link
                  to="/employer/candidates"
                  className="text-neutral-400 hover:text-white transition-colors text-sm"
                >
                  Tìm ứng viên
                </Link>
              </li>
              <li>
                <Link
                  to="/pricing"
                  className="text-neutral-400 hover:text-white transition-colors text-sm"
                >
                  Đăng ký doanh nghiệp
                </Link>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom: Copyright */}
        <div className="border-t border-neutral-800 mt-8 pt-8 flex flex-col md:flex-row justify-between items-center">
          <p className="text-neutral-400 text-sm">
            © {currentYear} Nền Tảng Tuyển Dụng Việc Làm. Bảo lưu mọi quyền.
          </p>
          <div className="flex space-x-6 mt-4 md:mt-0">
            <Link
              to="/privacy"
              className="text-neutral-400 hover:text-white text-sm transition-colors"
            >
              Chính Sách Bảo Mật
            </Link>
            <Link
              to="/terms"
              className="text-neutral-400 hover:text-white text-sm transition-colors"
            >
              Điều Khoản Dịch Vụ
            </Link>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
