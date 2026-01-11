import React, { useState, useEffect } from 'react';
import { 
  Building2, 
  CheckCircle, 
  XCircle, 
  Loader2, 
  Mail, 
  Globe, 
  MapPin,
  Eye,
  Calendar,
  FileText
} from 'lucide-react';
import { toast } from 'react-toastify';
import adminService from '../../services/admin.service';

/**
 * CompanyApproval Component
 * Trang dành cho Admin để xem xét và duyệt/từ chối đăng ký doanh nghiệp
 * 
 * Tính năng:
 * - Liệt kê các đăng ký doanh nghiệp đang chờ duyệt với thông tin chi tiết
 * - Hiển thị chi tiết doanh nghiệp (tên, mô tả, website, địa chỉ, v.v.)
 * - Duyệt doanh nghiệp (PENDING → ACTIVE)
 * - Từ chối doanh nghiệp (PENDING → BLOCKED)
 * - Lọc theo trạng thái (Đang chờ, Hoạt động, Bị chặn, Tất cả)
 * - Hỗ trợ phân trang nếu cần thiết
 * 
 * API Integration:
 * - GET /api/v1/admin/users?roleCode=DN - Lấy tất cả doanh nghiệp
 * - PATCH /api/v1/admin/companies/{id}/status - Thay đổi trạng thái doanh nghiệp
 * 
 * Khả năng bảo mật:
 * - Chỉ truy cập được bởi ROLE_ADM
 * - Được bảo vệ bởi PrivateRoute trong AppRoutes
 */
