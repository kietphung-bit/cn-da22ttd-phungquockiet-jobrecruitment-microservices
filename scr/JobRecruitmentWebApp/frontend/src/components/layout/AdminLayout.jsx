import { Outlet, NavLink } from 'react-router-dom';
import { LayoutDashboard, Users, FileCheck, FolderTree, LogOut } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';

/**
 * AdminLayout Component
 * Layout specifically for Admin with sidebar navigation
 * 
 * Features:
 * - Sidebar with admin navigation links
 * - Protected layout for admins only (role: ADM)
 * - Responsive sidebar
 * - Active link highlighting
 * - Follows CompanyLayout pattern
 * 
 * Routes:
 * - /admin/dashboard - Tổng quan
 * - /admin/users - Quản lý người dùng
 * - /admin/jobs - Duyệt tin tuyển dụng
 * - /admin/categories - Quản lý danh mục
 */
const AdminLayout = () => {
  const { user, logout } = useAuth();

  const navLinks = [
    {
      to: '/admin/dashboard',
      icon: LayoutDashboard,
      label: 'Tổng quan',
      description: 'Dashboard và thống kê hệ thống'
    },
    {
      to: '/admin/users',
      icon: Users,
      label: 'Quản lý người dùng',
      description: 'Khóa/Mở khóa tài khoản'
    },
    {
      to: '/admin/jobs',
      icon: FileCheck,
      label: 'Duyệt tin tuyển dụng',
      description: 'Phê duyệt tin đăng'
    },
    {
      to: '/admin/categories',
      icon: FolderTree,
      label: 'Quản lý danh mục',
      description: 'CRUD danh mục công việc'
    }
  ];

  return (
    <div className="min-h-screen bg-neutral-50 flex">
      {/* Sidebar */}
      <aside className="w-64 bg-white border-r border-neutral-200 fixed h-full overflow-y-auto">
        {/* Logo & Admin Badge */}
        <div className="p-6 border-b border-neutral-200">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 bg-gradient-to-br from-red-500 to-red-600 rounded-lg flex items-center justify-center">
              <Users className="w-6 h-6 text-white" />
            </div>
            <div>
              <h2 className="font-bold text-neutral-900">Quản trị viên</h2>
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
                      ? 'bg-red-600 text-white'
                      : 'text-neutral-700 hover:bg-neutral-100'
                  }`
                }
              >
                <link.icon className="w-5 h-5 mt-0.5 flex-shrink-0" />
                <div className="flex-1 min-w-0">
                  <div className="font-medium text-sm">{link.label}</div>
                  <div className="text-xs opacity-75 mt-0.5">{link.description}</div>
                </div>
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

      {/* Main Content Area */}
      <main className="flex-1 ml-64">
        <div className="p-8">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default AdminLayout;
