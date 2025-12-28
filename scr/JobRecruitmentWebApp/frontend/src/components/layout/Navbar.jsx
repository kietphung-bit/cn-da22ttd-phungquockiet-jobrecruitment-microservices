import React, { useState, useRef, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Search, Menu, X, User, Settings, Bookmark, LogOut, ChevronDown } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import logo from '../../assets/images/logo/logo.png';

/**
 * Navbar Component
 * Left: Logo (Actual logo image)
 * Center: Links (Home, Jobs, Companies, Contact)
 * Right: Search input (with icon), Auth Buttons OR User Avatar with Dropdown
 * Responsive with hamburger menu for mobile
 * 
 * Auth States:
 * - Guest: Show "Login" / "Register" buttons
 * - Authenticated: Show User Avatar Icon with dropdown menu
 */
const Navbar = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const userMenuRef = useRef(null);

  // Close user menu when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (userMenuRef.current && !userMenuRef.current.contains(event.target)) {
        setIsUserMenuOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  const toggleUserMenu = () => {
    setIsUserMenuOpen(!isUserMenuOpen);
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/jobs?keyword=${encodeURIComponent(searchQuery)}`);
      setSearchQuery('');
      setIsMobileMenuOpen(false);
    }
  };

  const handleLogout = () => {
    logout();
    setIsUserMenuOpen(false);
    setIsMobileMenuOpen(false);
  };

  // Get user display name or default
  const getUserDisplayName = () => {
    if (!user) return 'Người dùng';
    return user.candidateName || user.companyName || user.username || 'Người dùng';
  };

  // Get user avatar/initials
  const getUserAvatar = () => {
    const displayName = getUserDisplayName();
    return displayName.charAt(0).toUpperCase();
  };

  return (
    <nav className="bg-white shadow-lg sticky top-0 z-50 border-b border-neutral-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-20">
          {/* Left: Logo */}
          <Link to="/" className="flex items-center space-x-3 flex-shrink-0">
            <img 
              src={logo} 
              alt="Tuyển Dụng Việc Làm Logo" 
              className="w-12 h-12 object-contain"
            />
          </Link>

          {/* Center: Navigation Links */}
          <div className="hidden lg:flex items-center space-x-8 flex-1 justify-center">
            <Link
              to="/"
              className="text-neutral-700 hover:text-primary font-semibold text-base transition-colors duration-200 relative after:absolute after:bottom-0 after:left-0 after:w-0 after:h-0.5 after:bg-primary after:transition-all hover:after:w-full"
            >
              Trang chủ
            </Link>
            <Link
              to="/jobs"
              className="text-neutral-700 hover:text-primary font-semibold text-base transition-colors duration-200 relative after:absolute after:bottom-0 after:left-0 after:w-0 after:h-0.5 after:bg-primary after:transition-all hover:after:w-full"
            >
              Tin tuyển dụng
            </Link>
            <Link
              to="/companies"
              className="text-neutral-700 hover:text-primary font-semibold text-base transition-colors duration-200 relative after:absolute after:bottom-0 after:left-0 after:w-0 after:h-0.5 after:bg-primary after:transition-all hover:after:w-full"
            >
              Công ty
            </Link>
            <Link
              to="/contact"
              className="text-neutral-700 hover:text-primary font-semibold text-base transition-colors duration-200 relative after:absolute after:bottom-0 after:left-0 after:w-0 after:h-0.5 after:bg-primary after:transition-all hover:after:w-full">
                Tin ứng tuyển
            </Link>
            <Link
              to="/contact"
              className="text-neutral-700 hover:text-primary font-semibold text-base transition-colors duration-200 relative after:absolute after:bottom-0 after:left-0 after:w-0 after:h-0.5 after:bg-primary after:transition-all hover:after:w-full"
            >
              Liên hệ
            </Link>
          </div>

          {/* Right: Search + Auth Buttons */}
          <div className="hidden lg:flex items-center space-x-4 flex-shrink-0">
            {/* Search Input */}
            {/* <form onSubmit={handleSearch} className="relative">
              <input
                type="text"
                placeholder="Tìm kiếm việc làm..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-56 pl-10 pr-4 py-2.5 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
              />
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-neutral-400 w-5 h-5" />
            </form> */}

            {/* Conditional Rendering based on Auth State */}
            {!isAuthenticated ? (
              <>
                {/* Guest: Register + Login Buttons */}
                <Link
                  to="/register"
                  className="px-6 py-2.5 border-2 border-primary text-primary rounded-lg font-semibold hover:bg-primary-50 transition-all duration-200"
                >
                  Đăng ký
                </Link>
                <Link
                  to="/login"
                  state={{ from: location }}
                  className="px-6 py-2.5 bg-primary text-white rounded-lg font-semibold hover:bg-primary-600 shadow-md hover:shadow-lg transition-all duration-200"
                >
                  Đăng nhập
                </Link>
              </>
            ) : (
              <>
                {/* Authenticated: User Avatar with Dropdown */}
                <div className="relative" ref={userMenuRef}>
                  <button
                    onClick={toggleUserMenu}
                    className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-neutral-100 transition-all duration-200"
                  >
                    {/* User Avatar */}
                    <div className="w-10 h-10 bg-primary text-white rounded-full flex items-center justify-center font-semibold">
                      {getUserAvatar()}
                    </div>
                    <span className="text-neutral-700 font-medium max-w-32 truncate">
                      {getUserDisplayName()}
                    </span>
                    <ChevronDown className={`w-4 h-4 text-neutral-500 transition-transform ${isUserMenuOpen ? 'rotate-180' : ''}`} />
                  </button>

                  {/* Dropdown Menu */}
                  {isUserMenuOpen && (
                    <div className="absolute right-0 mt-2 w-64 bg-white rounded-lg shadow-lg border border-neutral-200 py-2 z-50">
                      {/* User Info Header */}
                      <div className="px-4 py-3 border-b border-neutral-200">
                        <p className="text-sm font-semibold text-neutral-900 truncate">
                          {getUserDisplayName()}
                        </p>
                        <p className="text-xs text-neutral-500 truncate">
                          {user?.email || user?.username}
                        </p>
                      </div>

                      {/* Menu Items - Role-based */}
                      {user?.role === 'UV' && (
                        <>
                          {/* Candidate Menu */}
                          <Link
                            to="/candidate/profile"
                            className="flex items-center gap-3 px-4 py-3 hover:bg-neutral-50 transition-colors"
                            onClick={() => setIsUserMenuOpen(false)}
                          >
                            <Settings className="w-5 h-5 text-neutral-600" />
                            <span className="text-neutral-700">Quản lý tài khoản</span>
                          </Link>
                          <Link
                            to="/candidate/saved-jobs"
                            className="flex items-center gap-3 px-4 py-3 hover:bg-neutral-50 transition-colors"
                            onClick={() => setIsUserMenuOpen(false)}
                          >
                            <Bookmark className="w-5 h-5 text-neutral-600" />
                            <span className="text-neutral-700">Công việc đã lưu</span>
                          </Link>
                        </>
                      )}

                      {user?.role === 'DN' && (
                        <>
                          {/* Employer Menu */}
                          <Link
                            to="/employer/dashboard"
                            className="flex items-center gap-3 px-4 py-3 hover:bg-neutral-50 transition-colors"
                            onClick={() => setIsUserMenuOpen(false)}
                          >
                            <Settings className="w-5 h-5 text-neutral-600" />
                            <span className="text-neutral-700">Bảng điều khiển</span>
                          </Link>
                          <Link
                            to="/employer/jobs"
                            className="flex items-center gap-3 px-4 py-3 hover:bg-neutral-50 transition-colors"
                            onClick={() => setIsUserMenuOpen(false)}
                          >
                            <Bookmark className="w-5 h-5 text-neutral-600" />
                            <span className="text-neutral-700">Quản lý tin tuyển dụng</span>
                          </Link>
                        </>
                      )}

                      {user?.role === 'ADM' && (
                        <>
                          {/* Admin Menu */}
                          <Link
                            to="/admin/dashboard"
                            className="flex items-center gap-3 px-4 py-3 hover:bg-neutral-50 transition-colors"
                            onClick={() => setIsUserMenuOpen(false)}
                          >
                            <Settings className="w-5 h-5 text-neutral-600" />
                            <span className="text-neutral-700">Quản trị hệ thống</span>
                          </Link>
                        </>
                      )}

                      {/* Divider */}
                      <div className="border-t border-neutral-200 my-2"></div>

                      {/* Logout */}
                      <button
                        onClick={handleLogout}
                        className="flex items-center gap-3 px-4 py-3 hover:bg-red-50 transition-colors w-full text-left"
                      >
                        <LogOut className="w-5 h-5 text-red-600" />
                        <span className="text-red-600 font-medium">Đăng xuất</span>
                      </button>
                    </div>
                  )}
                </div>
              </>
            )}
          </div>

          {/* Mobile Menu Button */}
          <button
            onClick={toggleMobileMenu}
            className="lg:hidden p-2 rounded-lg hover:bg-neutral-100"
            aria-label="Toggle menu"
          >
            {isMobileMenuOpen ? (
              <X className="w-6 h-6 text-neutral-700" />
            ) : (
              <Menu className="w-6 h-6 text-neutral-700" />
            )}
          </button>
        </div>

        {/* Mobile Menu */}
        {isMobileMenuOpen && (
          <div className="lg:hidden py-4 border-t border-neutral-200">
            {/* Mobile Search */}
            <form onSubmit={handleSearch} className="relative mb-4">
              <input
                type="text"
                placeholder="Tìm kiếm việc làm..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
              />
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-neutral-400 w-5 h-5" />
            </form>

            {/* Mobile Navigation Links */}
            <div className="flex flex-col space-y-3 mb-4">
              <Link
                to="/"
                className="text-neutral-700 hover:text-primary font-medium transition-colors py-2"
                onClick={toggleMobileMenu}
              >
                Trang Chủ
              </Link>
              <Link
                to="/jobs"
                className="text-neutral-700 hover:text-primary font-medium transition-colors py-2"
                onClick={toggleMobileMenu}
              >
                Việc Làm
              </Link>
              <Link
                to="/companies"
                className="text-neutral-700 hover:text-primary font-medium transition-colors py-2"
                onClick={toggleMobileMenu}
              >
                Công Ty
              </Link>
              <Link
                to="/contact"
                className="text-neutral-700 hover:text-primary font-medium transition-colors py-2"
                onClick={toggleMobileMenu}
              >
                Liên Hệ
              </Link>
            </div>

            {/* Mobile Auth Section */}
            {!isAuthenticated ? (
              <>
                {/* Guest: Register + Login */}
                <div className="flex flex-col space-y-2">
                  <Link
                    to="/register"
                    className="w-full px-6 py-2 border-2 border-primary text-primary rounded-lg font-medium hover:bg-primary hover:text-white transition-all duration-200 text-center"
                    onClick={toggleMobileMenu}
                  >
                    Đăng Ký
                  </Link>
                  <Link
                    to="/login"
                    state={{ from: location }}
                    className="w-full px-6 py-2 bg-primary text-white rounded-lg font-medium hover:bg-primary-600 transition-all duration-200 text-center"
                    onClick={toggleMobileMenu}
                  >
                    Đăng Nhập
                  </Link>
                </div>
              </>
            ) : (
              <>
                {/* Authenticated: User Menu */}
                <div className="border-t border-neutral-200 pt-4 mt-4">
                  {/* User Info */}
                  <div className="flex items-center gap-3 px-4 py-3 bg-neutral-100 rounded-lg mb-4">
                    <div className="w-12 h-12 bg-primary text-white rounded-full flex items-center justify-center font-semibold text-lg">
                      {getUserAvatar()}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-semibold text-neutral-900 truncate">
                        {getUserDisplayName()}
                      </p>
                      <p className="text-xs text-neutral-500 truncate">
                        {user?.email || user?.username}
                      </p>
                    </div>
                  </div>

                  {/* Menu Links - Role-based */}
                  <div className="flex flex-col space-y-2">
                    {user?.role === 'UV' && (
                      <>
                        {/* Candidate Menu */}
                        <Link
                          to="/candidate/profile"
                          className="flex items-center gap-3 px-4 py-3 text-neutral-700 hover:bg-neutral-100 rounded-lg transition-colors"
                          onClick={toggleMobileMenu}
                        >
                          <Settings className="w-5 h-5" />
                          <span>Quản lý tài khoản</span>
                        </Link>
                        <Link
                          to="/candidate/saved-jobs"
                          className="flex items-center gap-3 px-4 py-3 text-neutral-700 hover:bg-neutral-100 rounded-lg transition-colors"
                          onClick={toggleMobileMenu}
                        >
                          <Bookmark className="w-5 h-5" />
                          <span>Công việc đã lưu</span>
                        </Link>
                      </>
                    )}

                    {user?.role === 'DN' && (
                      <>
                        {/* Employer Menu */}
                        <Link
                          to="/employer/dashboard"
                          className="flex items-center gap-3 px-4 py-3 text-neutral-700 hover:bg-neutral-100 rounded-lg transition-colors"
                          onClick={toggleMobileMenu}
                        >
                          <Settings className="w-5 h-5" />
                          <span>Bảng điều khiển</span>
                        </Link>
                        <Link
                          to="/employer/jobs"
                          className="flex items-center gap-3 px-4 py-3 text-neutral-700 hover:bg-neutral-100 rounded-lg transition-colors"
                          onClick={toggleMobileMenu}
                        >
                          <Bookmark className="w-5 h-5" />
                          <span>Quản lý tin tuyển dụng</span>
                        </Link>
                      </>
                    )}

                    {user?.role === 'ADM' && (
                      <>
                        {/* Admin Menu */}
                        <Link
                          to="/admin/dashboard"
                          className="flex items-center gap-3 px-4 py-3 text-neutral-700 hover:bg-neutral-100 rounded-lg transition-colors"
                          onClick={toggleMobileMenu}
                        >
                          <Settings className="w-5 h-5" />
                          <span>Quản trị hệ thống</span>
                        </Link>
                      </>
                    )}

                    {/* Logout */}
                    <button
                      onClick={handleLogout}
                      className="flex items-center gap-3 px-4 py-3 text-red-600 hover:bg-red-50 rounded-lg transition-colors w-full text-left"
                    >
                      <LogOut className="w-5 h-5" />
                      <span className="font-medium">Đăng xuất</span>
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