const CompanyApproval = () => {
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState({});
  const [selectedCompany, setSelectedCompany] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);

  // Trạng thái lọc
  const [statusFilter, setStatusFilter] = useState('PENDING'); // Mặc định là đang chờ
  const [pagination, setPagination] = useState({
    totalPages: 0,
    totalElements: 0,
    currentPage: 0,
  });

  // Lấy danh sách doanh nghiệp khi component được mount và khi trạng thái lọc thay đổi
  useEffect(() => {
    fetchCompanies();
  }, [statusFilter]);

  /**
   * Lấy danh sách doanh nghiệp từ API
   */
  const fetchCompanies = async () => {
    try {
      setLoading(true);
      const params = {
        page: 0,
        size: 100,
        roleCode: 'DN', // Chỉ nhà tuyển dụng
        sort: 'userId,desc',
      };

      const response = await adminService.getAllUsers(params);
      const data = response.data?.data || response.data;

      let allCompanies = [];
      if (data.content) {
        allCompanies = data.content;
        setPagination({
          totalPages: data.totalPages,
          totalElements: data.totalElements,
          currentPage: data.number,
        });
      } else {
        allCompanies = data || [];
      }

      // Lọc theo trạng thái
      let filteredCompanies = allCompanies;
      if (statusFilter && statusFilter !== 'ALL') {
        filteredCompanies = allCompanies.filter(
          (company) => company.companyStatus === statusFilter
        );
      }

      setCompanies(filteredCompanies);
    } catch (error) {
      console.error('Error fetching companies:', error);
      toast.error('Không thể tải danh sách doanh nghiệp');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Xử lý duyệt doanh nghiệp
   */
  const handleApprove = async (company) => {
    const confirmed = window.confirm(
      `Bạn có chắc chắn muốn duyệt doanh nghiệp "${company.companyName}"?`
    );

    if (!confirmed) return;

    try {
      setActionLoading((prev) => ({ ...prev, [company.userId]: true }));
      await adminService.changeCompanyStatus(company.companyId, 'ACTIVE');
      toast.success(`Đã duyệt doanh nghiệp "${company.companyName}" thành công`);
      fetchCompanies(); // Làm mới danh sách
    } catch (error) {
      console.error('Error approving company:', error);
      toast.error(error.response?.data?.message || 'Không thể duyệt doanh nghiệp');
    } finally {
      setActionLoading((prev) => ({ ...prev, [company.userId]: false }));
    }
  };

  /**
   * Xử lý từ chối doanh nghiệp
   */
  const handleReject = async (company) => {
    const confirmed = window.confirm(
      `Bạn có chắc chắn muốn từ chối doanh nghiệp "${company.companyName}"?\\n\\nDoanh nghiệp sẽ không thể đăng nhập.`
    );

    if (!confirmed) return;

    try {
      setActionLoading((prev) => ({ ...prev, [company.userId]: true }));
      await adminService.changeCompanyStatus(company.companyId, 'BLOCKED');
      toast.success(`Đã từ chối doanh nghiệp "${company.companyName}"`);
      fetchCompanies(); // Làm mới danh sách
    } catch (error) {
      console.error('Error rejecting company:', error);
      toast.error(error.response?.data?.message || 'Không thể từ chối doanh nghiệp');
    } finally {
      setActionLoading((prev) => ({ ...prev, [company.userId]: false }));
    }
  };

  /**
   * Xử lý xem chi tiết doanh nghiệp
   */
  const handleViewDetail = (company) => {
    setSelectedCompany(company);
    setShowDetailModal(true);
  };

  /**
   * Lấy nhãn trạng thái và màu sắc tương ứng
   */
  const getStatusBadge = (status) => {
    const badges = {
      PENDING: { label: 'Chờ duyệt', color: 'bg-yellow-100 text-yellow-800' },
      ACTIVE: { label: 'Đang hoạt động', color: 'bg-green-100 text-green-800' },
      BLOCKED: { label: 'Bị khóa', color: 'bg-red-100 text-red-800' },
    };
    return badges[status] || { label: status, color: 'bg-gray-100 text-gray-800' };
  };

  if (loading && companies.length === 0) {
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
        <h1 className="text-3xl font-bold text-neutral-900">Duyệt doanh nghiệp</h1>
        <p className="text-neutral-600 mt-2">
          Xem xét và phê duyệt các đăng ký doanh nghiệp mới
        </p>
      </div>

      {/* Status Filter */}
      <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-4 mb-6">
        <div className="flex gap-2">
          <button
            onClick={() => setStatusFilter('PENDING')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              statusFilter === 'PENDING'
                ? 'bg-yellow-600 text-white'
                : 'bg-neutral-100 text-neutral-700 hover:bg-neutral-200'
            }`}
          >
            Chờ duyệt
          </button>
          <button
            onClick={() => setStatusFilter('ACTIVE')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              statusFilter === 'ACTIVE'
                ? 'bg-green-600 text-white'
                : 'bg-neutral-100 text-neutral-700 hover:bg-neutral-200'
            }`}
          >
            Đã duyệt
          </button>
          <button
            onClick={() => setStatusFilter('BLOCKED')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              statusFilter === 'BLOCKED'
                ? 'bg-red-600 text-white'
                : 'bg-neutral-100 text-neutral-700 hover:bg-neutral-200'
            }`}
          >
            Đã từ chối
          </button>
          <button
            onClick={() => setStatusFilter('ALL')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              statusFilter === 'ALL'
                ? 'bg-red-600 text-white'
                : 'bg-neutral-100 text-neutral-700 hover:bg-neutral-200'
            }`}
          >
            Tất cả
          </button>
        </div>
      </div>

      {/* Companies Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {companies.length === 0 ? (
          <div className="col-span-full text-center py-12">
            <Building2 className="w-16 h-16 text-neutral-300 mx-auto mb-4" />
            <p className="text-neutral-500 text-lg">
              {statusFilter === 'PENDING'
                ? 'Không có doanh nghiệp nào đang chờ duyệt'
                : 'Không tìm thấy doanh nghiệp nào'}
            </p>
          </div>
        ) : (
          companies.map((company) => {
            const statusBadge = getStatusBadge(company.companyStatus);
            const isLoading = actionLoading[company.userId];

            return (
              <div
                key={company.userId}
                className="bg-white rounded-lg shadow-sm border border-neutral-200 p-6 hover:shadow-md transition-shadow"
              >
                {/* Company Header */}
                <div className="flex items-start gap-4 mb-4">
                  <div className="w-16 h-16 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
                    <Building2 className="w-8 h-8 text-blue-600" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <h3 className="text-lg font-semibold text-neutral-900 truncate">
                      {company.companyName}
                    </h3>
                    <p className="text-sm text-neutral-500">
                      Mã: {company.userCode}
                    </p>
                  </div>
                </div>

                {/* Status Badge */}
                <div className="mb-4">
                  <span
                    className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${statusBadge.color}`}
                  >
                    {statusBadge.label}
                  </span>
                </div>

                {/* Company Info Preview */}
                <div className="space-y-2 mb-4 text-sm">
                  <div className="flex items-start gap-2 text-neutral-600">
                    <Mail className="w-4 h-4 mt-0.5 flex-shrink-0" />
                    <span className="truncate">{company.username}</span>
                  </div>
                  {company.createdAt && (
                    <div className="flex items-center gap-2 text-neutral-600">
                      <Calendar className="w-4 h-4 flex-shrink-0" />
                      <span>
                        Đăng ký: {new Date(company.createdAt).toLocaleDateString('vi-VN')}
                      </span>
                    </div>
                  )}
                </div>

                {/* Actions */}
                <div className="flex gap-2">
                  {/* View Detail Button */}
                  <button
                    onClick={() => handleViewDetail(company)}
                    className="flex-1 inline-flex items-center justify-center gap-2 px-3 py-2 bg-blue-100 text-blue-700 rounded-lg hover:bg-blue-200 transition-colors"
                  >
                    <Eye className="w-4 h-4" />
                    Chi tiết
                  </button>

                  {/* Action Buttons - Only for PENDING status */}
                  {company.companyStatus === 'PENDING' && (
                    <>
                      <button
                        onClick={() => handleApprove(company)}
                        disabled={isLoading}
                        className="flex-1 inline-flex items-center justify-center gap-2 px-3 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50"
                      >
                        {isLoading ? (
                          <Loader2 className="w-4 h-4 animate-spin" />
                        ) : (
                          <CheckCircle className="w-4 h-4" />
                        )}
                        Duyệt
                      </button>
                      <button
                        onClick={() => handleReject(company)}
                        disabled={isLoading}
                        className="flex-1 inline-flex items-center justify-center gap-2 px-3 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50"
                      >
                        {isLoading ? (
                          <Loader2 className="w-4 h-4 animate-spin" />
                        ) : (
                          <XCircle className="w-4 h-4" />
                        )}
                        Từ chối
                      </button>
                    </>
                  )}
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Detail Modal */}
      {showDetailModal && selectedCompany && (
        <CompanyDetailModal
          company={selectedCompany}
          onClose={() => {
            setShowDetailModal(false);
            setSelectedCompany(null);
          }}
          onApprove={handleApprove}
          onReject={handleReject}
          isLoading={actionLoading[selectedCompany.userId]}
        />
      )}
    </div>
  );
};

/**
 * CompanyDetailModal - Modal to show full company details
 */
const CompanyDetailModal = ({ company, onClose, onApprove, onReject, isLoading }) => {
  const statusBadge = {
    PENDING: { label: 'Chờ duyệt', color: 'bg-yellow-100 text-yellow-800' },
    ACTIVE: { label: 'Đang hoạt động', color: 'bg-green-100 text-green-800' },
    BLOCKED: { label: 'Bị khóa', color: 'bg-red-100 text-red-800' },
  }[company.companyStatus];

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-xl shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        {/* Modal Header */}
        <div className="border-b border-neutral-200 p-6">
          <div className="flex items-start justify-between">
            <div className="flex items-start gap-4">
              <div className="w-16 h-16 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
                <Building2 className="w-8 h-8 text-blue-600" />
              </div>
              <div>
                <h2 className="text-2xl font-bold text-neutral-900">
                  {company.companyName}
                </h2>
                <p className="text-sm text-neutral-500 mt-1">
                  Mã doanh nghiệp: {company.userCode}
                </p>
                <span
                  className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full mt-2 ${statusBadge.color}`}
                >
                  {statusBadge.label}
                </span>
              </div>
            </div>
            <button
              onClick={onClose}
              className="text-neutral-400 hover:text-neutral-600 transition-colors"
            >
              <XCircle className="w-6 h-6" />
            </button>
          </div>
        </div>

        {/* Modal Body */}
        <div className="p-6 space-y-6">
          {/* Contact Information */}
          <div>
            <h3 className="text-lg font-semibold text-neutral-900 mb-3">
              Thông tin liên hệ
            </h3>
            <div className="space-y-3">
              <div className="flex items-start gap-3">
                <Mail className="w-5 h-5 text-neutral-400 mt-0.5" />
                <div>
                  <p className="text-sm text-neutral-500">Email</p>
                  <p className="text-neutral-900">{company.username}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Registration Date */}
          <div>
            <h3 className="text-lg font-semibold text-neutral-900 mb-3">
              Thông tin đăng ký
            </h3>
            <div className="space-y-3">
              <div className="flex items-start gap-3">
                <Calendar className="w-5 h-5 text-neutral-400 mt-0.5" />
                <div>
                  <p className="text-sm text-neutral-500">Ngày đăng ký</p>
                  <p className="text-neutral-900">
                    {company.createdAt
                      ? new Date(company.createdAt).toLocaleString('vi-VN')
                      : 'N/A'}
                  </p>
                </div>
              </div>
              {company.updatedAt && (
                <div className="flex items-start gap-3">
                  <Calendar className="w-5 h-5 text-neutral-400 mt-0.5" />
                  <div>
                    <p className="text-sm text-neutral-500">Cập nhật lần cuối</p>
                    <p className="text-neutral-900">
                      {new Date(company.updatedAt).toLocaleString('vi-VN')}
                    </p>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* User ID Info */}
          <div>
            <h3 className="text-lg font-semibold text-neutral-900 mb-3">
              Thông tin hệ thống
            </h3>
            <div className="bg-neutral-50 rounded-lg p-4 space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-neutral-500">User ID:</span>
                <span className="text-neutral-900 font-mono">{company.userId}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-neutral-500">Company ID:</span>
                <span className="text-neutral-900 font-mono">{company.companyId || 'N/A'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-neutral-500">Vai trò:</span>
                <span className="text-neutral-900">
                  {company.role?.roleName || 'Doanh nghiệp'}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Modal Footer - Actions */}
        {company.companyStatus === 'PENDING' && (
          <div className="border-t border-neutral-200 p-6 flex gap-3">
            <button
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-neutral-300 text-neutral-700 rounded-lg hover:bg-neutral-50 transition-colors"
            >
              Đóng
            </button>
            <button
              onClick={() => {
                onReject(company);
                onClose();
              }}
              disabled={isLoading}
              className="flex-1 inline-flex items-center justify-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50"
            >
              {isLoading ? (
                <Loader2 className="w-5 h-5 animate-spin" />
              ) : (
                <XCircle className="w-5 h-5" />
              )}
              Từ chối
            </button>
            <button
              onClick={() => {
                onApprove(company);
                onClose();
              }}
              disabled={isLoading}
              className="flex-1 inline-flex items-center justify-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50"
            >
              {isLoading ? (
                <Loader2 className="w-5 h-5 animate-spin" />
              ) : (
                <CheckCircle className="w-5 h-5" />
              )}
              Duyệt doanh nghiệp
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default CompanyApproval;
