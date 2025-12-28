import React, { useState, useEffect } from 'react';
import { Eye, CheckCircle, XCircle, Loader2, Briefcase, Building2, Calendar } from 'lucide-react';
import { toast } from 'react-toastify';
import adminService from '../../services/admin.service';

/**
 * JobModeration Component
 * Admin interface for reviewing and approving/rejecting job postings
 * 
 * Features:
 * - List jobs by status (Pending/Active/Rejected)
 * - View job details in modal
 * - Approve job (PENDING → ACTIVE)
 * - Reject job (PENDING → REJECTED)
 * - Tabs for different job statuses
 * 
 * API Integration:
 * - GET /api/v1/jobs?jobStatus=PENDING - List jobs by status
 * - PATCH /api/v1/admin/jobs/{id}/status?newStatus=ACTIVE - Approve job
 * - PATCH /api/v1/admin/jobs/{id}/status?newStatus=REJECTED - Reject job
 * 
 * Access Control:
 * - Only accessible by ROLE_ADM
 * - Protected by PrivateRoute in AppRoutes
 */
const JobModeration = () => {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState({});
  const [activeTab, setActiveTab] = useState('PENDING'); // PENDING, ACTIVE, REJECTED
  const [selectedJob, setSelectedJob] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);

  // Pagination state
  const [pagination, setPagination] = useState({
    totalPages: 0,
    totalElements: 0,
    currentPage: 0,
  });
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 20;

  // Fetch jobs when tab or page changes
  useEffect(() => {
    fetchJobs();
  }, [activeTab, currentPage]);

  /**
   * Fetch jobs from API with status filter
   */
  const fetchJobs = async () => {
    try {
      setLoading(true);
      const params = {
        page: currentPage,
        size: pageSize,
        sort: 'createdDate,desc',
        jobStatus: activeTab,
      };

      const response = await adminService.getJobsByStatus(params);
      const data = response.data?.data || response.data;

      // Handle paginated response
      if (data.content) {
        setJobs(data.content);
        setPagination({
          totalPages: data.totalPages,
          totalElements: data.totalElements,
          currentPage: data.number,
        });
      } else {
        setJobs(data || []);
      }
    } catch (error) {
      console.error('Error fetching jobs:', error);
      toast.error('Không thể tải danh sách tin tuyển dụng');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Handle tab change
   */
  const handleTabChange = (tab) => {
    setActiveTab(tab);
    setCurrentPage(0);
  };

  /**
   * Handle view job detail
   */
  const handleViewDetail = (job) => {
    setSelectedJob(job);
    setShowDetailModal(true);
  };

  /**
   * Handle approve job
   */
  const handleApproveJob = async (jobId, jobTitle) => {
    const confirmed = window.confirm(
      `Bạn có chắc chắn muốn duyệt tin tuyển dụng "${jobTitle}"?\n\nTin này sẽ được công khai và ứng viên có thể ứng tuyển.`
    );

    if (!confirmed) {
      return;
    }

    try {
      setActionLoading((prev) => ({ ...prev, [jobId]: true }));
      await adminService.approveJob(jobId);
      toast.success(`Đã duyệt tin "${jobTitle}"`);
      fetchJobs(); // Refresh list
    } catch (error) {
      console.error('Error approving job:', error);
      const errorMessage = error.response?.data?.message || 'Không thể duyệt tin tuyển dụng';
      toast.error(errorMessage);
    } finally {
      setActionLoading((prev) => ({ ...prev, [jobId]: false }));
    }
  };

  /**
   * Handle reject job
   */
  const handleRejectJob = async (jobId, jobTitle) => {
    const confirmed = window.confirm(
      `Bạn có chắc chắn muốn từ chối tin tuyển dụng "${jobTitle}"?\n\nTin này sẽ không được công khai.`
    );

    if (!confirmed) {
      return;
    }

    try {
      setActionLoading((prev) => ({ ...prev, [jobId]: true }));
      await adminService.rejectJob(jobId);
      toast.success(`Đã từ chối tin "${jobTitle}"`);
      fetchJobs(); // Refresh list
    } catch (error) {
      console.error('Error rejecting job:', error);
      const errorMessage = error.response?.data?.message || 'Không thể từ chối tin tuyển dụng';
      toast.error(errorMessage);
    } finally {
      setActionLoading((prev) => ({ ...prev, [jobId]: false }));
    }
  };

  /**
   * Get status badge
   */
  const getStatusBadge = (status) => {
    const badges = {
      PENDING: { label: 'Chờ duyệt', color: 'bg-yellow-100 text-yellow-800' },
      ACTIVE: { label: 'Đã duyệt', color: 'bg-green-100 text-green-800' },
      REJECTED: { label: 'Đã từ chối', color: 'bg-red-100 text-red-800' },
      CLOSED: { label: 'Đã đóng', color: 'bg-gray-100 text-gray-800' },
      HIDDEN: { label: 'Đã ẩn', color: 'bg-neutral-100 text-neutral-800' },
    };
    return badges[status] || { label: status, color: 'bg-gray-100 text-gray-800' };
  };

  /**
   * Format date
   */
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  /**
   * Format salary
   */
  const formatSalary = (salary) => {
    if (!salary) return 'Thỏa thuận';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(salary);
  };

  /**
   * Handle pagination
   */
  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
  };

  if (loading && jobs.length === 0) {
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
        <h1 className="text-3xl font-bold text-neutral-900">Duyệt tin tuyển dụng</h1>
        <p className="text-neutral-600 mt-2">
          Xem xét và phê duyệt các tin tuyển dụng từ doanh nghiệp
        </p>
      </div>

      {/* Tabs */}
      <div className="bg-white rounded-lg shadow-sm border border-neutral-200 mb-6">
        <div className="flex border-b border-neutral-200">
          <button
            onClick={() => handleTabChange('PENDING')}
            className={`flex-1 px-6 py-4 font-medium transition-colors relative ${
              activeTab === 'PENDING'
                ? 'text-red-600 border-b-2 border-red-600'
                : 'text-neutral-600 hover:text-neutral-900'
            }`}
          >
            Chờ duyệt
            {activeTab === 'PENDING' && pagination.totalElements > 0 && (
              <span className="ml-2 px-2 py-1 text-xs bg-red-100 text-red-800 rounded-full">
                {pagination.totalElements}
              </span>
            )}
          </button>
          <button
            onClick={() => handleTabChange('ACTIVE')}
            className={`flex-1 px-6 py-4 font-medium transition-colors relative ${
              activeTab === 'ACTIVE'
                ? 'text-red-600 border-b-2 border-red-600'
                : 'text-neutral-600 hover:text-neutral-900'
            }`}
          >
            Đã duyệt
            {activeTab === 'ACTIVE' && pagination.totalElements > 0 && (
              <span className="ml-2 px-2 py-1 text-xs bg-green-100 text-green-800 rounded-full">
                {pagination.totalElements}
              </span>
            )}
          </button>
          <button
            onClick={() => handleTabChange('REJECTED')}
            className={`flex-1 px-6 py-4 font-medium transition-colors relative ${
              activeTab === 'REJECTED'
                ? 'text-red-600 border-b-2 border-red-600'
                : 'text-neutral-600 hover:text-neutral-900'
            }`}
          >
            Đã từ chối
            {activeTab === 'REJECTED' && pagination.totalElements > 0 && (
              <span className="ml-2 px-2 py-1 text-xs bg-red-100 text-red-800 rounded-full">
                {pagination.totalElements}
              </span>
            )}
          </button>
        </div>
      </div>

      {/* Jobs Table */}
      <div className="bg-white rounded-lg shadow-sm border border-neutral-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-neutral-50 border-b border-neutral-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                Tin tuyển dụng
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                Công ty
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                Ngày đăng
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                Trạng thái
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-neutral-500 uppercase tracking-wider">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-neutral-200">
            {jobs.length === 0 ? (
              <tr>
                <td colSpan="5" className="px-6 py-8 text-center text-neutral-500">
                  {activeTab === 'PENDING' && 'Không có tin tuyển dụng chờ duyệt'}
                  {activeTab === 'ACTIVE' && 'Chưa có tin tuyển dụng nào được duyệt'}
                  {activeTab === 'REJECTED' && 'Chưa có tin tuyển dụng nào bị từ chối'}
                </td>
              </tr>
            ) : (
              jobs.map((job) => {
                const statusBadge = getStatusBadge(job.jobStatus);
                const isLoading = actionLoading[job.jobId];

                return (
                  <tr key={job.jobId} className="hover:bg-neutral-50">
                    {/* Job Title */}
                    <td className="px-6 py-4">
                      <div className="flex items-start gap-3">
                        <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
                          <Briefcase className="w-5 h-5 text-blue-600" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="text-sm font-medium text-neutral-900 truncate">
                            {job.jobTitle}
                          </div>
                          <div className="text-xs text-neutral-500 mt-1">
                            ID: {job.jobId} • {job.jobLocation || 'N/A'}
                          </div>
                        </div>
                      </div>
                    </td>

                    {/* Company Name */}
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        <Building2 className="w-4 h-4 text-neutral-400" />
                        <span className="text-sm text-neutral-700">
                          {job.companyName || 'N/A'}
                        </span>
                      </div>
                    </td>

                    {/* Posted Date */}
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center gap-2 text-sm text-neutral-600">
                        <Calendar className="w-4 h-4 text-neutral-400" />
                        {formatDate(job.createdDate)}
                      </div>
                    </td>

                    {/* Status */}
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span
                        className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${statusBadge.color}`}
                      >
                        {statusBadge.label}
                      </span>
                    </td>

                    {/* Actions */}
                    <td className="px-6 py-4 whitespace-nowrap text-right">
                      <div className="flex items-center justify-end gap-2">
                        {/* View Detail Button */}
                        <button
                          onClick={() => handleViewDetail(job)}
                          className="inline-flex items-center gap-1 px-3 py-1 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                          title="Xem chi tiết"
                        >
                          <Eye className="w-4 h-4" />
                          <span className="text-sm">Xem</span>
                        </button>

                        {/* Approve Button (only for PENDING) */}
                        {job.jobStatus === 'PENDING' && (
                          <button
                            onClick={() => handleApproveJob(job.jobId, job.jobTitle)}
                            disabled={isLoading}
                            className="inline-flex items-center gap-1 px-3 py-1 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50"
                          >
                            {isLoading ? (
                              <Loader2 className="w-4 h-4 animate-spin" />
                            ) : (
                              <CheckCircle className="w-4 h-4" />
                            )}
                            <span className="text-sm">Duyệt tin</span>
                          </button>
                        )}

                        {/* Reject Button (only for PENDING) */}
                        {job.jobStatus === 'PENDING' && (
                          <button
                            onClick={() => handleRejectJob(job.jobId, job.jobTitle)}
                            disabled={isLoading}
                            className="inline-flex items-center gap-1 px-3 py-1 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50"
                          >
                            {isLoading ? (
                              <Loader2 className="w-4 h-4 animate-spin" />
                            ) : (
                              <XCircle className="w-4 h-4" />
                            )}
                            <span className="text-sm">Từ chối</span>
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
              <span className="font-medium">{pagination.currentPage * pageSize + 1}</span> đến{' '}
              <span className="font-medium">
                {Math.min((pagination.currentPage + 1) * pageSize, pagination.totalElements)}
              </span>{' '}
              trong tổng số <span className="font-medium">{pagination.totalElements}</span> tin
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

      {/* Job Detail Modal */}
      {showDetailModal && selectedJob && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
          onClick={() => setShowDetailModal(false)}
        >
          <div
            className="bg-white rounded-lg shadow-xl max-w-3xl w-full max-h-[90vh] overflow-y-auto"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Modal Header */}
            <div className="sticky top-0 bg-white border-b border-neutral-200 px-6 py-4 flex items-center justify-between">
              <h2 className="text-xl font-bold text-neutral-900">Chi tiết tin tuyển dụng</h2>
              <button
                onClick={() => setShowDetailModal(false)}
                className="text-neutral-400 hover:text-neutral-600"
              >
                <XCircle className="w-6 h-6" />
              </button>
            </div>

            {/* Modal Body */}
            <div className="p-6 space-y-6">
              {/* Job Title */}
              <div>
                <h3 className="text-2xl font-bold text-neutral-900">{selectedJob.jobTitle}</h3>
                <div className="flex items-center gap-4 mt-2 text-sm text-neutral-600">
                  <span className="flex items-center gap-1">
                    <Building2 className="w-4 h-4" />
                    {selectedJob.companyName || 'N/A'}
                  </span>
                  <span className="flex items-center gap-1">
                    <Calendar className="w-4 h-4" />
                    {formatDate(selectedJob.createdDate)}
                  </span>
                  <span className={`px-2 py-1 rounded-full text-xs font-semibold ${getStatusBadge(selectedJob.jobStatus).color}`}>
                    {getStatusBadge(selectedJob.jobStatus).label}
                  </span>
                </div>
              </div>

              {/* Job Info Grid */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-neutral-500">Mức lương</label>
                  <p className="text-base font-semibold text-green-600 mt-1">
                    {formatSalary(selectedJob.jobSalary)}
                  </p>
                </div>
                <div>
                  <label className="text-sm font-medium text-neutral-500">Địa điểm</label>
                  <p className="text-base text-neutral-900 mt-1">
                    {selectedJob.jobLocation || 'N/A'}
                  </p>
                </div>
                <div>
                  <label className="text-sm font-medium text-neutral-500">Danh mục</label>
                  <p className="text-base text-neutral-900 mt-1">
                    {selectedJob.categoryName || 'N/A'}
                  </p>
                </div>
                <div>
                  <label className="text-sm font-medium text-neutral-500">Hạn nộp hồ sơ</label>
                  <p className="text-base text-neutral-900 mt-1">
                    {formatDate(selectedJob.jobDeadline)}
                  </p>
                </div>
              </div>

              {/* Job Description */}
              <div>
                <label className="text-sm font-medium text-neutral-500">Mô tả công việc</label>
                <div className="mt-2 text-neutral-700 whitespace-pre-wrap bg-neutral-50 p-4 rounded-lg">
                  {selectedJob.jobDescription || 'Không có mô tả'}
                </div>
              </div>

              {/* Job Requirements */}
              <div>
                <label className="text-sm font-medium text-neutral-500">Yêu cầu công việc</label>
                <div className="mt-2 text-neutral-700 whitespace-pre-wrap bg-neutral-50 p-4 rounded-lg">
                  {selectedJob.jobRequirements || 'Không có yêu cầu'}
                </div>
              </div>

              {/* Job Benefits */}
              {selectedJob.jobBenefits && (
                <div>
                  <label className="text-sm font-medium text-neutral-500">Quyền lợi</label>
                  <div className="mt-2 text-neutral-700 whitespace-pre-wrap bg-neutral-50 p-4 rounded-lg">
                    {selectedJob.jobBenefits}
                  </div>
                </div>
              )}

              {/* Action Buttons (only for PENDING) */}
              {selectedJob.jobStatus === 'PENDING' && (
                <div className="flex gap-3 pt-4 border-t border-neutral-200">
                  <button
                    onClick={() => {
                      setShowDetailModal(false);
                      handleApproveJob(selectedJob.jobId, selectedJob.jobTitle);
                    }}
                    className="flex-1 flex items-center justify-center gap-2 px-4 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors font-medium"
                  >
                    <CheckCircle className="w-5 h-5" />
                    Duyệt tin tuyển dụng
                  </button>
                  <button
                    onClick={() => {
                      setShowDetailModal(false);
                      handleRejectJob(selectedJob.jobId, selectedJob.jobTitle);
                    }}
                    className="flex-1 flex items-center justify-center gap-2 px-4 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors font-medium"
                  >
                    <XCircle className="w-5 h-5" />
                    Từ chối tin tuyển dụng
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default JobModeration;
