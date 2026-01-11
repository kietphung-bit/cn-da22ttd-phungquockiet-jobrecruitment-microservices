import React, { useState, useEffect } from 'react';
import { Search, Lock, Unlock, Loader2, User, Building2, Eye, Copy, FileSpreadsheet, FileText as FilePDF } from 'lucide-react';
import { toast } from 'react-toastify';
import { format } from 'date-fns';
import UserDetailModal from '../../components/features/admin/UserDetailModal';
import adminService from '../../services/admin.service';
import { useAuth } from '../../contexts/AuthContext';
import { copyToClipboard, exportToExcel, exportToPDF } from '../../utils/tableExport';

/**
 * UserManagement Component
 * Giao diện quản lý tài khoản người dùng (khóa/mở khóa)
 * 
 * Tính năng:
 * - Liệt kê tất cả người dùng với phân trang
 * - Lọc theo vai trò (Tất cả, Ứng viên, Nhà tuyển dụng)
 * - Tìm kiếm theo tên hoặc email
 * - Khóa/Mở khóa tài khoản người dùng
 * - Phản hồi trực quan cho người dùng bị khóa (mờ đi)
 * - Ngăn admin khóa chính họ
 * 
 * Lưu ý: Phê duyệt công ty hiện được xử lý trong trang CompanyApproval riêng biệt
 * 
 * Tích hợp API:
 * - GET /api/v1/admin/users - Liệt kê người dùng
 * - PATCH /api/v1/admin/users/{id}/lock - Khóa người dùng
 * - PATCH /api/v1/admin/users/{id}/unlock - Mở khóa người dùng
 * 
 * Access Control:
 * - Chỉ truy cập được bởi ROLE_ADM
 * - Được bảo vệ bởi PrivateRoute trong AppRoutes
 */
