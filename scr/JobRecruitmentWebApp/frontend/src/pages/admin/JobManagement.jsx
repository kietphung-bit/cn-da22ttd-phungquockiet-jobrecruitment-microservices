import React, { useState, useEffect } from 'react';
import { Eye, Trash2, Loader2, Briefcase, Building2, Calendar, User, MapPin, DollarSign, EyeOff, Check, X, Search, Filter, Copy, FileSpreadsheet, FileText as FilePDF } from 'lucide-react';
import { toast } from 'react-toastify';
import { format } from 'date-fns';
import adminService from '../../services/admin.service';
import seekingPostService from '../../services/seekingPost.service';
import { copyToClipboard, exportToExcel, exportToPDF } from '../../utils/tableExport';

/**
 * JobManagement Component (Post-moderation Model)
 * Giao diện quản lý tin tuyển dụng và tin tìm việc vi phạm cho Admin
 * 
 * Tính năng:
 * - Hai tab: "Tin tuyển dụng" và "Tin tìm việc"
 * - Xem chi tiết tin tuyển dụng/tin tìm việc trong modal
 * - Xóa tin tuyển dụng/tin tìm việc (chỉ với các tin vi phạm)
 * - Không cần duyệt trước (Chính sách hậu duyệt)
 * 
 * API Integration:
 * - GET /api/v1/jobs - Lấy tất cả tin tuyển dụng
 * - GET /api/v1/seeking-posts - Lấy tất cả tin tìm việc
 * - DELETE /api/v1/admin/jobs/{id} - Xóa tin tuyển dụng vi phạm
 * - DELETE /api/v1/admin/seeking-posts/{id} - Xóa tin tìm việc vi phạm
 * 
 * Khả năng bảo mật:
 * - Chỉ truy cập được bởi ROLE_ADM
 * - Được bảo vệ bởi PrivateRoute trong AppRoutes
 */
