/**
 * EmployerApplications.jsx - Quản lý đơn ứng tuyển của nhà tuyển dụng
 * 
 * Features:
 * - Danh sách tất cả đơn ứng tuyển nhận được
 * - Lọc theo công việc và trạng thái
 * - Xem thông tin ứng viên
 * - Duyệt/Từ chối đơn ứng tuyển
 * - Phân trang
 * 
 * API Endpoints:
 * - GET /api/v1/applications/company (fetch applications)
 * - PATCH /api/v1/applications/{id}/status (update status)
 * - GET /api/v1/jobs (fetch employer's jobs for filter)
 * - GET /api/v1/candidates/{id} (view candidate profile)
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  FileText, 
  Filter, 
  Loader2, 
  CheckCircle, 
  XCircle, 
  Clock,
  User,
  Briefcase,
  Calendar,
  ExternalLink,
  Search,
  Copy,
  FileSpreadsheet,
  FileText as FilePDF
} from 'lucide-react';
import { format } from 'date-fns';
import { toast } from 'react-toastify';
import applicationService from '../../services/application.service';
import jobService from '../../services/job.service';
import { copyToClipboard, exportToExcel, exportToPDF } from '../../utils/tableExport';

const EmployerApplications = () => {
  const navigate = useNavigate();

  // Quản lý trạng thái
  const [applications, setApplications] = useState([]);
  const [filteredApplications, setFilteredApplications] = useState([]);
  const [myJobs, setMyJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  // Quản lý phân trang
  const [pagination, setPagination] = useState({
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    pageSize: 10
  });

  // Quản lý trạng thái bộ lọc
  const [filters, setFilters] = useState({
    jobId: '',
    status: ''
  });

  // Quản lý trạng thái modal chi tiết ứng viên
  const [selectedCandidate, setSelectedCandidate] = useState(null);
  const [showCandidateModal, setShowCandidateModal] = useState(false);

  useEffect(() => {
    // Áp dụng bộ lọc tìm kiếm
    if (searchTerm.trim()) {
      const term = searchTerm.toLowerCase();
      const filtered = applications.filter(app =>
        app.candidate?.candidateName?.toLowerCase().includes(term) ||
        app.candidate?.candidateEmail?.toLowerCase().includes(term) ||
        app.job?.jobTitle?.toLowerCase().includes(term)
      );
      setFilteredApplications(filtered);
    } else {
      setFilteredApplications(applications);
    }
  }, [applications, searchTerm]);

  /**
   * Lấy danh sách đơn ứng tuyển với bộ lọc và phân trang
   */
  const fetchApplications = async (page = 0) => {
    try {
      setLoading(true);
      const params = {
        page,
        size: pagination.pageSize,
        sort: 'applyTime,desc'
      };

      // Thêm bộ lọc nếu được đặt
      if (filters.jobId) {
        params.jobId = filters.jobId;
      }
      if (filters.status) {
        params.status = filters.status;
      }

      const response = await applicationService.getCompanyApplications(params);
      
      setApplications(response.content || []);
      setPagination({
        currentPage: response.number || 0,
        totalPages: response.totalPages || 0,
        totalElements: response.totalElements || 0,
        pageSize: response.size || 10
      });
    } catch (error) {
      console.error('Failed to fetch applications:', error);
      toast.error('Không thể tải danh sách ứng tuyển. Vui lòng thử lại!');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Lấy danh sách công việc của nhà tuyển dụng cho dropdown bộ lọc
   */
  const fetchMyJobs = async () => {
    try {
      const params = {
        page: 0,
        size: 1000, // Lấy tất cả công việc cho dropdown
        sort: 'createdAt,desc'
      };

      const response = await jobService.getMyJobs(params);
      setMyJobs(response.content || []);
    } catch (error) {
      console.error('Failed to fetch jobs:', error);
      toast.error('Không thể tải danh sách công việc!');
    }
  };

  /**
   * Xử lý cập nhật trạng thái (Duyệt/Từ chối) đơn ứng tuyển
   */
  const handleStatusUpdate = async (applicationId, newStatus, candidateName) => {
    const statusText = newStatus === 'APPROVED' ? 'duyệt' : 'từ chối';
    const confirmMessage = `Bạn có chắc chắn muốn ${statusText} đơn ứng tuyển của ${candidateName}?`;

    if (!window.confirm(confirmMessage)) {
      return;
    }

    try {
      setUpdatingId(applicationId);

      await applicationService.updateApplicationStatus(applicationId, newStatus);

      setApplications(prev =>
        prev.map(app => {
          const appId = app.appId || app.applicationId;
          if (appId === applicationId) {
            return { 
              ...app, 
              appStatus: newStatus,
              applicationStatus: newStatus 
            };
          }
          return app;
        })
      );

      toast.success(`Đã ${statusText} đơn ứng tuyển thành công!`);
    } catch (error) {
      console.error('Status update failed:', error);
      toast.error(`Không thể ${statusText} đơn ứng tuyển. Vui lòng thử lại!`);
    } finally {
      setUpdatingId(null);
    }
  };

  /**
   * Xử lý thay đổi bộ lọc
   */
  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({
      ...prev,
      [name]: value
    }));
  };

  /**
   * Áp dụng bộ lọc
   */
  const applyFilters = () => {
    fetchApplications(0); // Reset to first page
  };

  /**
   * Xử lý xuất dữ liệu sang các định dạng khác nhau
   */
  const handleExport = async (type) => {
    const columns = [
      { header: 'Ứng viên', accessor: (row) => row.candidate?.candidateName || 'N/A' },
      { header: 'Email', accessor: (row) => row.candidate?.candidateEmail || 'N/A' },
      { header: 'Công việc', accessor: (row) => row.job?.jobTitle || 'N/A' },
      { header: 'CV', accessor: (row) => row.cvPath || 'Chưa có' },
      { header: 'Ngày nộp', accessor: (row) => row.applyTime ? format(new Date(row.applyTime), 'dd/MM/yyyy HH:mm') : 'N/A' },
      { header: 'Trạng thái', accessor: (row) => {
        const statusMap = { 
          PENDING: 'Chờ duyệt', 
          APPROVED: 'Đã duyệt', 
          REJECTED: 'Từ chối' 
        };
        return statusMap[row.apStatus] || row.apStatus;
      }}
    ];

    const dataToExport = filteredApplications.length > 0 ? filteredApplications : applications;

    try {
      switch (type) {
        case 'copy':
          await copyToClipboard(dataToExport, columns);
          toast.success('Đã sao chép vào clipboard!');
          break;
        case 'excel':
          exportToExcel(dataToExport, columns, 'danh-sach-ung-tuyen.csv');
          toast.success('Đã xuất file Excel!');
          break;
        case 'pdf':
          await exportToPDF(dataToExport, columns, 'danh-sach-ung-tuyen.pdf', {
            title: 'Danh Sách Ứng Tuyển'
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
   * Đặt lại bộ lọc
   */
  const resetFilters = () => {
    setFilters({
      jobId: '',
      status: ''
    });
    // Lấy dữ liệu không có bộ lọc
    setTimeout(() => {
      fetchApplications(0);
    }, 0);
  };

  /**
   * Lấy badge trạng thái ứng tuyển
   */
  const getStatusBadge = (status) => {
    const statusConfig = {
      PENDING: {
        label: 'Đang chờ',
        className: 'bg-yellow-100 text-yellow-800',
        icon: <Clock className="w-3 h-3" />
      },
      APPROVED: {
        label: 'Đã duyệt',
        className: 'bg-green-100 text-green-800',
        icon: <CheckCircle className="w-3 h-3" />
      },
      REJECTED: {
        label: 'Đã từ chối',
        className: 'bg-red-100 text-red-800',
        icon: <XCircle className="w-3 h-3" />
      }
    };

    const config = statusConfig[status] || statusConfig.PENDING;

    return (
      <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium ${config.className}`}>
        {config.icon}
        {config.label}
      </span>
    );
  };

  /**
   * Định dạng ngày tháng thành DD/MM/YYYY HH:mm
   */
  const formatDateTime = (dateString) => {
    try {
      return format(new Date(dateString), 'dd/MM/yyyy HH:mm');
    } catch {
      return dateString;
    }
  };

  /**
   * Điều khiển phân trang
   */
  const renderPagination = () => {
    if (pagination.totalPages <= 1) return null;

    const pages = [];
    for (let i = 0; i < pagination.totalPages; i++) {
      pages.push(i);
    }

    return (
      <div className="flex items-center justify-between mt-6 pt-4 border-t">
        <div className="text-sm text-neutral-600">
          Hiển thị {applications.length > 0 ? pagination.currentPage * pagination.pageSize + 1 : 0} - {Math.min((pagination.currentPage + 1) * pagination.pageSize, pagination.totalElements)} trong tổng số {pagination.totalElements} ứng tuyển
        </div>

        <div className="flex gap-2">
          <button
            onClick={() => fetchApplications(pagination.currentPage - 1)}
            disabled={pagination.currentPage === 0}
            className="px-4 py-2 text-sm font-medium text-neutral-700 bg-white border border-neutral-300 rounded-lg hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Trước
          </button>

          {pages.map(page => (
            <button
              key={page}
              onClick={() => fetchApplications(page)}
              className={`px-4 py-2 text-sm font-medium rounded-lg ${
                page === pagination.currentPage
                  ? 'bg-primary text-white'
                  : 'text-neutral-700 bg-white border border-neutral-300 hover:bg-neutral-50'
              }`}
            >
              {page + 1}
            </button>
          ))}

          <button
            onClick={() => fetchApplications(pagination.currentPage + 1)}
            disabled={pagination.currentPage === pagination.totalPages - 1}
            className="px-4 py-2 text-sm font-medium text-neutral-700 bg-white border border-neutral-300 rounded-lg hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Sau
          </button>
        </div>
      </div>
    );
  };

  // Lấy dữ liệu ban đầu
  useEffect(() => {
    fetchApplications();
    fetchMyJobs();
  }, []);

  if (loading && applications.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
        <span className="ml-2 text-neutral-600">Đang tải...</span>
      </div>
    );
  }

  return (
    <div className="p-6 min-h-[calc(100vh-200px)]">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-neutral-800 mb-2">
          Quản lý Đơn Ứng Tuyển
        </h1>
        <p className="text-neutral-600">
          Xem và quản lý tất cả đơn ứng tuyển cho các tin tuyển dụng của bạn
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
                placeholder="Tìm kiếm theo tên ứng viên, email, công việc..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
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
            Tìm thấy {filteredApplications.length} kết quả cho "{searchTerm}"
          </div>
        )}
      </div>

      {/* Filters */}
      <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-4 mb-6">
        <div className="flex items-center gap-2 mb-4">
          <Filter className="w-5 h-5 text-primary" />
          <h2 className="text-lg font-semibold text-neutral-800">Bộ lọc</h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Job filter */}
          <div>
            <label className="block text-sm font-medium text-neutral-700 mb-2">
              Công việc
            </label>
            <select
              name="jobId"
              value={filters.jobId}
              onChange={handleFilterChange}
              className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
            >
              <option value="">Tất cả công việc</option>
              {myJobs.map(job => (
                <option key={job.jobId} value={job.jobId}>
                  {job.jobTitle}
                </option>
              ))}
            </select>
          </div>

          {/* Status filter */}
          <div>
            <label className="block text-sm font-medium text-neutral-700 mb-2">
              Trạng thái
            </label>
            <select
              name="status"
              value={filters.status}
              onChange={handleFilterChange}
              className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
            >
              <option value="">Tất cả trạng thái</option>
              <option value="PENDING">Đang chờ</option>
              <option value="APPROVED">Đã duyệt</option>
              <option value="REJECTED">Đã từ chối</option>
            </select>
          </div>

          {/* Action buttons */}
          <div className="flex items-end gap-2">
            <button
              onClick={applyFilters}
              className="flex-1 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors font-medium"
            >
              Áp dụng
            </button>
            <button
              onClick={resetFilters}
              className="px-4 py-2 bg-neutral-100 text-neutral-700 rounded-lg hover:bg-neutral-200 transition-colors font-medium"
            >
              Đặt lại
            </button>
          </div>
        </div>
      </div>

      {/* Applications Table */}
      {applications.length === 0 ? (
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-12 text-center">
          <FileText className="w-16 h-16 mx-auto text-neutral-300 mb-4" />
          <h3 className="text-lg font-semibold text-neutral-800 mb-2">
            Chưa có đơn ứng tuyển nào
          </h3>
          <p className="text-neutral-600">
            {filters.jobId || filters.status
              ? 'Không tìm thấy đơn ứng tuyển phù hợp với bộ lọc.'
              : 'Các đơn ứng tuyển sẽ hiển thị ở đây khi ứng viên nộp đơn.'}
          </p>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-neutral-50 border-b border-neutral-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-neutral-700 uppercase tracking-wider">
                    Ứng viên
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-neutral-700 uppercase tracking-wider">
                    Công việc
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-neutral-700 uppercase tracking-wider">
                    Ngày ứng tuyển
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-neutral-700 uppercase tracking-wider">
                    CV
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-neutral-700 uppercase tracking-wider">
                    Trạng thái
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-neutral-700 uppercase tracking-wider">
                    Thao tác
                  </th>
                </tr>
              </thead>

              <tbody className="divide-y divide-neutral-200">
                {filteredApplications.length === 0 ? (
                  <tr>
                    <td colSpan="6" className="px-6 py-12 text-center">
                      <Search className="w-12 h-12 mx-auto text-neutral-300 mb-3" />
                      <p className="text-neutral-600 font-medium">Không tìm thấy đơn ứng tuyển nào</p>
                      <p className="text-sm text-neutral-500 mt-1">
                        Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc
                      </p>
                    </td>
                  </tr>
                ) : (
                  filteredApplications.map(application => {
                  // Handle both nested and flattened response structures
                  const appId = application.appId || application.applicationId;
                  const appStatus = application.appStatus || application.applicationStatus;
                  const jobTitle = application.job?.jobTitle || application.jobTitle || 'N/A';
                  const candidateName = application.candidateName || application.candidate?.candidateName || `Ứng viên #${application.candidateId || 'N/A'}`;
                  const candidateId = application.candidateId || application.candidate?.candidateId;
                  const cvFile = application.cvFile || application.cv?.cvFile || (application.cvId ? `/api/v1/cvs/${application.cvId}/download` : null);
                  
                  return (
                    <tr key={appId} className="hover:bg-neutral-50 transition-colors">
                      {/* Candidate Name */}
                      <td className="px-6 py-4">
                        {candidateId ? (
                          <button
                            onClick={() => navigate(`/employer/candidates/${candidateId}`)}
                            className="flex items-center gap-2 text-primary hover:text-primary/80 font-medium"
                          >
                            <User className="w-4 h-4" />
                            {candidateName}
                          </button>
                        ) : (
                          <div className="flex items-center gap-2 text-neutral-500">
                            <User className="w-4 h-4" />
                            {candidateName}
                          </div>
                        )}
                      </td>

                      {/* Job Title */}
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-2">
                          <Briefcase className="w-4 h-4 text-neutral-400" />
                          <span className="text-sm text-neutral-800">
                            {jobTitle}
                          </span>
                        </div>
                      </td>

                      {/* Apply Date */}
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-2 text-sm text-neutral-600">
                          <Calendar className="w-4 h-4 text-neutral-400" />
                          {formatDateTime(application.applyTime)}
                        </div>
                      </td>

                      {/* CV Link */}
                      <td className="px-6 py-4">
                        {cvFile ? (
                          <a
                            href={cvFile}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="inline-flex items-center gap-1 text-primary hover:text-primary/80 text-sm font-medium"
                          >
                            <FileText className="w-4 h-4" />
                            Xem CV
                            <ExternalLink className="w-3 h-3" />
                          </a>
                        ) : (
                          <span className="text-sm text-neutral-400">Không có CV</span>
                        )}
                      </td>

                      {/* Status */}
                      <td className="px-6 py-4">
                        {getStatusBadge(appStatus)}
                      </td>

                      {/* Actions */}
                      <td className="px-6 py-4">
                        {appStatus === 'PENDING' && (
                          <div className="flex gap-2">
                            <button
                              onClick={() => handleStatusUpdate(appId, 'APPROVED', candidateName)}
                              disabled={updatingId === appId}
                              className="inline-flex items-center gap-1 px-3 py-1.5 bg-green-500 text-white text-sm font-medium rounded-lg hover:bg-green-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                              {updatingId === appId ? (
                                <>
                                  <Loader2 className="w-4 h-4 animate-spin" />
                                  Đang xử lý...
                                </>
                              ) : (
                                <>
                                  <CheckCircle className="w-4 h-4" />
                                  Duyệt
                                </>
                              )}
                            </button>

                            <button
                              onClick={() => handleStatusUpdate(appId, 'REJECTED', candidateName)}
                              disabled={updatingId === appId}
                              className="inline-flex items-center gap-1 px-3 py-1.5 bg-red-500 text-white text-sm font-medium rounded-lg hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                              {updatingId === appId ? (
                                <>
                                  <Loader2 className="w-4 h-4 animate-spin" />
                                  Đang xử lý...
                                </>
                              ) : (
                                <>
                                  <XCircle className="w-4 h-4" />
                                  Từ chối
                                </>
                              )}
                            </button>
                          </div>
                        )}

                        {appStatus === 'APPROVED' && (
                          <span className="text-sm text-neutral-500 italic">Đã duyệt</span>
                        )}

                        {appStatus === 'REJECTED' && (
                          <span className="text-sm text-neutral-500 italic">Đã từ chối</span>
                        )}
                      </td>
                    </tr>
                  );
                })
                )}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {renderPagination()}
        </div>
      )}
    </div>
  );
};

export default EmployerApplications;
