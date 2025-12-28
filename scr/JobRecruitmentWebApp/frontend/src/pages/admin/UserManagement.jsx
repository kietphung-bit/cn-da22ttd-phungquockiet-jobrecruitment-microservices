import React, { useState, useEffect } from 'react';
import { Search, Lock, Unlock, Loader2, User, Building2 } from 'lucide-react';
import { toast } from 'react-toastify';
import adminService from '../../services/admin.service';
import { useAuth } from '../../contexts/AuthContext';

/**
 * UserManagement Component
 * Admin interface for managing user accounts (lock/unlock)
 * 
 * Features:
 * - List all users with pagination
 * - Filter by role (All, Candidate, Employer)
 * - Search by name or email
 * - Lock/Unlock user accounts
 * - Visual feedback for locked users (grayed out)
 * - Prevent admin from locking themselves
 * 
 * API Integration:
 * - GET /api/v1/admin/users - List users
 * - PATCH /api/v1/admin/users/{id}/lock - Lock user
 * - PATCH /api/v1/admin/users/{id}/unlock - Unlock user
 * 
 * Access Control:
 * - Only accessible by ROLE_ADM
 * - Protected by PrivateRoute in AppRoutes
 */
const UserManagement = () => {
  const { user: currentUser } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState({});

  // Filter and pagination state
  const [filters, setFilters] = useState({
    roleCode: '', // '' = All, 'ADM', 'DN', 'UV'
    search: '',
    page: 0,
    size: 20,
  });

  const [pagination, setPagination] = useState({
    totalPages: 0,
    totalElements: 0,
    currentPage: 0,
  });

  // Fetch users on mount and when filters change
  useEffect(() => {
    fetchUsers();
  }, [filters.roleCode, filters.page]);

  /**
   * Fetch users from API with filters
   */
  const fetchUsers = async () => {
    try {
      setLoading(true);
      const params = {
        page: filters.page,
        size: filters.size,
        sort: 'userId,asc',
      };

      // Add role filter if selected
      if (filters.roleCode) {
        params.roleCode = filters.roleCode;
      }

      const response = await adminService.getAllUsers(params);
      const data = response.data?.data || response.data;

      // Handle paginated response
      if (data.content) {
        setUsers(data.content);
        setPagination({
          totalPages: data.totalPages,
          totalElements: data.totalElements,
          currentPage: data.number,
        });
      } else {
        setUsers(data || []);
      }
    } catch (error) {
      console.error('Error fetching users:', error);
      toast.error('Không thể tải danh sách người dùng');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Handle role filter change
   */
  const handleRoleFilterChange = (roleCode) => {
    setFilters((prev) => ({ ...prev, roleCode, page: 0 }));
  };

  /**
   * Handle search
   */
  const handleSearch = () => {
    // TODO: Implement search API if backend supports it
    toast.info('Tính năng tìm kiếm đang phát triển');
  };

  /**
   * Handle lock user
   */
  const handleLockUser = async (userId, username) => {
    // Prevent admin from locking themselves
    if (currentUser?.userId === userId) {
      toast.error('Bạn không thể khóa tài khoản của chính mình!');
      return;
    }

    const confirmed = window.confirm(
      `Bạn có chắc chắn muốn khóa tài khoản "${username}"?\n\nNgười dùng này sẽ không thể đăng nhập vào hệ thống.`
    );

    if (!confirmed) {
      return;
    }

    try {
      setActionLoading((prev) => ({ ...prev, [userId]: true }));
      await adminService.lockUser(userId);
      toast.success(`Đã khóa tài khoản "${username}"`);
      fetchUsers(); // Refresh list
    } catch (error) {
      console.error('Error locking user:', error);
      const errorMessage = error.response?.data?.message || 'Không thể khóa tài khoản';
      toast.error(errorMessage);
    } finally {
      setActionLoading((prev) => ({ ...prev, [userId]: false }));
    }
  };

  /**
   * Handle unlock user
   */
  const handleUnlockUser = async (userId, username) => {
    const confirmed = window.confirm(
      `Bạn có chắc chắn muốn mở khóa tài khoản "${username}"?\n\nNgười dùng này sẽ có thể đăng nhập vào hệ thống trở lại.`
    );

    if (!confirmed) {
      return;
    }

    try {
      setActionLoading((prev) => ({ ...prev, [userId]: true }));
      await adminService.unlockUser(userId);
      toast.success(`Đã mở khóa tài khoản "${username}"`);
      fetchUsers(); // Refresh list
    } catch (error) {
      console.error('Error unlocking user:', error);
      const errorMessage = error.response?.data?.message || 'Không thể mở khóa tài khoản';
      toast.error(errorMessage);
    } finally {
      setActionLoading((prev) => ({ ...prev, [userId]: false }));
    }
  };

  /**
   * Get role badge color
   */
  const getRoleBadge = (roleCode) => {
    const badges = {
      ADM: { label: 'Admin', color: 'bg-red-100 text-red-800' },
      DN: { label: 'Nhà tuyển dụng', color: 'bg-blue-100 text-blue-800' },
      UV: { label: 'Ứng viên', color: 'bg-green-100 text-green-800' },
    };
    return badges[roleCode] || { label: roleCode, color: 'bg-gray-100 text-gray-800' };
  };

  /**
   * Get user avatar/logo
   */
  const getUserAvatar = (user) => {
    if (user.roleCode === 'DN') {
      return (
        <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
          <Building2 className="w-5 h-5 text-blue-600" />
        </div>
      );
    }
    return (
      <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
        <User className="w-5 h-5 text-green-600" />
      </div>
    );
  };

  /**
   * Handle pagination
   */
  const handlePageChange = (newPage) => {
    setFilters((prev) => ({ ...prev, page: newPage }));
  };

  if (loading && users.length === 0) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-600"></div>
      </div>
    );
  }

  return (
    <div>
      {/* Page Header */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-neutral-900">Quản lý người dùng</h1>
        <p className="text-neutral-600 mt-2">
          Quản lý tài khoản người dùng, khóa/mở khóa tài khoản
        </p>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-4 mb-6">
        <div className="flex flex-col md:flex-row gap-4">
          {/* Role Filter */}
          <div className="flex gap-2">
            <button
              onClick={() => handleRoleFilterChange('')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                filters.roleCode === ''
                  ? 'bg-red-600 text-white'
                  : 'bg-neutral-100 text-neutral-700 hover:bg-neutral-200'
              }`}
            >
              Tất cả
            </button>
            <button
              onClick={() => handleRoleFilterChange('UV')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                filters.roleCode === 'UV'
                  ? 'bg-red-600 text-white'
                  : 'bg-neutral-100 text-neutral-700 hover:bg-neutral-200'
              }`}
            >
              Ứng viên
            </button>
            <button
              onClick={() => handleRoleFilterChange('DN')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                filters.roleCode === 'DN'
                  ? 'bg-red-600 text-white'
                  : 'bg-neutral-100 text-neutral-700 hover:bg-neutral-200'
              }`}
            >
              Nhà tuyển dụng
            </button>
          </div>

          {/* Search */}
          <div className="flex-1 flex gap-2">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-neutral-400 w-5 h-5" />
              <input
                type="text"
                placeholder="Tìm theo tên hoặc email..."
                value={filters.search}
                onChange={(e) => setFilters((prev) => ({ ...prev, search: e.target.value }))}
                className="w-full pl-10 pr-4 py-2 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
              />
            </div>
            <button
              onClick={handleSearch}
              className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
            >
              Tìm kiếm
            </button>
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-white rounded-lg shadow-sm border border-neutral-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-neutral-50 border-b border-neutral-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                Người dùng
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                Email
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                Vai trò
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                Trạng thái
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-neutral-500 uppercase tracking-wider">
                Thao tác
              </th>
              {/* <th className="px-6 py-3 text-right text-xs font-medium text-neutral-500 uppercase tracking-wider">
                
              </th> */}
            </tr>
          </thead>
          <tbody className="divide-y divide-neutral-200">
            {users.length === 0 ? (
              <tr>
                <td colSpan="5" className="px-6 py-8 text-center text-neutral-500">
                  Không tìm thấy người dùng nào
                </td>
              </tr>
            ) : (
              users.map((user) => {
                const roleBadge = getRoleBadge(user.roleCode);
                const isLocked = user.locked || false;
                const isCurrentUser = currentUser?.userId === user.userId;
                const isLoading = actionLoading[user.userId];

                return (
                  <tr
                    key={user.userId}
                    className={`hover:bg-neutral-50 ${isLocked ? 'bg-neutral-100 opacity-60' : ''}`}
                  >
                    {/* User Avatar & Name */}
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center gap-3">
                        {getUserAvatar(user)}
                        <div>
                          <div className="text-sm font-medium text-neutral-900">
                            {user.username}
                          </div>
                          <div className="text-xs text-neutral-500">ID: {user.userId}</div>
                        </div>
                      </div>
                    </td>

                    {/* Email */}
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-neutral-700">
                      {user.username}
                    </td>

                    {/* Role Badge */}
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span
                        className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${roleBadge.color}`}
                      >
                        {roleBadge.label}
                      </span>
                    </td>

                    {/* Status */}
                    <td className="px-6 py-4 whitespace-nowrap">
                      {isLocked ? (
                        <span className="inline-flex px-3 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800">
                          Đã khóa
                        </span>
                      ) : (
                        <span className="inline-flex px-3 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                          Hoạt động
                        </span>
                      )}
                    </td>

                    {/* <td>
                      <button
                        onClick={() => alert(`User ID: ${user.userId}`)}
                        className="text-blue-600 hover:underline text-sm"
                      >
                        Xem chi tiết
                      </button>
                    </td> */}

                    {/* Actions */}
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      {isCurrentUser ? (
                        <span className="text-neutral-400 text-xs">Tài khoản hiện tại</span>
                      ) : isLocked ? (
                        <button
                          onClick={() => handleUnlockUser(user.userId, user.username)}
                          disabled={isLoading}
                          className="inline-flex items-center gap-2 px-3 py-1 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50"
                        >
                          {isLoading ? (
                            <Loader2 className="w-4 h-4 animate-spin" />
                          ) : (
                            <Unlock className="w-4 h-4" />
                          )}
                          Mở khóa
                        </button>
                      ) : (
                        <button
                          onClick={() => handleLockUser(user.userId, user.username)}
                          disabled={isLoading}
                          className="inline-flex items-center gap-2 px-3 py-1 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50"
                        >
                          {isLoading ? (
                            <Loader2 className="w-4 h-4 animate-spin" />
                          ) : (
                            <Lock className="w-4 h-4" />
                          )}
                          Khóa tài khoản
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>

        {/* Pagination */}
        {pagination.totalPages > 1 && (
          <div className="px-6 py-4 border-t border-neutral-200 flex items-center justify-between">
            <div className="text-sm text-neutral-700">
              Hiển thị{' '}
              <span className="font-medium">
                {pagination.currentPage * filters.size + 1}
              </span>{' '}
              đến{' '}
              <span className="font-medium">
                {Math.min(
                  (pagination.currentPage + 1) * filters.size,
                  pagination.totalElements
                )}
              </span>{' '}
              trong tổng số <span className="font-medium">{pagination.totalElements}</span> người
              dùng
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => handlePageChange(pagination.currentPage - 1)}
                disabled={pagination.currentPage === 0}
                className="px-3 py-1 border border-neutral-300 rounded-lg hover:bg-neutral-100 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Trước
              </button>
              <span className="px-3 py-1 text-sm text-neutral-700">
                Trang {pagination.currentPage + 1} / {pagination.totalPages}
              </span>
              <button
                onClick={() => handlePageChange(pagination.currentPage + 1)}
                disabled={pagination.currentPage >= pagination.totalPages - 1}
                className="px-3 py-1 border border-neutral-300 rounded-lg hover:bg-neutral-100 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Sau
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default UserManagement;
