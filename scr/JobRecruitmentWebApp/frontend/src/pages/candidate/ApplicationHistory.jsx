import React, { useState, useEffect } from 'react';
import { Briefcase, Calendar, Building2, FileCheck, Trash2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import axiosClient from '../../api/axiosClient';
import applicationService from '../../services/application.service';

/**
 * Tiện ích: Tạo URL đầy đủ cho file tải lên từ backend
 */
const getBackendFileUrl = (filePath) => {
  if (!filePath) return null;
  if (filePath.startsWith('http')) return filePath;
  const baseUrl = import.meta.env.VITE_API_URL?.replace('/api/v1', '') || 'http://localhost:5000';
  return `${baseUrl}${filePath}`;
};

/**
 * ApplicationHistory Component
 * Hiển thị danh sách đơn ứng tuyển của ứng viên dưới dạng bảng
 * 
 * Tính năng:
 * - Bảng liệt kê tất cả đơn ứng tuyển (Tiêu đề công việc, Công ty, Ngày, Trạng thái)
 * - Badge trạng thái với mã màu:
 *   - PENDING: Vàng (Đang chờ)
 *   - APPROVED: Xanh lá (Đã duyệt)
 *   - REJECTED: Đỏ (Đã từ chối)
 * - Hỗ trợ phân trang
 * - API: GET /api/v1/applications/me
 */
const ApplicationHistory = () => {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [withdrawingId, setWithdrawingId] = useState(null);
  const [pagination, setPagination] = useState({
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    size: 10,
  });

  useEffect(() => {
    fetchApplications(0);
  }, []);

  const fetchApplications = async (page = 0) => {
    setLoading(true);
    setError('');
    try {
      const response = await axiosClient.get('/applications/me', {
        params: {
          page,
          size: 10,
          sort: 'applyTime,desc',
        },
      });

      const data = response.data;
      console.log('ApplicationHistory API Response:', data);
      
      setApplications(data.content || []);
      setPagination({
        currentPage: data.number || 0,
        totalPages: data.totalPages || 0,
        totalElements: data.totalElements || 0,
        size: data.size || 10,
      });
    } catch (err) {
      console.error('Error fetching applications:', err);
      setError('Không thể tải danh sách đơn ứng tuyển. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  };

  const handleWithdraw = async (applicationId, jobTitle) => {
    const confirmWithdraw = window.confirm(
      `Bạn có chắc chắn muốn rút đơn ứng tuyển vào "${jobTitle}"?\n\nHành động này không thể hoàn tác.`
    );

    if (!confirmWithdraw) return;

    try {
      setWithdrawingId(applicationId);
      console.log('Withdrawing application:', applicationId);
      
      await applicationService.withdrawApplication(applicationId);
      
      toast.success('Rút đơn ứng tuyển thành công');
      
      // Làm mới danh sách đơn ứng tuyển
      await fetchApplications(pagination.currentPage);
    } catch (error) {
      console.error('Error withdrawing application:', error);
      const errorMsg = error.response?.data?.message || 'Không thể rút đơn ứng tuyển. Vui lòng thử lại.';
      toast.error(errorMsg);
    } finally {
      setWithdrawingId(null);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      PENDING: {
        label: 'Đang chờ',
        bgColor: 'bg-yellow-100',
        textColor: 'text-yellow-800',
        icon: '⏳',
      },
      APPROVED: {
        label: 'Đã duyệt',
        bgColor: 'bg-green-100',
        textColor: 'text-green-800',
        icon: '✓',
      },
      REJECTED: {
        label: 'Đã từ chối',
        bgColor: 'bg-red-100',
        textColor: 'text-red-800',
        icon: '✗',
      },
    };

    const config = statusConfig[status] || statusConfig.PENDING;

    return (
      <span
        className={`inline-flex items-center gap-1 px-3 py-1 ${config.bgColor} ${config.textColor} rounded-full text-sm font-medium`}
      >
        <span>{config.icon}</span>
        {config.label}
      </span>
    );
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pagination.totalPages) {
      fetchApplications(newPage);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-sm min-h-[calc(100vh-200px)]">
      {/* Header */}
      <div className="px-6 py-5 border-b border-neutral-200">
        <h1 className="text-2xl font-bold text-neutral-900">Lịch sử nộp đơn</h1>
        <p className="text-sm text-neutral-600 mt-1">
          Theo dõi trạng thái các đơn ứng tuyển của bạn
        </p>
      </div>

      {/* Error Message */}
      {error && (
        <div className="mx-6 mt-6">
          <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg">
            {error}
          </div>
        </div>
      )}

      {/* Stats */}
      {!loading && applications.length > 0 && (
        <div className="px-6 py-4 bg-neutral-50 border-b border-neutral-200">
          <div className="flex items-center gap-2 text-sm text-neutral-600">
            <FileCheck className="w-5 h-5 text-primary" />
            <span>
              Tổng số đơn: <span className="font-semibold text-neutral-900">{pagination.totalElements}</span>
            </span>
          </div>
        </div>
      )}

      {/* Table */}
      <div className="px-6 pb-6">
        {loading ? (
          <div className="flex items-center justify-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
          </div>
        ) : applications.length === 0 ? (
          <div className="text-center py-12">
            <Briefcase className="w-16 h-16 text-neutral-300 mx-auto mb-4" />
            <p className="text-neutral-500 text-lg">Chưa có đơn ứng tuyển nào</p>
            <p className="text-neutral-400 text-sm mt-2">
              Tìm kiếm và ứng tuyển vào các công việc phù hợp
            </p>
            <Link
              to="/jobs"
              className="inline-block mt-4 px-6 py-2 bg-primary text-white rounded-lg hover:bg-primary-600 transition-colors font-medium"
            >
              Tìm việc ngay
            </Link>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto mt-6">
              <table className="w-full">
                <thead>
                  <tr className="bg-neutral-50 border-b border-neutral-200">
                    <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">
                      Mã đơn
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">
                      Vị trí công việc
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">
                      Ngày nộp
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">
                      Trạng thái
                    </th>
                    <th className="px-6 py-4 text-center text-sm font-semibold text-neutral-700">
                      Chi tiết
                    </th>
                    <th className="px-6 py-4 text-center text-sm font-semibold text-neutral-700">
                      Hành động
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-neutral-200">
                  {applications.map((application) => (
                    <tr key={application.applicationId} className="hover:bg-neutral-50 transition-colors">
                      <td className="px-6 py-4 text-sm text-neutral-900 font-medium">
                        {application.applicationCode}
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-start gap-2">
                          <Briefcase className="w-5 h-5 text-primary mt-0.5 flex-shrink-0" />
                          <div>
                            <p className="text-sm font-semibold text-neutral-900">
                              {application.jobTitle || 'Chưa cập nhật'}
                            </p>
                            <p className="text-xs text-neutral-500 mt-0.5">
                              {application.jobCode || ''}
                            </p>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-2 text-sm text-neutral-600">
                          <Calendar className="w-4 h-4 text-neutral-400" />
                          <span>{formatDateTime(application.applyTime)}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        {getStatusBadge(application.applicationStatus)}
                      </td>
                      <td className="px-6 py-4 text-center">
                        <Link
                          to={`/jobs/${application.jobId}`}
                          className="text-primary hover:text-primary-600 font-medium text-sm hover:underline"
                        >
                          Xem tin
                        </Link>
                      </td>
                      <td className="px-6 py-4 text-center">
                        {application.applicationStatus === 'PENDING' ? (
                          <button
                            onClick={() => handleWithdraw(application.applicationId, application.jobTitle)}
                            disabled={withdrawingId === application.applicationId}
                            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-sm font-medium text-red-700 bg-red-50 hover:bg-red-100 border border-red-200 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                            title="Rút đơn ứng tuyển"
                          >
                            <Trash2 className="w-4 h-4" />
                            {withdrawingId === application.applicationId ? 'Đang xử lý...' : 'Rút đơn'}
                          </button>
                        ) : (
                          <span className="text-xs text-neutral-400">-</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {pagination.totalPages > 1 && (
              <div className="flex items-center justify-between mt-6 pt-6 border-t border-neutral-200">
                <p className="text-sm text-neutral-600">
                  Trang {pagination.currentPage + 1} / {pagination.totalPages}
                  {' '}({pagination.totalElements} đơn)
                </p>
                <div className="flex gap-2">
                  <button
                    onClick={() => handlePageChange(pagination.currentPage - 1)}
                    disabled={pagination.currentPage === 0}
                    className="px-4 py-2 border border-neutral-300 text-neutral-700 rounded-lg hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-medium text-sm"
                  >
                    Trước
                  </button>
                  <button
                    onClick={() => handlePageChange(pagination.currentPage + 1)}
                    disabled={pagination.currentPage >= pagination.totalPages - 1}
                    className="px-4 py-2 border border-neutral-300 text-neutral-700 rounded-lg hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-medium text-sm"
                  >
                    Sau
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default ApplicationHistory;
