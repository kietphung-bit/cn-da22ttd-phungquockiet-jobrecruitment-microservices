import React, { useState } from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { User, FileText, Clock, Shield, Menu, X, Briefcase, Bookmark } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import Navbar from './Navbar';
import Footer from './Footer';

/**
 * CandidateLayout Component
 * Layout wrapper cho tất cả các trang dashboard của ứng viên (/candidate/*)
 * 
 * Cấu trúc:
 * - Trên cùng: Thanh điều hướng toàn cục (với dropdown người dùng để đăng xuất)
 * - Giữa: Lưới dashboard (Thanh điều hướng bên trái + Nội dung chính bên phải)
 * - Dưới cùng: Chân trang toàn cục
 * 
 * Tính năng:
 * - Thanh điều hướng bên có chiều rộng cố định với menu điều hướng dọc
 * - Khu vực nội dung chính với <Outlet /> (flex-grow)
 * - Thiết kế đáp ứng với menu hamburger trên di động
 * - Hiển thị thông tin người dùng ở đầu thanh điều hướng bên
 * - Khoảng cách nút ổn định (không nhảy/đè chồng)
 */
const CandidateLayout = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const location = useLocation();
  const { user } = useAuth();

  // Các mục menu phù hợp với routes của ứng viên
  const menuItems = [
    {
      icon: User,
      label: 'Thông tin cá nhân',
      path: '/candidate/profile',
    },
    {
      icon: FileText,
      label: 'Danh sách hồ sơ',
      path: '/candidate/cv-manager',
    },
    {
      icon: Briefcase,
      label: 'Tin đăng tìm việc',
      path: '/candidate/my-posts',
    },
    {
      icon: Clock,
      label: 'Lịch sử nộp đơn',
      path: '/candidate/applications',
    },
    {
      icon: Bookmark,
      label: 'Việc làm đã lưu',
      path: '/candidate/saved-jobs',
    },
    {
      icon: Shield,
      label: 'Bảo mật',
      path: '/candidate/security',
    },
  ];

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  const isActive = (path) => location.pathname === path;

  return (
    <div className="flex flex-col min-h-screen">
      {/* Top: Global Navbar */}
      <Navbar />

      {/* Middle: Dashboard Grid (Sidebar + Main Content) */}
      <div className="flex flex-1">
        {/* Mobile Menu Button - Only visible on mobile */}
        <div className="lg:hidden fixed top-20 left-4 z-50">
          <button
            onClick={toggleMobileMenu}
            className="p-2 bg-white rounded-lg shadow-md hover:bg-neutral-100 transition-colors"
            aria-label="Toggle sidebar menu"
          >
            {isMobileMenuOpen ? (
              <X className="w-6 h-6 text-neutral-700" />
            ) : (
              <Menu className="w-6 h-6 text-neutral-700" />
            )}
          </button>
        </div>

        {/* Left: Sidebar (Fixed width: w-64) */}
        <aside
          className={`
            fixed lg:static top-20 lg:top-0 bottom-0 left-0 z-40
            w-64 bg-white border-r border-neutral-200
            transform transition-transform duration-200 ease-in-out
            overflow-y-auto
            ${isMobileMenuOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
          `}
        >
          {/* User Info Section */}
          <div className="p-6 border-b border-neutral-200">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 bg-primary text-white rounded-full flex items-center justify-center font-semibold text-lg flex-shrink-0">
                {user?.candidateName?.charAt(0)?.toUpperCase() || 'U'}
              </div>
              <div className="flex-1 min-w-0">
                <h3 className="text-sm font-semibold text-neutral-900 truncate">
                  {user?.candidateName || 'Ứng viên'}
                </h3>
                <p className="text-xs text-neutral-500 truncate">
                  {user?.email || user?.username}
                </p>
              </div>
            </div>
          </div>

          {/* Navigation Menu - Fixed spacing to prevent jumps */}
          <nav className="p-4">
            <ul className="flex flex-col gap-2">
              {menuItems.map((item) => {
                const Icon = item.icon;
                const active = isActive(item.path);

                return (
                  <li key={item.path}>
                    <Link
                      to={item.path}
                      onClick={() => setIsMobileMenuOpen(false)}
                      className={`
                        flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200
                        ${
                          active
                            ? 'bg-primary text-white shadow-md'
                            : 'text-neutral-700 hover:bg-neutral-100'
                        }
                      `}
                    >
                      <Icon className="w-5 h-5 flex-shrink-0" />
                      <span className="font-medium">{item.label}</span>
                    </Link>
                  </li>
                );
              })}
            </ul>
          </nav>

          {/* Note: Logout button removed - Use Navbar dropdown instead */}
        </aside>

        {/* Mobile Overlay */}
        {isMobileMenuOpen && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 z-30 lg:hidden"
            onClick={() => setIsMobileMenuOpen(false)}
          />
        )}

        {/* Right: Main Content (Flex-grow) */}
        <main className="flex-1 bg-neutral-50 p-6 lg:p-8 overflow-x-hidden">
          <div className="max-w-7xl mx-auto">
            <Outlet />
          </div>
        </main>
      </div>

      {/* Bottom: Global Footer */}
      <Footer />
    </div>
  );
};

export default CandidateLayout;
