import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Pencil, Trash2, Eye, EyeOff, Loader2, Calendar, MapPin, DollarSign, Users } from 'lucide-react';
import { toast } from 'react-toastify';
import jobService from '../../services/job.service';
import { format } from 'date-fns';

/**
 * JobManager Component
 * List and manage employer's job postings
 * 
 * Features:
 * - Display jobs in table format
 * - Filter and pagination
 * - Quick actions: Edit, Delete/Hide
 * - Status badges with Vietnamese labels
 * - Application count per job
 * - Navigate to create/edit forms
 * 
 * Status Mapping (Vietnamese):
 * - PENDING: "Chờ xét duyệt" (Yellow)
 * - WAIT: "Chưa mở" (Gray)
 * - ACTIVE: "Đang mở" (Green)
 * - CLOSED: "Đã đóng" (Red)
 * - HIDDEN: "Tạm ẩn" (Dark Gray)
 */
const JobManager = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [jobs, setJobs] = useState([]);
  const [pagination, setPagination] = useState({
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    pageSize: 10,
  });
  const [deletingId, setDeletingId] = useState(null);

  useEffect(() => {
    fetchJobs(0);
  }, []);

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

  const handleDelete = async (jobId, jobTitle) => {
    const confirmDelete = window.confirm(
      `Bạn có chắc chắn muốn xóa tin tuyển dụng "${jobTitle}"?\n\nTin tuyển dụng sẽ bị ẩn và không thể khôi phục.`
    );
    
    if (!confirmDelete) return;

    try {
      setDeletingId(jobId);
      await jobService.deleteJob(jobId);
      toast.success('Xóa tin tuyển dụng thành công');
      await fetchJobs(pagination.currentPage);
    } catch (error) {
      console.error('Error deleting job:', error);
      toast.error('Không thể xóa tin tuyển dụng');
    } finally {
      setDeletingId(null);
    }
  };

  const handleToggleStatus = async (jobId, currentStatus) => {
    const newStatus = currentStatus === 'HIDDEN' ? 'ACTIVE' : 'HIDDEN';
    const statusText = newStatus === 'HIDDEN' ? 'ẩn' : 'hiển thị';

    try {
      await jobService.updateJobStatus(jobId, newStatus);
      toast.success(`Đã ${statusText} tin tuyển dụng`);
      await fetchJobs(pagination.currentPage);
    } catch (error) {
      console.error('Error toggling job status:', error);
      toast.error(`Không thể ${statusText} tin tuyển dụng`);
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
                        Ứng tuyển
                      </th>
                      <th className="px-6 py-4 text-center text-sm font-semibold text-neutral-700">
                        Hành động
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-neutral-200">
                    {jobs.map((job) => (
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
                          {job.jobCategory?.jcName || 'N/A'}
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
                        <td className="px-6 py-4 text-center">
                          <span className="inline-flex items-center gap-1 px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm font-semibold">
                            <Users className="w-4 h-4" />
                            {job.applicationCount || 0}
                          </span>
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

                            {/* Toggle Visibility Button */}
                            <button
                              onClick={() => handleToggleStatus(job.jobId, job.jobStatus)}
                              className={`p-2 rounded-lg transition-colors ${
                                job.jobStatus === 'HIDDEN'
                                  ? 'text-green-600 hover:bg-green-50'
                                  : 'text-gray-600 hover:bg-gray-50'
                              }`}
                              title={job.jobStatus === 'HIDDEN' ? 'Hiển thị' : 'Ẩn'}
                            >
                              {job.jobStatus === 'HIDDEN' ? (
                                <Eye className="w-4 h-4" />
                              ) : (
                                <EyeOff className="w-4 h-4" />
                              )}
                            </button>

                            {/* Delete Button */}
                            <button
                              onClick={() => handleDelete(job.jobId, job.jobTitle)}
                              disabled={deletingId === job.jobId}
                              className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
                              title="Xóa"
                            >
                              {deletingId === job.jobId ? (
                                <Loader2 className="w-4 h-4 animate-spin" />
                              ) : (
                                <Trash2 className="w-4 h-4" />
                              )}
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
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
