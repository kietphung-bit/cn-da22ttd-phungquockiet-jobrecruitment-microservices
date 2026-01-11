import { Outlet, NavLink } from 'react-router-dom';
import { LayoutDashboard, Building2, Briefcase, FileText, LogOut, Users } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';

/**
 * CompanyLayout Component
 * Layout cho Nhà tuyển dụng (Doanh nghiệp) với thanh điều hướng bên
 * 
 * Tính năng:
 * - Thanh điều hướng bên với các liên kết điều hướng của công ty
 * - Layout bảo vệ chỉ dành cho nhà tuyển dụng (vai trò: DN)
 * - Thanh điều hướng đáp ứng
 * - Active link highlighting
 * 
 * Routes:
 * - /employer/dashboard - Tổng quan
 * - /employer/profile - Hồ sơ công ty
 * - /employer/jobs - Quản lý tin đăng
 * - /employer/applications - Hồ sơ ứng tuyển
 */
const CompanyLayout = () => {
  const { user, logout } = useAuth();

  const navLinks = [
    {
      to: '/employer/dashboard',
      icon: LayoutDashboard,
      label: 'Tổng quan',
      description: 'Bảng điều khiển chính'
    },
    {
      to: '/employer/profile',
      icon: Building2,
      label: 'Hồ sơ công ty',
      description: 'Quản lý thông tin công ty'
    },
    {
      to: '/employer/jobs',
      icon: Briefcase,
      label: 'Quản lý tin đăng',
      description: 'Quản lý tin tuyển dụng'
    },
    {
      to: '/employer/applications',
      icon: FileText,
      label: 'Hồ sơ ứng tuyển',
      description: 'Xem và duyệt hồ sơ'
    },
    {
      to: '/employer/talents',
      icon: Users,
      label: 'Tìm kiếm nhân tài',
      description: 'Tìm ứng viên phù hợp'
    }
  ];

  return (
    <div className="min-h-screen bg-neutral-50 flex">
      {/* Sidebar */}
      <aside className="w-64 bg-white border-r border-neutral-200 fixed h-full overflow-y-auto">
        {/* Logo & Company Name */}
        <div className="p-6 border-b border-neutral-200">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 bg-primary rounded-lg flex items-center justify-center">
              <Building2 className="w-6 h-6 text-white" />
            </div>
            <div>
              <h2 className="font-bold text-neutral-900">Nhà tuyển dụng</h2>
              <p className="text-xs text-neutral-500">{user?.email}</p>
            </div>
          </div>
        </div>

        {/* Navigation Links */}
        <nav className="p-4">
          <div className="space-y-1">
            {navLinks.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                className={({ isActive }) =>
                  `flex items-start gap-3 px-4 py-3 rounded-lg transition-colors ${
                    isActive
                      ? 'bg-primary text-white'
                      : 'text-neutral-700 hover:bg-neutral-100'
                  }`
                }
              >
                {({ isActive }) => (
                  <>
                    <link.icon className={`w-5 h-5 mt-0.5 flex-shrink-0 ${isActive ? 'text-white' : 'text-neutral-400'}`} />
                    <div className="flex-1">
                      <div className={`font-semibold text-sm ${isActive ? 'text-white' : 'text-neutral-900'}`}>
                        {link.label}
                      </div>
                      <div className={`text-xs mt-0.5 ${isActive ? 'text-blue-100' : 'text-neutral-500'}`}>
                        {link.description}
                      </div>
                    </div>
                  </>
                )}
              </NavLink>
            ))}
          </div>
        </nav>

        {/* Logout Button */}
        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-neutral-200 bg-white">
          <button
            onClick={logout}
            className="w-full flex items-center justify-center gap-2 px-4 py-3 text-red-600 hover:bg-red-50 rounded-lg transition-colors font-medium"
          >
            <LogOut className="w-5 h-5" />
            Đăng xuất
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="ml-64 flex-1">
        <Outlet />
        
        {/* Footer */}
        <footer className="border-t border-neutral-200 bg-white py-4 px-8">
          <div className="text-center text-sm text-neutral-600">
            © 2026 JobRecruitment. All rights reserved.
          </div>
        </footer>
      </div>
    </div>
  );
};

export default CompanyLayout;