const UserManagement = () => {
  const { user: currentUser } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState({});

  // Trạng thái lọc và phân trang
  const [filters, setFilters] = useState({
    roleCode: '', // '' = Tất cả, 'ADM', 'DN', 'UV'
    search: '',
    page: 0,
    size: 20,
  });

  // Trạng thái tìm kiếm và lọc người dùng
  const [searchTerm, setSearchTerm] = useState('');
  const [filteredUsers, setFilteredUsers] = useState([]);

  const [pagination, setPagination] = useState({
    totalPages: 0,
    totalElements: 0,
    currentPage: 0,
  });

  // Trạng thái modal chi tiết người dùng
  const [selectedUser, setSelectedUser] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Lấy người dùng khi component mount và khi bộ lọc thay đổi
  useEffect(() => {
    fetchUsers();
  }, [filters.roleCode, filters.page]);

  // Áp dụng bộ lọc tìm kiếm
  useEffect(() => {
    if (!searchTerm.trim()) {
      setFilteredUsers(users);
      return;
    }

    const searchLower = searchTerm.toLowerCase();
    const filtered = users.filter(user => {
      const username = (user.username || '').toLowerCase();
      const email = (user.email || '').toLowerCase();
      const companyName = (user.companyName || '').toLowerCase();
      const candidateName = (user.candidateName || '').toLowerCase();
      
      return username.includes(searchLower) || 
             email.includes(searchLower) ||
             companyName.includes(searchLower) ||
             candidateName.includes(searchLower);
    });
    
    setFilteredUsers(filtered);
  }, [users, searchTerm]);

  /**
   * Lấy người dùng từ API với bộ lọc
   */
  const fetchUsers = async () => {
    try {
      setLoading(true);
      const params = {
        page: filters.page,
        size: filters.size,
        sort: 'userId,asc',
      };

      // Thêm bộ lọc vai trò nếu được chọn
      if (filters.roleCode) {
        params.roleCode = filters.roleCode;
      }

      const response = await adminService.getAllUsers(params);
      const data = response.data?.data || response.data;

      // Xử lý phản hồi phân trang
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
   * Xử lý thay đổi bộ lọc vai trò
   */
  const handleRoleFilterChange = (roleCode) => {
    setFilters((prev) => ({ ...prev, roleCode, page: 0 }));
  };

  /**
   * Xử lý xuất dữ liệu ra các định dạng khác nhau
   */
  const handleExport = async (type) => {
    const columns = [
      { header: 'Tên người dùng', accessor: (row) => row.username || 'N/A' },
      { header: 'Email', accessor: (row) => row.email || 'N/A' },
      { header: 'Vai trò', accessor: (row) => {
        const roles = { ADM: 'Admin', DN: 'Nhà tuyển dụng', UV: 'Ứng viên' };
        return roles[row.roleCode] || row.roleCode;
      }},
      { header: 'Tên hiển thị', accessor: (row) => row.companyName || row.candidateName || 'N/A' },
      { header: 'Trạng thái', accessor: (row) => row.locked ? 'Đã khóa' : 'Hoạt động' },
      { header: 'Ngày tạo', accessor: (row) => row.createdAt ? format(new Date(row.createdAt), 'dd/MM/yyyy') : 'N/A' }
    ];

    const dataToExport = filteredUsers.length > 0 ? filteredUsers : users;

    try {
      switch (type) {
        case 'copy':
          await copyToClipboard(dataToExport, columns);
          toast.success('Đã sao chép vào clipboard!');
          break;
        case 'excel':
          exportToExcel(dataToExport, columns, 'danh-sach-nguoi-dung.csv');
          toast.success('Đã xuất file Excel!');
          break;
        case 'pdf':
          await exportToPDF(dataToExport, columns, 'danh-sach-nguoi-dung.pdf', {
            title: 'Danh Sách Người Dùng'
          });
          toast.success('Đã xuất file PDF!');
          break;
        default:
          break;
      }
    } catch (error) {
      console.error('Export failed:', error);
      toast.error('Xuất dữ liệu thất bại. Vui lòng thử lại!');
    }
  };

  /**
   * Xử lý thay đổi trạng thái khóa (khóa/mở khóa) người dùng
   */
  const handleToggleLock = async (userId, username, isCurrentlyLocked) => {
    // Ngăn admin khóa chính mình
    if (currentUser?.userId === userId) {
      toast.error('Bạn không thể khóa tài khoản của chính mình!');
      return;
    }

    const action = isCurrentlyLocked ? 'mở khóa' : 'khóa';
    const confirmed = window.confirm(
      `Bạn có chắc chắn muốn ${action} tài khoản "${username}"?`
    );

    if (!confirmed) {
      return;
    }

    try {
      setActionLoading((prev) => ({ ...prev, [userId]: true }));
      // Backend xử lý logic toggle - chỉ cần gọi lockUser
      await adminService.lockUser(userId);
      toast.success(`Đã ${action} tài khoản "${username}" thành công`);
      fetchUsers(); // Làm mới danh sách
    } catch (error) {
      console.error(`Error ${action} user:`, error);
      const errorMessage = error.response?.data?.message || `Không thể ${action} tài khoản`;
      toast.error(errorMessage);
    } finally {
      setActionLoading((prev) => ({ ...prev, [userId]: false }));
    }
  };

  /**
   * Xử lý xem chi tiết người dùng
   */
  const handleViewDetail = (user) => {
    setSelectedUser(user);
    setIsModalOpen(true);
  };

  /**
   * Xử lý đóng modal
   */
  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedUser(null);
  };

  /**
   * Lấy màu badge vai trò
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
   * Lấy avatar/logo người dùng
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
   * Xử lý phân trang
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

      {/* Search and Export Bar */}
      <div className="bg-white rounded-lg shadow-sm p-4 mb-4 border border-neutral-200">
        <div className="flex flex-col lg:flex-row gap-4">
          {/* Search Input */}
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-neutral-400 w-5 h-5" />
              <input
                type="text"
                placeholder="Tìm kiếm theo tên, email, công ty, ứng viên..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
              />
            </div>
          </div>

          {/* Export Buttons */}
          <div className="flex gap-2">
            <button
              onClick={() => handleExport('copy')}
              className="inline-flex items-center gap-2 px-4 py-2.5 text-sm font-medium text-neutral-700 bg-white border border-neutral-300 rounded-lg hover:bg-neutral-50 transition-colors"
              title="Sao chép vào clipboard"
            >
              <Copy className="w-4 h-4" />
              <span className="hidden sm:inline">Copy</span>
            </button>
            
            <button
              onClick={() => handleExport('excel')}
              className="inline-flex items-center gap-2 px-4 py-2.5 text-sm font-medium text-white bg-green-600 rounded-lg hover:bg-green-700 transition-colors"
              title="Xuất ra Excel"
            >
              <FileSpreadsheet className="w-4 h-4" />
              <span className="hidden sm:inline">Excel</span>
            </button>
            
            <button
              onClick={() => handleExport('pdf')}
              className="inline-flex items-center gap-2 px-4 py-2.5 text-sm font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 transition-colors"
              title="Xuất ra PDF"
            >
              <FilePDF className="w-4 h-4" />
              <span className="hidden sm:inline">PDF</span>
            </button>
          </div>
        </div>

        {/* Search Results Info */}
        {searchTerm && (
          <div className="mt-3 pt-3 border-t text-sm text-neutral-600">
            Tìm thấy {filteredUsers.length} kết quả cho "{searchTerm}"
          </div>
        )}
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
            {filteredUsers.length === 0 ? (
              <tr>
                <td colSpan="5" className="px-6 py-12 text-center">
                  <Search className="w-12 h-12 mx-auto text-neutral-300 mb-3" />
                  <p className="text-neutral-600 font-medium">Không tìm thấy người dùng nào</p>
                  <p className="text-sm text-neutral-500 mt-1">
                    {searchTerm ? 'Thử thay đổi từ khóa tìm kiếm' : 'Chưa có người dùng trong hệ thống'}
                  </p>
                </td>
              </tr>
            ) : (
              filteredUsers.map((user) => {
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
                      <div className="flex items-center justify-end gap-2">
                        {/* View Detail Button */}
                        <button
                          onClick={() => handleViewDetail(user)}
                          className="inline-flex items-center gap-1 px-3 py-1 bg-blue-100 text-blue-700 rounded-lg hover:bg-blue-200 transition-colors"
                          title="Xem chi tiết"
                        >
                          <Eye className="w-4 h-4" />
                          Chi tiết
                        </button>

                        {/* Conditional Actions based on user type and status */}
                        {isCurrentUser ? (
                          <span className="text-neutral-400 text-xs">Tài khoản hiện tại</span>
                        ) : user.roleCode === 'DN' && user.companyStatus === 'PENDING' ? (
                          // PENDING Employers: Show Approve/Reject buttons ONLY
                          <>
                            <button
                              onClick={() => handleChangeCompanyStatus(user.userId, user.companyId, user.companyName, 'ACTIVE')}
                              disabled={actionLoading[`company_${user.userId}`]}
                              className="inline-flex items-center gap-1 px-3 py-1 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50"
                              title="Duyệt doanh nghiệp"
                            >
                              {actionLoading[`company_${user.userId}`] ? (
                                <Loader2 className="w-4 h-4 animate-spin" />
                              ) : (
                                <CheckCircle className="w-4 h-4" />
                              )}
                              Duyệt
                            </button>
                            <button
                              onClick={() => handleChangeCompanyStatus(user.userId, user.companyId, user.companyName, 'BLOCKED')}
                              disabled={actionLoading[`company_${user.userId}`]}
                              className="inline-flex items-center gap-1 px-3 py-1 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50"
                              title="Từ chối doanh nghiệp"
                            >
                              {actionLoading[`company_${user.userId}`] ? (
                                <Loader2 className="w-4 h-4 animate-spin" />
                              ) : (
                                <XCircle className="w-4 h-4" />
                              )}
                              Từ chối
                            </button>
                          </>
                        ) : (
                          // All other users (Candidates, ACTIVE/BLOCKED Employers): Show Lock/Unlock button
                          <button
                            onClick={() => handleToggleLock(user.userId, user.username, isLocked)}
                            disabled={isLoading}
                            className={`inline-flex items-center gap-2 px-3 py-1 rounded-lg transition-colors disabled:opacity-50 ${
                              isLocked
                                ? 'bg-green-600 text-white hover:bg-green-700'
                                : 'bg-red-600 text-white hover:bg-red-700'
                            }`}
                          >
                            {isLoading ? (
                              <Loader2 className="w-4 h-4 animate-spin" />
                            ) : isLocked ? (
                              <Unlock className="w-4 h-4" />
                            ) : (
                              <Lock className="w-4 h-4" />
                            )}
                            {isLocked ? 'Mở khóa' : 'Khóa'}
                          </button>
                        )}
                      </div>
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

      {/* User Detail Modal */}
      <UserDetailModal
        user={selectedUser}
        isOpen={isModalOpen}
        onClose={handleCloseModal}
      />
    </div>
  );
};

export default UserManagement;
