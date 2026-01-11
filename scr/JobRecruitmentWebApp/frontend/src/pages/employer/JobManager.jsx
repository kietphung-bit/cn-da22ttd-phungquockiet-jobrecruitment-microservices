import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Pencil, Eye, EyeOff, Loader2, Calendar, MapPin, DollarSign, Users, Search, Filter, Copy, FileSpreadsheet, FileText } from 'lucide-react';
import { toast } from 'react-toastify';
import jobService from '../../services/job.service';
import { format } from 'date-fns';
import { copyToClipboard, exportToExcel, exportToPDF } from '../../utils/tableExport';

/**
 * JobManager Component
 * Xem và quản lý các tin tuyển dụng của nhà tuyển dụng
 * 
 * Tính năng:
 * - Hiển thị các tin tuyển dụng dưới dạng bảng
 * - Tìm kiếm theo tiêu đề công việc
 * - Lọc theo trạng thái và danh mục
 * - Xuất dữ liệu sang Copy/Excel/PDF
 * - Phân trang
 * - Hành động nhanh: Chỉnh sửa, Ẩn/Hiện
 * - Nhãn trạng thái với nhãn tiếng Việt
 * - Số lượng ứng viên cho mỗi tin tuyển dụng
 * - Điều hướng đến các biểu mẫu tạo/chỉnh sửa
 * 
 * Ánh xạ trạng thái (Tiếng Việt):
 * - PENDING: "Chờ xét duyệt" (Vàng)
 * - WAIT: "Chưa mở" (Xám)
 * - ACTIVE: "Đang mở" (Xanh lá)
 * - CLOSED: "Đã đóng" (Đỏ)
 * - HIDDEN: "Tạm ẩn" (Xám đậm)
 */