const JobManagement = () => {
  const [activeTab, setActiveTab] = useState('jobs'); // 'jobs' hoặc 'seekingPosts'
  const [jobs, setJobs] = useState([]);
  const [seekingPosts, setSeekingPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState({});
  const [selectedItem, setSelectedItem] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);

  // Trạng thái tìm kiếm và lọc
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [filteredJobs, setFilteredJobs] = useState([]);
  const [filteredPosts, setFilteredPosts] = useState([]);

  // Phân trang cho tin tuyển dụng
  const [jobPagination, setJobPagination] = useState({
    totalPages: 0,
    totalElements: 0,
    currentPage: 0,
  });
  const [jobCurrentPage, setJobCurrentPage] = useState(0);

  // Phân trang cho tin tìm việc
  const [postPagination, setPostPagination] = useState({
    totalPages: 0,
    totalElements: 0,
    currentPage: 0,
  });
  const [postCurrentPage, setPostCurrentPage] = useState(0);

  const pageSize = 20;

  // Lấy dữ liệu khi tab hoặc trang thay đổi
  useEffect(() => {
    if (activeTab === 'jobs') {
      fetchJobs();
    } else {
      fetchSeekingPosts();
    }
  }, [activeTab, jobCurrentPage, postCurrentPage]);

  // Áp dụng tìm kiếm và lọc cho tin tuyển dụng
  useEffect(() => {
    if (!searchTerm.trim() && statusFilter === 'ALL') {
      setFilteredJobs(jobs);
      return;
    }

    let filtered = jobs;

    // Áp dụng bộ lọc tìm kiếm
    if (searchTerm.trim()) {
      const searchLower = searchTerm.toLowerCase();
      filtered = filtered.filter(job => {
        const title = (job.jobTitle || '').toLowerCase();
        const company = (job.companyName || '').toLowerCase();
        const location = (job.jobLocation || '').toLowerCase();
        const category = (job.jcName || '').toLowerCase();
        
        return title.includes(searchLower) || 
               company.includes(searchLower) ||
               location.includes(searchLower) ||
               category.includes(searchLower);
      });
    }

    // Áp dụng bộ lọc trạng thái
    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(job => job.jobStatus === statusFilter);
    }

    setFilteredJobs(filtered);
  }, [jobs, searchTerm, statusFilter]);

  // Áp dụng bộ lọc tìm kiếm cho tin tìm việc
  useEffect(() => {
    if (!searchTerm.trim()) {
      setFilteredPosts(seekingPosts);
      return;
    }

    const searchLower = searchTerm.toLowerCase();
    const filtered = seekingPosts.filter(post => {
      const title = (post.spTitle || '').toLowerCase();
      const candidateName = (post.candidateName || '').toLowerCase();
      const description = (post.spDescription || '').toLowerCase();
      
      return title.includes(searchLower) || 
             candidateName.includes(searchLower) ||
             description.includes(searchLower);
    });
    
    setFilteredPosts(filtered);
  }, [seekingPosts, searchTerm]);

  /**
   * Lấy tất cả tin tuyển dụng
   */
  const fetchJobs = async () => {
    try {
      setLoading(true);
      const params = {
        page: jobCurrentPage,
        size: pageSize,
        sort: 'createdAt,desc',
      };

      const response = await adminService.getJobsByStatus(params);
      const data = response.data?.data || response.data;

      if (data.content) {
        setJobs(data.content);
        setJobPagination({
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
   * Lấy tất cả tin tìm việc
   */
  const fetchSeekingPosts = async () => {
    try {
      setLoading(true);
      const params = {
        page: postCurrentPage,
        size: pageSize,
      };

      const response = await adminService.getAllSeekingPosts(params);
      const data = response.data?.data || response.data;

      if (data.content) {
        setSeekingPosts(data.content);
        setPostPagination({
          totalPages: data.totalPages,
          totalElements: data.totalElements,
          currentPage: data.number,
        });
      } else {
        setSeekingPosts(data || []);
      }
    } catch (error) {
      console.error('Error fetching seeking posts:', error);
      toast.error('Không thể tải danh sách tin tìm việc');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Xử lý thay đổi tab
   */
  const handleTabChange = (tab) => {
    setActiveTab(tab);
    setLoading(true);
  };

  /**
   * Xử lý xem chi tiết tin tuyển dụng/tin tìm việc
   */
  const handleViewDetail = (item) => {
    setSelectedItem(item);
    setShowDetailModal(true);
  };

  /**
   * Xử lý xuất tin tuyển dụng ra các định dạng khác nhau
   */
  const handleExportJobs = async (type) => {
    const columns = [
      { header: 'Tiêu đề', accessor: (row) => row.jobTitle || 'N/A' },
      { header: 'Công ty', accessor: (row) => row.companyName || 'N/A' },
      { header: 'Danh mục', accessor: (row) => row.jcName || 'N/A' },
      { header: 'Địa điểm', accessor: (row) => row.jobLocation || 'N/A' },
      { header: 'Lương', accessor: (row) => row.jobSalary || 'N/A' },
      { header: 'Ngày đăng', accessor: (row) => row.createdAt ? format(new Date(row.createdAt), 'dd/MM/yyyy') : 'N/A' },
      { header: 'Hạn nộp', accessor: (row) => row.applicationDeadline ? format(new Date(row.applicationDeadline), 'dd/MM/yyyy') : 'N/A' },
      { 
        header: 'Trạng thái', 
        accessor: (row) => {
          const statusMap = {
            PENDING: 'Chờ duyệt',
            WAIT: 'Đợi duyệt',
            ACTIVE: 'Đang hoạt động',
            CLOSED: 'Đã đóng',
            HIDDEN: 'Đã ẩn'
          };
          return statusMap[row.jobStatus] || row.jobStatus;
        }
      },
      { header: 'Số đơn', accessor: (row) => row.applicationCount || 0 }
    ];

    const dataToExport = filteredJobs.length > 0 ? filteredJobs : jobs;

    try {
      switch (type) {
        case 'copy':
          await copyToClipboard(dataToExport, columns);
          toast.success('Đã sao chép vào clipboard!');
          break;
        case 'excel':
          exportToExcel(dataToExport, columns, 'danh-sach-tin-tuyen-dung.csv');
          toast.success('Đã xuất file Excel!');
          break;
        case 'pdf':
          await exportToPDF(dataToExport, columns, 'danh-sach-tin-tuyen-dung.pdf', {
            title: 'Danh Sách Tin Tuyển Dụng'
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
   * Xử lý xuất tin tìm việc ra các định dạng khác nhau
   */
  const handleExportPosts = async (type) => {
    const columns = [
      { header: 'Tiêu đề', accessor: (row) => row.spTitle || 'N/A' },
      { header: 'Ứng viên', accessor: (row) => row.candidateName || 'N/A' },
      { header: 'Mô tả', accessor: (row) => (row.spDescription || '').substring(0, 100) },
      { header: 'Ngày đăng', accessor: (row) => row.createdAt ? format(new Date(row.createdAt), 'dd/MM/yyyy') : 'N/A' },
      { header: 'Trạng thái', accessor: (row) => row.spStatus === 'ACTIVE' ? 'Hoạt động' : 'Đã ẩn' }
    ];

    const dataToExport = filteredPosts.length > 0 ? filteredPosts : seekingPosts;

    try {
      switch (type) {
        case 'copy':
          await copyToClipboard(dataToExport, columns);
          toast.success('Đã sao chép vào clipboard!');
          break;
        case 'excel':
          exportToExcel(dataToExport, columns, 'danh-sach-tin-tim-viec.csv');
          toast.success('Đã xuất file Excel!');
          break;
        case 'pdf':
          await exportToPDF(dataToExport, columns, 'danh-sach-tin-tim-viec.pdf', {
            title: 'Danh Sách Tin Tìm Việc'
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
   * Xử lý chuyển đổi trạng thái tin tuyển dụng (ACTIVE ↔ HIDDEN)
   */
  const handleToggleJobStatus = async (jobId, currentStatus) => {
    const action = currentStatus === 'ACTIVE' ? 'ẩn' : 'hiện';
    
    if (!window.confirm(`Xác nhận ${action} tin tuyển dụng này?`)) {
      return;
    }

    try {
      setActionLoading({ ...actionLoading, [jobId]: true });
      await adminService.toggleJobStatus(jobId);
      toast.success(`Đã ${action} tin tuyển dụng thành công`);
      fetchJobs();
    } catch (error) {
      console.error('Error toggling job status:', error);
      toast.error(error.response?.data?.message || `Không thể ${action} tin tuyển dụng`);
    } finally {
      setActionLoading({ ...actionLoading, [jobId]: false });
    }
  };

  /**
   * Xử lý chuyển đổi trạng thái tin tìm việc (ACTIVE ↔ HIDDEN)
   */
  const handleToggleSeekingPostStatus = async (skPostId, currentStatus) => {
    const action = currentStatus === 'ACTIVE' ? 'ẩn' : 'hiện';
    
    if (!window.confirm(`Xác nhận ${action} tin tìm việc này?`)) {
      return;
    }

    try {
      setActionLoading({ ...actionLoading, [skPostId]: true });
      await adminService.toggleSeekingPostStatus(skPostId);
      toast.success(`Đã ${action} tin tìm việc thành công`);
      fetchSeekingPosts();
    } catch (error) {
      console.error('Error toggling seeking post status:', error);
      toast.error(error.response?.data?.message || `Không thể ${action} tin tìm việc`);
    } finally {
      setActionLoading({ ...actionLoading, [skPostId]: false });
    }
  };

  /**
   * Định dạng lương
   */
  const formatSalary = (salary) => {
    if (!salary) return 'Thỏa thuận';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(salary);
  };

  /**
   * Định dạng ngày
   */
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
  };

  /**
   * Phân tích kỹ năng
   */
  const parseSkills = (skillsData) => {
    if (!skillsData) return [];
    if (Array.isArray(skillsData)) return skillsData;
    
    try {
      // Thử phân tích dưới dạng JSON
      const parsed = JSON.parse(skillsData);
      if (Array.isArray(parsed)) return parsed;
    } catch (e) {
      // Nếu phân tích JSON thất bại, thử tách CSV
      if (typeof skillsData === 'string') {
        return skillsData.split(',').map(s => s.trim()).filter(Boolean);
      }
    }
    
    return [];
  };

  /**
   * Lấy màu badge trạng thái
   */
  const getStatusBadge = (status) => {
    const statusMap = {
      'ACTIVE': { label: 'Đang hiển thị', className: 'bg-green-100 text-green-800' },
      'HIDDEN': { label: 'Đã ẩn', className: 'bg-neutral-100 text-neutral-800' },
      'EXPIRED': { label: 'Hết hạn', className: 'bg-red-100 text-red-800' },
      'PENDING': { label: 'Chờ duyệt', className: 'bg-yellow-100 text-yellow-800' },
      'REJECTED': { label: 'Bị từ chối', className: 'bg-red-100 text-red-800' },
    };
    
    const statusInfo = statusMap[status] || { label: status, className: 'bg-neutral-100 text-neutral-800' };
    
    return (
      <span className={`px-3 py-1 rounded-full text-sm font-medium ${statusInfo.className}`}>
        {statusInfo.label}
      </span>
    );
  };

  return (
    <div className="min-h-screen bg-neutral-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-neutral-900 mb-2">
            Quản lý tin đăng
          </h1>
          <p className="text-neutral-600">
            Quản lý tin tuyển dụng và tin tìm việc, xử lý vi phạm
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
                  placeholder={activeTab === 'jobs' ? 'Tìm kiếm theo tiêu đề, công ty, địa điểm...' : 'Tìm kiếm theo tiêu đề, ứng viên...'}
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
                />
              </div>
            </div>

            {/* Status Filter (Jobs only) */}
            {activeTab === 'jobs' && (
              <div className="w-full lg:w-48">
                <div className="relative">
                  <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 text-neutral-400 w-5 h-5" />
                  <select
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                    className="w-full pl-10 pr-4 py-2.5 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent appearance-none bg-white"
                  >
                    <option value="ALL">Tất cả trạng thái</option>
                    <option value="ACTIVE">Đang hoạt động</option>
                    <option value="PENDING">Chờ duyệt</option>
                    <option value="WAIT">Đợi duyệt</option>
                    <option value="CLOSED">Đã đóng</option>
                    <option value="HIDDEN">Đã ẩn</option>
                  </select>
                </div>
              </div>
            )}

            {/* Export Buttons */}
            <div className="flex gap-2">
              <button
                onClick={() => activeTab === 'jobs' ? handleExportJobs('copy') : handleExportPosts('copy')}
                className="inline-flex items-center gap-2 px-4 py-2.5 text-sm font-medium text-neutral-700 bg-white border border-neutral-300 rounded-lg hover:bg-neutral-50 transition-colors"
                title="Sao chép vào clipboard"
              >
                <Copy className="w-4 h-4" />
                <span className="hidden sm:inline">Copy</span>
              </button>
              
              <button
                onClick={() => activeTab === 'jobs' ? handleExportJobs('excel') : handleExportPosts('excel')}
                className="inline-flex items-center gap-2 px-4 py-2.5 text-sm font-medium text-white bg-green-600 rounded-lg hover:bg-green-700 transition-colors"
                title="Xuất ra Excel"
              >
                <FileSpreadsheet className="w-4 h-4" />
                <span className="hidden sm:inline">Excel</span>
              </button>
              
              <button
                onClick={() => activeTab === 'jobs' ? handleExportJobs('pdf') : handleExportPosts('pdf')}
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
              Tìm thấy {activeTab === 'jobs' ? filteredJobs.length : filteredPosts.length} kết quả cho "{searchTerm}"
              {activeTab === 'jobs' && statusFilter !== 'ALL' && ` (Lọc: ${statusFilter})`}
            </div>
          )}
        </div>

        {/* Tabs */}
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200 mb-6">
          <div className="flex border-b border-neutral-200">
            <button
              onClick={() => handleTabChange('jobs')}
              className={`flex-1 px-6 py-4 text-center font-medium transition-colors ${
                activeTab === 'jobs'
                  ? 'text-primary border-b-2 border-primary bg-blue-50'
                  : 'text-neutral-600 hover:text-neutral-900 hover:bg-neutral-50'
              }`}
            >
              <Briefcase className="w-5 h-5 inline mr-2" />
              Tin tuyển dụng
              {activeTab === 'jobs' && jobs.length > 0 && (
                <span className="ml-2 px-2 py-1 bg-primary text-white text-xs rounded-full">
                  {jobPagination.totalElements || jobs.length}
                </span>
              )}
            </button>
            <button
              onClick={() => handleTabChange('seekingPosts')}
              className={`flex-1 px-6 py-4 text-center font-medium transition-colors ${
                activeTab === 'seekingPosts'
                  ? 'text-primary border-b-2 border-primary bg-blue-50'
                  : 'text-neutral-600 hover:text-neutral-900 hover:bg-neutral-50'
              }`}
            >
              <User className="w-5 h-5 inline mr-2" />
              Tin tìm việc
              {activeTab === 'seekingPosts' && seekingPosts.length > 0 && (
                <span className="ml-2 px-2 py-1 bg-primary text-white text-xs rounded-full">
                  {postPagination.totalElements || seekingPosts.length}
                </span>
              )}
            </button>
          </div>
        </div>

        {/* Content */}
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200">
          {loading ? (
            <div className="flex justify-center items-center py-20">
              <Loader2 className="w-12 h-12 text-primary animate-spin" />
            </div>
          ) : (
            <>
              {/* Jobs Tab */}
              {activeTab === 'jobs' && (
                <div className="overflow-x-auto">
                  {filteredJobs.length === 0 ? (
                    <div className="text-center py-20">
                      <Search className="w-16 h-16 text-neutral-300 mx-auto mb-4" />
                      <p className="text-neutral-600 font-medium">
                        {searchTerm || statusFilter !== 'ALL' ? 'Không tìm thấy tin tuyển dụng nào' : 'Không có tin tuyển dụng nào'}
                      </p>
                      {searchTerm || statusFilter !== 'ALL' ? (
                        <p className="text-sm text-neutral-500 mt-1">
                          Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc
                        </p>
                      ) : null}
                    </div>
                  ) : (
                    <table className="w-full">\n                      <thead className="bg-neutral-50 border-b border-neutral-200">
                        <tr>
                          <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-900">
                            Tin tuyển dụng
                          </th>
                          <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-900">
                            Mức lương
                          </th>
                          <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-900">
                            Trạng thái
                          </th>
                          <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-900">
                            Ngày đăng
                          </th>
                          <th className="px-6 py-4 text-center text-sm font-semibold text-neutral-900">
                            Thao tác
                          </th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-neutral-200">
                        {filteredJobs.map((job) => (
                          <tr key={job.jobId} className="hover:bg-neutral-50 transition-colors">
                            <td className="px-6 py-4">
                              <div className="flex items-center gap-3">
                                <Briefcase className="w-5 h-5 text-primary flex-shrink-0" />
                                <div>
                                  <p className="font-semibold text-neutral-900">
                                    {job.jobTitle}
                                  </p>
                                  <p className="text-sm text-neutral-600">
                                    #{job.jobCode}
                                  </p>
                                </div>
                              </div>
                            </td>
                            <td className="px-6 py-4">
                              <div className="flex items-center gap-2">
                                <span className="text-neutral-700 font-medium">
                                  {formatSalary(job.jobSalary)}
                                </span>
                              </div>
                            </td>
                            <td className="px-6 py-4">
                              {getStatusBadge(job.jobStatus)}
                            </td>
                            <td className="px-6 py-4">
                              <div className="flex items-center gap-2 text-neutral-600">
                                <Calendar className="w-4 h-4" />
                                <span className="text-sm">{formatDate(job.createdAt)}</span>
                              </div>
                            </td>
                            <td className="px-6 py-4">
                              <div className="flex items-center justify-center gap-2">
                                <button
                                  onClick={() => handleViewDetail(job)}
                                  className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                  title="Xem chi tiết"
                                >
                                  <Eye className="w-5 h-5" />
                                </button>
                                <button
                                  onClick={() => handleToggleJobStatus(job.jobId, job.jobStatus)}
                                  disabled={actionLoading[job.jobId]}
                                  className={`p-2 rounded-lg transition-colors disabled:opacity-50 ${
                                    job.jobStatus === 'ACTIVE'
                                      ? 'text-yellow-600 hover:bg-yellow-50'
                                      : 'text-green-600 hover:bg-green-50'
                                  }`}
                                  title={job.jobStatus === 'ACTIVE' ? 'Ẩn tin' : 'Hiện tin'}
                                >
                                  {actionLoading[job.jobId] ? (
                                    <Loader2 className="w-5 h-5 animate-spin" />
                                  ) : job.jobStatus === 'ACTIVE' ? (
                                    <X className="w-5 h-5" />
                                  ) : (
                                    <Check className="w-5 h-5" />
                                  )}
                                </button>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )}

                  {/* Pagination for Jobs */}
                  {jobs.length > 0 && jobPagination.totalPages > 1 && (
                    <div className="px-6 py-4 border-t border-neutral-200 flex items-center justify-between">
                      <p className="text-sm text-neutral-600">
                        Hiển thị {jobs.length} / {jobPagination.totalElements} tin
                      </p>
                      <div className="flex gap-2">
                        <button
                          onClick={() => setJobCurrentPage(prev => Math.max(0, prev - 1))}
                          disabled={jobCurrentPage === 0}
                          className="px-4 py-2 border border-neutral-300 rounded-lg text-neutral-700 hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          Trước
                        </button>
                        <span className="px-4 py-2 border border-neutral-300 rounded-lg text-neutral-700">
                          Trang {jobCurrentPage + 1} / {jobPagination.totalPages}
                        </span>
                        <button
                          onClick={() => setJobCurrentPage(prev => Math.min(jobPagination.totalPages - 1, prev + 1))}
                          disabled={jobCurrentPage >= jobPagination.totalPages - 1}
                          className="px-4 py-2 border border-neutral-300 rounded-lg text-neutral-700 hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          Sau
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {/* Seeking Posts Tab */}
              {activeTab === 'seekingPosts' && (
                <div className="overflow-x-auto">
                  {filteredPosts.length === 0 ? (
                    <div className="text-center py-20">
                      <Search className="w-16 h-16 text-neutral-300 mx-auto mb-4" />
                      <p className="text-neutral-600 font-medium">
                        {searchTerm ? 'Không tìm thấy tin tìm việc nào' : 'Không có tin tìm việc nào'}
                      </p>
                      {searchTerm ? (
                        <p className="text-sm text-neutral-500 mt-1">
                          Thử thay đổi từ khóa tìm kiếm
                        </p>
                      ) : null}
                    </div>
                  ) : (
                    <table className="w-full">
                      <thead className="bg-neutral-50 border-b border-neutral-200">
                        <tr>
                          <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-900">
                            Tin tìm việc
                          </th>
                          <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-900">
                            Ứng viên
                          </th>
                          <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-900">
                            Địa điểm
                          </th>
                          <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-900">
                            Mức lương mong muốn
                          </th>
                          <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-900">
                            Trạng thái
                          </th>
                          <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-900">
                            Ngày đăng
                          </th>
                          <th className="px-6 py-4 text-center text-sm font-semibold text-neutral-900">
                            Thao tác
                          </th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-neutral-200">
                        {filteredPosts.map((post) => {
                          const skills = parseSkills(post.skPostSkills);
                          
                          return (
                            <tr key={post.skPostId} className="hover:bg-neutral-50 transition-colors">
                              <td className="px-6 py-4">
                                <div className="flex items-center gap-3">
                                  <User className="w-5 h-5 text-primary flex-shrink-0" />
                                  <div>
                                    <p className="font-semibold text-neutral-900">
                                      {post.skPostTitle}
                                    </p>
                                    <p className="text-sm text-neutral-600">
                                      #{post.skPostCode}
                                    </p>
                                    {skills.length > 0 && (
                                      <div className="flex gap-1 mt-1">
                                        {skills.slice(0, 3).map((skill, idx) => (
                                          <span
                                            key={idx}
                                            className="px-2 py-0.5 bg-blue-50 text-blue-700 text-xs rounded"
                                          >
                                            {skill}
                                          </span>
                                        ))}
                                        {skills.length > 3 && (
                                          <span className="px-2 py-0.5 bg-neutral-100 text-neutral-600 text-xs rounded">
                                            +{skills.length - 3}
                                          </span>
                                        )}
                                      </div>
                                    )}
                                  </div>
                                </div>
                              </td>
                              <td className="px-6 py-4">
                                <span className="text-neutral-700">
                                  {post.candidateName || 'N/A'}
                                </span>
                              </td>
                              <td className="px-6 py-4">
                                <div className="flex items-center gap-2">
                                  <MapPin className="w-4 h-4 text-neutral-400" />
                                  <span className="text-neutral-700">
                                    {post.desiredLocation || 'N/A'}
                                  </span>
                                </div>
                              </td>
                              <td className="px-6 py-4">
                                <div className="flex items-center gap-2">
                                  <DollarSign className="w-4 h-4 text-neutral-400" />
                                  <span className="text-neutral-700 font-medium">
                                    {formatSalary(post.desiredSalary)}
                                  </span>
                                </div>
                              </td>
                              <td className="px-6 py-4">
                                {getStatusBadge(post.status)}
                              </td>
                              <td className="px-6 py-4">
                                <div className="flex items-center gap-2 text-neutral-600">
                                  <Calendar className="w-4 h-4" />
                                  <span className="text-sm">{formatDate(post.createdAt)}</span>
                                </div>
                              </td>
                              <td className="px-6 py-4">
                                <div className="flex items-center justify-center gap-2">
                                  <button
                                    onClick={() => handleViewDetail(post)}
                                    className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                    title="Xem chi tiết"
                                  >
                                    <Eye className="w-5 h-5" />
                                  </button>
                                  <button
                                    onClick={() => handleToggleSeekingPostStatus(post.id, post.status)}
                                    disabled={actionLoading[post.id]}
                                    className={`p-2 rounded-lg transition-colors disabled:opacity-50 ${
                                      post.status === 'ACTIVE'
                                        ? 'text-yellow-600 hover:bg-yellow-50'
                                        : 'text-green-600 hover:bg-green-50'
                                    }`}
                                    title={post.status === 'ACTIVE' ? 'Ẩn tin' : 'Hiện tin'}
                                  >
                                    {actionLoading[post.id] ? (
                                      <Loader2 className="w-5 h-5 animate-spin" />
                                    ) : post.status === 'ACTIVE' ? (
                                      <X className="w-5 h-5" />
                                    ) : (
                                      <Check className="w-5 h-5" />
                                    )}
                                  </button>
                                </div>
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  )}

                  {/* Pagination for Seeking Posts */}
                  {seekingPosts.length > 0 && postPagination.totalPages > 1 && (
                    <div className="px-6 py-4 border-t border-neutral-200 flex items-center justify-between">
                      <p className="text-sm text-neutral-600">
                        Hiển thị {seekingPosts.length} / {postPagination.totalElements} tin
                      </p>
                      <div className="flex gap-2">
                        <button
                          onClick={() => setPostCurrentPage(prev => Math.max(0, prev - 1))}
                          disabled={postCurrentPage === 0}
                          className="px-4 py-2 border border-neutral-300 rounded-lg text-neutral-700 hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          Trước
                        </button>
                        <span className="px-4 py-2 border border-neutral-300 rounded-lg text-neutral-700">
                          Trang {postCurrentPage + 1} / {postPagination.totalPages}
                        </span>
                        <button
                          onClick={() => setPostCurrentPage(prev => Math.min(postPagination.totalPages - 1, prev + 1))}
                          disabled={postCurrentPage >= postPagination.totalPages - 1}
                          className="px-4 py-2 border border-neutral-300 rounded-lg text-neutral-700 hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          Sau
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              )}
            </>
          )}
        </div>

        {/* Detail Modal */}
        {showDetailModal && selectedItem && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg shadow-xl max-w-3xl w-full max-h-[90vh] overflow-y-auto">
              <div className="p-6 border-b border-neutral-200">
                <h2 className="text-2xl font-bold text-neutral-900">
                  {activeTab === 'jobs' ? 'Chi tiết tin tuyển dụng' : 'Chi tiết tin tìm việc'}
                </h2>
              </div>

              <div className="p-6 space-y-6">
                {activeTab === 'jobs' ? (
                  // Job Details
                  <>
                    <div>
                      <h3 className="font-semibold text-neutral-900 mb-2">Tiêu đề</h3>
                      <p className="text-neutral-700">{selectedItem.jobTitle}</p>
                    </div>
                    <div>
                      <h3 className="font-semibold text-neutral-900 mb-2">Mã tin</h3>
                      <p className="text-neutral-700">#{selectedItem.jobCode}</p>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <h3 className="font-semibold text-neutral-900 mb-2">Mức lương</h3>
                        <p className="text-neutral-700">{formatSalary(selectedItem.jobSalary)}</p>
                      </div>
                      <div>
                        <h3 className="font-semibold text-neutral-900 mb-2">Địa điểm</h3>
                        <p className="text-neutral-700">{selectedItem.jobLocation}</p>
                      </div>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <h3 className="font-semibold text-neutral-900 mb-2">Ngày bắt đầu</h3>
                        <p className="text-neutral-700">{formatDate(selectedItem.startDate)}</p>
                      </div>
                      <div>
                        <h3 className="font-semibold text-neutral-900 mb-2">Ngày kết thúc</h3>
                        <p className="text-neutral-700">{formatDate(selectedItem.endDate)}</p>
                      </div>
                    </div>
                    <div>
                      <h3 className="font-semibold text-neutral-900 mb-2">Số lượng tuyển</h3>
                      <p className="text-neutral-700">{selectedItem.maxCandidates} người</p>
                    </div>
                    <div>
                      <h3 className="font-semibold text-neutral-900 mb-2">Mô tả công việc</h3>
                      <div 
                        className="prose max-w-none text-neutral-700"
                        dangerouslySetInnerHTML={{ __html: selectedItem.jobDescription }}
                      />
                    </div>
                    <div>
                      <h3 className="font-semibold text-neutral-900 mb-2">Yêu cầu công việc</h3>
                      <div 
                        className="prose max-w-none text-neutral-700"
                        dangerouslySetInnerHTML={{ __html: selectedItem.jobRequirement }}
                      />
                    </div>
                  </>
                ) : (
                  // Seeking Post Details
                  <>
                    <div>
                      <h3 className="font-semibold text-neutral-900 mb-2">Tiêu đề</h3>
                      <p className="text-neutral-700">{selectedItem.skPostTitle}</p>
                    </div>
                    <div>
                      <h3 className="font-semibold text-neutral-900 mb-2">Mã tin</h3>
                      <p className="text-neutral-700">#{selectedItem.skPostCode}</p>
                    </div>
                    <div>
                      <h3 className="font-semibold text-neutral-900 mb-2">Ứng viên</h3>
                      <p className="text-neutral-700">{selectedItem.candidateName}</p>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <h3 className="font-semibold text-neutral-900 mb-2">Địa điểm mong muốn</h3>
                        <p className="text-neutral-700">{selectedItem.desiredLocation || 'N/A'}</p>
                      </div>
                      <div>
                        <h3 className="font-semibold text-neutral-900 mb-2">Mức lương mong muốn</h3>
                        <p className="text-neutral-700">{formatSalary(selectedItem.desiredSalary)}</p>
                      </div>
                    </div>
                    <div>
                      <h3 className="font-semibold text-neutral-900 mb-2">Kỹ năng</h3>
                      <div className="flex flex-wrap gap-2">
                        {parseSkills(selectedItem.skPostSkills).map((skill, idx) => (
                          <span
                            key={idx}
                            className="px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm font-medium"
                          >
                            {skill}
                          </span>
                        ))}
                      </div>
                    </div>
                    <div>
                      <h3 className="font-semibold text-neutral-900 mb-2">Giới thiệu</h3>
                      <p className="text-neutral-700 whitespace-pre-wrap">
                        {selectedItem.skPostIntro || 'Không có thông tin'}
                      </p>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <h3 className="font-semibold text-neutral-900 mb-2">Ngày đăng</h3>
                        <p className="text-neutral-700">{formatDate(selectedItem.createdAt)}</p>
                      </div>
                      <div>
                        <h3 className="font-semibold text-neutral-900 mb-2">Ngày hết hạn</h3>
                        <p className="text-neutral-700">{formatDate(selectedItem.expiryDate)}</p>
                      </div>
                    </div>
                  </>
                )}
              </div>

              <div className="p-6 border-t border-neutral-200 flex justify-end">
                <button
                  onClick={() => setShowDetailModal(false)}
                  className="px-6 py-3 bg-neutral-200 text-neutral-700 rounded-lg hover:bg-neutral-300 transition-colors font-medium"
                >
                  Đóng
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default JobManagement;