const JobManager = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [jobs, setJobs] = useState([]);
  const [filteredJobs, setFilteredJobs] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [pagination, setPagination] = useState({
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    pageSize: 10,
  });

  useEffect(() => {
    fetchJobs(0);
  }, []);

  useEffect(() => {
    // Áp dụng tìm kiếm và lọc mỗi khi jobs, searchTerm hoặc statusFilter thay đổi
    applyFilters();
  }, [jobs, searchTerm, statusFilter]);

  const applyFilters = () => {
    let filtered = [...jobs];

    // Áp dụng tìm kiếm
    if (searchTerm.trim()) {
      const term = searchTerm.toLowerCase();
      filtered = filtered.filter(job => 
        job.jobTitle?.toLowerCase().includes(term) ||
        job.jobLocation?.toLowerCase().includes(term) ||
        job.jcName?.toLowerCase().includes(term)
      );
    }

    // Áp dụng lọc trạng thái
    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(job => job.jobStatus === statusFilter);
    }

    setFilteredJobs(filtered);
  };

  const fetchJobs = async (page = 0) => {
    try {
      setLoading(true);
      const response = await jobService.getMyJobs({
        page: page,
        size: pagination.pageSize,
        sort: 'createdAt,desc',
      });

      setJobs(response.data.content || []);
      setPagination({
        currentPage: response.data.number || 0,
        totalPages: response.data.totalPages || 0,
        totalElements: response.data.totalElements || 0,
        pageSize: response.data.size || 10,
      });
    } catch (error) {
      console.error('Error fetching jobs:', error);
      toast.error('Không thể tải danh sách tin tuyển dụng');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Chuyển đổi trạng thái hiển thị của tin tuyển dụng (ACTIVE <-> HIDDEN)
   * HIDDEN hoạt động như xóa mềm - tin không bị xóa khỏi cơ sở dữ liệu
   */
  const handleToggleStatus = async (jobId, currentStatus) => {
    const newStatus = currentStatus === 'HIDDEN' ? 'ACTIVE' : 'HIDDEN';
    const action = newStatus === 'HIDDEN' ? 'ẩn' : 'hiển thị lại';
    
    const confirmMessage = newStatus === 'HIDDEN'
      ? 'Bạn có chắc chắn muốn ẩn tin tuyển dụng này?\n\nTin sẽ không hiển thị với ứng viên nhưng vẫn có thể khôi phục.'
      : 'Bạn có muốn hiển thị lại tin tuyển dụng này?';
    
    if (!window.confirm(confirmMessage)) return;

    try {
      await jobService.updateJobStatus(jobId, newStatus);
      toast.success(`Đã ${action} tin tuyển dụng`);
      await fetchJobs(pagination.currentPage);
    } catch (error) {
      console.error('Error toggling job status:', error);
      toast.error(`Không thể ${action} tin tuyển dụng`);
    }
  };

  const getStatusBadge = (status) => {
    const statusMap = {
      PENDING: { label: 'Chờ xét duyệt', color: 'bg-yellow-100 text-yellow-800' },
      WAIT: { label: 'Chưa mở', color: 'bg-gray-100 text-gray-800' },
      ACTIVE: { label: 'Đang mở', color: 'bg-green-100 text-green-800' },
      CLOSED: { label: 'Đã đóng', color: 'bg-red-100 text-red-800' },
      HIDDEN: { label: 'Tạm ẩn', color: 'bg-neutral-700 text-white' },
    };

    const { label, color } = statusMap[status] || { label: status, color: 'bg-gray-100 text-gray-800' };

    return (
      <span className={`px-3 py-1 rounded-full text-xs font-semibold ${color}`}>
        {label}
      </span>
    );
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(amount);
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pagination.totalPages) {
      fetchJobs(newPage);
    }
  };

  const handleExport = async (type) => {
    const columns = [
      { header: 'Tiêu đề', accessor: (row) => row.jobTitle },
      { header: 'Danh mục', accessor: (row) => row.jcName || 'Chưa phân loại' },
      { header: 'Địa điểm', accessor: (row) => row.jobLocation },
      { header: 'Lương', accessor: (row) => formatCurrency(row.jobSalary) },
      { header: 'Ngày bắt đầu', accessor: (row) => row.startDate ? format(new Date(row.startDate), 'dd/MM/yyyy') : 'N/A' },
      { header: 'Ngày kết thúc', accessor: (row) => row.endDate ? format(new Date(row.endDate), 'dd/MM/yyyy') : 'N/A' },
      { header: 'Trạng thái', accessor: (row) => {
        const statusMap = {
          PENDING: 'Chờ xét duyệt',
          WAIT: 'Chưa mở',
          ACTIVE: 'Đang mở',
          CLOSED: 'Đã đóng',
          HIDDEN: 'Tạm ẩn'
        };
        return statusMap[row.jobStatus] || row.jobStatus;
      }},
      { header: 'Số đơn', accessor: (row) => row.applicationCount || 0 }
    ];

    const dataToExport = filteredJobs.length > 0 ? filteredJobs : jobs;

    try {
      let success = false;
      switch (type) {
        case 'copy':
          success = copyToClipboard(dataToExport, columns);
          if (success) toast.success('Đã sao chép vào clipboard');
          break;
        case 'excel':
          success = exportToExcel(dataToExport, columns, 'danh-sach-tuyen-dung');
          if (success) toast.success('Đã xuất file Excel');
          break;
        case 'pdf':
          success = await exportToPDF(dataToExport, columns, 'danh-sach-tuyen-dung', {
            title: 'Danh sách tin tuyển dụng',
            orientation: 'landscape'
          });
          if (success) toast.success('Đã xuất file PDF');
          break;
      }
      
      if (!success) {
        toast.error('Không thể xuất dữ liệu');
      }
    } catch (error) {
      console.error('Export error:', error);
      toast.error('Đã xảy ra lỗi khi xuất dữ liệu');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-neutral-50 flex items-center justify-center">
        <div className="text-center">
          <Loader2 className="w-12 h-12 text-primary animate-spin mx-auto mb-4" />
          <p className="text-neutral-600">Đang tải danh sách tin tuyển dụng...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-neutral-50">
      {/* Header */}
      <div className="bg-white border-b border-neutral-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-neutral-900">Quản lý tin tuyển dụng</h1>
              <p className="text-neutral-600 mt-1">
                Tổng số: {pagination.totalElements} tin tuyển dụng
              </p>
            </div>
            <button
              onClick={() => navigate('/employer/jobs/create')}
              className="flex items-center gap-2 px-6 py-3 bg-primary text-white rounded-lg hover:bg-primary-600 transition-colors font-semibold shadow-md hover:shadow-lg"
            >
              <Plus className="w-5 h-5" />
              Đăng tin mới
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Search, Filter and Export Bar */}
        <div className="bg-white rounded-lg shadow-sm p-4 mb-6 border border-neutral-200">
          <div className="flex flex-col lg:flex-row gap-4">
            {/* Search Input */}
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-neutral-400 w-5 h-5" />
                <input
                  type="text"
                  placeholder="Tìm kiếm theo tiêu đề, địa điểm, danh mục..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-primary"
                />
              </div>
            </div>

            {/* Status Filter */}
            <div className="lg:w-48">
              <div className="relative">
                <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 text-neutral-400 w-5 h-5" />
                <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-primary appearance-none bg-white"
                >
                  <option value="ALL">Tất cả trạng thái</option>
                  <option value="ACTIVE">Đang mở</option>
                  <option value="PENDING">Chờ xét duyệt</option>
                  <option value="WAIT">Chưa mở</option>
                  <option value="CLOSED">Đã đóng</option>
                  <option value="HIDDEN">Tạm ẩn</option>
                </select>
              </div>
            </div>

            {/* Export Buttons */}
            <div className="flex gap-2">
              <button
                onClick={() => handleExport('copy')}
                className="flex items-center gap-2 px-4 py-2.5 border border-neutral-300 text-neutral-700 rounded-lg hover:bg-neutral-50 transition-colors"
                title="Sao chép"
              >
                <Copy className="w-4 h-4" />
                <span className="hidden sm:inline">Copy</span>
              </button>
              <button
                onClick={() => handleExport('excel')}
                className="flex items-center gap-2 px-4 py-2.5 border border-neutral-300 text-white rounded-lg bg-green-600 hover:bg-green-700 transition-colors"
                title="Xuất Excel"
              >
                <FileSpreadsheet className="w-4 h-4" />
                <span className="hidden sm:inline">Excel</span>
              </button>
              <button
                onClick={() => handleExport('pdf')}
                className="flex items-center gap-2 px-4 py-2.5 border border-neutral-300 text-white rounded-lg bg-red-600 hover:bg-red-700 transition-colors"
                title="Xuất PDF"
              >
                <FileText className="w-4 h-4" />
                <span className="hidden sm:inline">PDF</span>
              </button>
            </div>
          </div>

          {/* Results Info */}
          {(searchTerm || statusFilter !== 'ALL') && (
            <div className="mt-3 pt-3 border-t border-neutral-200">
              <p className="text-sm text-neutral-600">
                Tìm thấy <span className="font-semibold text-primary">{filteredJobs.length}</span> kết quả
                {searchTerm && ` cho "${searchTerm}"`}
                {statusFilter !== 'ALL' && ` với trạng thái "${statusFilter}"`}
              </p>
            </div>
          )}
        </div>

        {jobs.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <div className="text-6xl mb-4">📋</div>
            <h3 className="text-xl font-bold text-neutral-900 mb-2">
              Chưa có tin tuyển dụng nào
            </h3>
            <p className="text-neutral-600 mb-6">
              Bắt đầu đăng tin tuyển dụng đầu tiên của bạn
            </p>
            <button
              onClick={() => navigate('/employer/jobs/create')}
              className="inline-flex items-center gap-2 px-6 py-3 bg-primary text-white rounded-lg hover:bg-primary-600 transition-colors font-semibold"
            >
              <Plus className="w-5 h-5" />
              Đăng tin mới
            </button>
          </div>
        ) : (
          <>
            {/* Jobs Table */}
            <div className="bg-white rounded-lg shadow-sm overflow-hidden border border-neutral-200">
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-neutral-50 border-b border-neutral-200">
                    <tr>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">
                        Tiêu đề công việc
                      </th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">
                        Danh mục
                      </th>
                      <th className="px-6 py-4 text-center text-sm font-semibold text-neutral-700">
                        Ngày đăng
                      </th>
                      <th className="px-6 py-4 text-center text-sm font-semibold text-neutral-700">
                        Ngày hết hạn
                      </th>
                      <th className="px-6 py-4 text-center text-sm font-semibold text-neutral-700">
                        Trạng thái
                      </th>
                      <th className="px-6 py-4 text-center text-sm font-semibold text-neutral-700">
                        Hành động
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-neutral-200">
                    {filteredJobs.length === 0 ? (
                      <tr>
                        <td colSpan="6" className="px-6 py-12 text-center">
                          <div className="text-neutral-400">
                            <Search className="w-12 h-12 mx-auto mb-3 opacity-50" />
                            <p className="text-lg font-medium">Không tìm thấy kết quả</p>
                            <p className="text-sm mt-1">Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm</p>
                          </div>
                        </td>
                      </tr>
                    ) : (
                      filteredJobs.map((job) => (
                      <tr key={job.jobId} className="hover:bg-neutral-50 transition-colors">
                        <td className="px-6 py-4">
                          <div>
                            <div className="font-semibold text-neutral-900">{job.jobTitle}</div>
                            <div className="flex items-center gap-4 mt-1 text-xs text-neutral-500">
                              <span className="flex items-center gap-1">
                                <MapPin className="w-3 h-3" />
                                {job.jobLocation}
                              </span>
                              <span className="flex items-center gap-1">
                                <DollarSign className="w-3 h-3" />
                                {formatCurrency(job.jobSalary)}
                              </span>
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4 text-sm text-neutral-700">
                          <span className="inline-flex items-center px-2.5 py-1 bg-indigo-50 text-indigo-700 rounded-full text-xs font-medium">
                            {job.jcName || 'Chưa phân loại'}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-center text-sm text-neutral-700">
                          <div className="flex items-center justify-center gap-1">
                            <Calendar className="w-4 h-4 text-neutral-400" />
                            {job.startDate ? format(new Date(job.startDate), 'dd/MM/yyyy') : 'N/A'}
                          </div>
                        </td>
                        <td className="px-6 py-4 text-center text-sm text-neutral-700">
                          <div className="flex items-center justify-center gap-1">
                            <Calendar className="w-4 h-4 text-neutral-400" />
                            {job.endDate ? format(new Date(job.endDate), 'dd/MM/yyyy') : 'N/A'}
                          </div>
                        </td>
                        <td className="px-6 py-4 text-center">
                          {getStatusBadge(job.jobStatus)}
                        </td>
                        <td className="px-6 py-4">
                          <div className="flex items-center justify-center gap-2">
                            {/* Edit Button */}
                            <button
                              onClick={() => navigate(`/employer/jobs/edit/${job.jobId}`)}
                              className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                              title="Chỉnh sửa"
                            >
                              <Pencil className="w-4 h-4" />
                            </button>

                            {/* Toggle Visibility Button (Hide = Soft Delete) */}
                            <button
                              onClick={() => handleToggleStatus(job.jobId, job.jobStatus)}
                              className={`p-2 rounded-lg transition-colors ${
                                job.jobStatus === 'HIDDEN'
                                  ? 'text-green-600 hover:bg-green-50'
                                  : 'text-amber-600 hover:bg-amber-50'
                              }`}
                              title={job.jobStatus === 'HIDDEN' ? 'Hiển thị lại' : 'Ẩn tin (Xóa mềm)'}
                            >
                              {job.jobStatus === 'HIDDEN' ? (
                                <Eye className="w-4 h-4" />
                              ) : (
                                <EyeOff className="w-4 h-4" />
                              )}
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Pagination */}
            {pagination.totalPages > 1 && (
              <div className="mt-6 flex items-center justify-between">
                <p className="text-sm text-neutral-600">
                  Hiển thị {pagination.currentPage * pagination.pageSize + 1} -{' '}
                  {Math.min((pagination.currentPage + 1) * pagination.pageSize, pagination.totalElements)}{' '}
                  trong tổng số {pagination.totalElements}
                </p>
                <div className="flex gap-2">
                  <button
                    onClick={() => handlePageChange(pagination.currentPage - 1)}
                    disabled={pagination.currentPage === 0}
                    className="px-4 py-2 border border-neutral-300 rounded-lg hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Trước
                  </button>
                  <div className="flex items-center gap-1">
                    {[...Array(pagination.totalPages)].map((_, index) => (
                      <button
                        key={index}
                        onClick={() => handlePageChange(index)}
                        className={`px-4 py-2 rounded-lg ${
                          pagination.currentPage === index
                            ? 'bg-primary text-white'
                            : 'border border-neutral-300 hover:bg-neutral-50'
                        }`}
                      >
                        {index + 1}
                      </button>
                    ))}
                  </div>
                  <button
                    onClick={() => handlePageChange(pagination.currentPage + 1)}
                    disabled={pagination.currentPage >= pagination.totalPages - 1}
                    className="px-4 py-2 border border-neutral-300 rounded-lg hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed"
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

export default JobManager;
