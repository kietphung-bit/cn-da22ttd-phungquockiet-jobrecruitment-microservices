import { useState, useEffect } from 'react';
import { Briefcase, FileText, CheckCircle, Clock, TrendingUp, Users } from 'lucide-react';
import jobService from '../../services/job.service';
import applicationService from '../../services/application.service';
import { toast } from 'react-toastify';

/**
 * EmployerDashboard Component
 * Bảng điều khiển tổng quan cho nhà tuyển dụng hiển thị các thống kê chính
 * 
 * Tính năng:
 * - Thẻ thống kê: Tin đang hiển thị, Đơn ứng tuyển mới, Đã duyệt, Đang chờ
 * - Lối tắt hành động nhanh
 * - Tóm tắt hoạt động gần đây
 * - Kết nối với API thực: GET /api/v1/jobs/me và GET /api/v1/applications/company
 */
const EmployerDashboard = () => {
  const [stats, setStats] = useState({
    activeJobs: 0,
    totalApplications: 0,
    approvedApplications: 0,
    pendingApplications: 0,
  });

  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardStats();
  }, []);

  const fetchDashboardStats = async () => {
    try {
      setLoading(true);

      // Lấy số lượng tin đang hiển thị
      const jobsResponse = await jobService.getMyJobs({ 
        page: 0, 
        size: 1000,
        sort: 'createdAt,desc' 
      });
      
      // Xử lý cấu trúc phản hồi: response.data chứa đối tượng Page
      const jobsData = jobsResponse?.data || jobsResponse;
      const jobs = jobsData?.content || [];
      const activeJobsCount = jobs.filter(job => job.jobStatus === 'ACTIVE').length;

      // Lấy tất cả đơn ứng tuyển cho các công việc của công ty
      const appsResponse = await applicationService.getCompanyApplications({ 
        page: 0, 
        size: 1000 
      });
      
      // Xử lý cấu trúc phản hồi: response.data chứa đối tượng Page
      const appsData = appsResponse?.data || appsResponse;
      const applications = appsData?.content || [];
      const pendingCount = applications.filter(app => app.apStatus === 'PENDING').length;
      const approvedCount = applications.filter(app => app.apStatus === 'APPROVED').length;

      setStats({
        activeJobs: activeJobsCount,
        totalApplications: applications.length,
        approvedApplications: approvedCount,
        pendingApplications: pendingCount,
      });
    } catch (error) {
      console.error('Error fetching dashboard stats:', error);
      toast.error('Không thể tải thống kê dashboard');
    } finally {
      setLoading(false);
    }
  };

  const statsCards = [
    {
      title: 'Tin đang hiển thị',
      value: stats.activeJobs,
      icon: Briefcase,
      bgColor: 'bg-blue-50',
      iconColor: 'text-blue-600',
      borderColor: 'border-blue-200',
    },
    {
      title: 'Tổng số đơn ứng tuyển',
      value: stats.totalApplications,
      icon: FileText,
      bgColor: 'bg-green-50',
      iconColor: 'text-green-600',
      borderColor: 'border-green-200',
    },
    {
      title: 'Hồ sơ đã duyệt',
      value: stats.approvedApplications,
      icon: CheckCircle,
      bgColor: 'bg-purple-50',
      iconColor: 'text-purple-600',
      borderColor: 'border-purple-200',
    },
    {
      title: 'Đang chờ xử lý',
      value: stats.pendingApplications,
      icon: Clock,
      bgColor: 'bg-yellow-50',
      iconColor: 'text-yellow-600',
      borderColor: 'border-yellow-200',
    },
  ];

  return (
    <div className="min-h-screen bg-neutral-50">
      {/* Header */}
      <div className="bg-white border-b border-neutral-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-neutral-900">Tổng quan</h1>
              <p className="text-neutral-600 mt-1">
                Xem thông tin tuyển dụng và quản lý ứng viên
              </p>
            </div>
            <div className="flex items-center gap-2 text-sm text-neutral-600">
              <TrendingUp className="w-5 h-5 text-green-500" />
              <span>Cập nhật mới nhất</span>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {loading ? (
            // Loading skeletons
            Array.from({ length: 4 }).map((_, index) => (
              <div key={index} className="bg-white rounded-lg shadow-sm p-6 border border-neutral-200 animate-pulse">
                <div className="h-12 w-12 bg-neutral-200 rounded-lg mb-4"></div>
                <div className="h-4 bg-neutral-200 rounded w-2/3 mb-2"></div>
                <div className="h-8 bg-neutral-200 rounded w-1/2"></div>
              </div>
            ))
          ) : (
            statsCards.map((card, index) => (
              <div
                key={index}
                className={`bg-white rounded-lg shadow-sm p-6 border-2 ${card.borderColor} hover:shadow-md transition-shadow`}
              >
                <div className={`w-12 h-12 ${card.bgColor} rounded-lg flex items-center justify-center mb-4`}>
                  <card.icon className={`w-6 h-6 ${card.iconColor}`} />
                </div>
                <p className="text-sm text-neutral-600 mb-1">{card.title}</p>
                <p className="text-3xl font-bold text-neutral-900">{card.value}</p>
              </div>
            ))
          )}
        </div>

        {/* Quick Actions */}
        <div className="bg-white rounded-lg shadow-sm p-6 border border-neutral-200 mb-8">
          <h2 className="text-lg font-bold text-neutral-900 mb-4">Thao tác nhanh</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <a
              href="/employer/jobs/new"
              className="flex items-center gap-3 px-4 py-3 bg-primary text-white rounded-lg hover:bg-primary-600 transition-colors"
            >
              <Briefcase className="w-5 h-5" />
              <span className="font-semibold">Đăng tin tuyển dụng mới</span>
            </a>
            <a
              href="/employer/applications"
              className="flex items-center gap-3 px-4 py-3 bg-neutral-100 text-neutral-900 rounded-lg hover:bg-neutral-200 transition-colors"
            >
              <FileText className="w-5 h-5" />
              <span className="font-semibold">Xem hồ sơ ứng tuyển</span>
            </a>
            <a
              href="/employer/profile"
              className="flex items-center gap-3 px-4 py-3 bg-neutral-100 text-neutral-900 rounded-lg hover:bg-neutral-200 transition-colors"
            >
              <Users className="w-5 h-5" />
              <span className="font-semibold">Cập nhật hồ sơ công ty</span>
            </a>
          </div>
        </div>

        {/* Info Cards */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Recent Activity */}
          <div className="bg-white rounded-lg shadow-sm p-6 border border-neutral-200">
            <h2 className="text-lg font-bold text-neutral-900 mb-4">Hoạt động gần đây</h2>
            <div className="space-y-4">
              <div className="flex items-start gap-3 pb-4 border-b border-neutral-100">
                <div className="w-8 h-8 bg-blue-50 rounded-full flex items-center justify-center flex-shrink-0">
                  <FileText className="w-4 h-4 text-blue-600" />
                </div>
                <div className="flex-1">
                  <p className="text-sm text-neutral-900 font-medium">
                    Nhận 3 hồ sơ ứng tuyển mới
                  </p>
                  <p className="text-xs text-neutral-500 mt-0.5">Vị trí: Frontend Developer</p>
                </div>
                <span className="text-xs text-neutral-400">2 giờ trước</span>
              </div>
              <div className="flex items-start gap-3 pb-4 border-b border-neutral-100">
                <div className="w-8 h-8 bg-green-50 rounded-full flex items-center justify-center flex-shrink-0">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                </div>
                <div className="flex-1">
                  <p className="text-sm text-neutral-900 font-medium">
                    Đã duyệt 2 hồ sơ
                  </p>
                  <p className="text-xs text-neutral-500 mt-0.5">Vị trí: Backend Developer</p>
                </div>
                <span className="text-xs text-neutral-400">5 giờ trước</span>
              </div>
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 bg-purple-50 rounded-full flex items-center justify-center flex-shrink-0">
                  <Briefcase className="w-4 h-4 text-purple-600" />
                </div>
                <div className="flex-1">
                  <p className="text-sm text-neutral-900 font-medium">
                    Đăng tin tuyển dụng mới
                  </p>
                  <p className="text-xs text-neutral-500 mt-0.5">Vị trí: DevOps Engineer</p>
                </div>
                <span className="text-xs text-neutral-400">1 ngày trước</span>
              </div>
            </div>
          </div>

          {/* Tips & Guides */}
          <div className="bg-gradient-to-br from-blue-50 to-indigo-50 rounded-lg shadow-sm p-6 border border-blue-200">
            <h2 className="text-lg font-bold text-neutral-900 mb-4">Mẹo tuyển dụng hiệu quả</h2>
            <ul className="space-y-3">
              <li className="flex items-start gap-2">
                <span className="text-blue-600 font-bold">•</span>
                <span className="text-sm text-neutral-700">
                  Viết mô tả công việc rõ ràng, chi tiết để thu hút ứng viên phù hợp
                </span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-blue-600 font-bold">•</span>
                <span className="text-sm text-neutral-700">
                  Cập nhật thông tin công ty thường xuyên để tăng độ tin cậy
                </span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-blue-600 font-bold">•</span>
                <span className="text-sm text-neutral-700">
                  Phản hồi hồ sơ ứng viên trong vòng 24-48 giờ
                </span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-blue-600 font-bold">•</span>
                <span className="text-sm text-neutral-700">
                  Sử dụng các từ khóa phổ biến để tin đăng dễ tìm thấy hơn
                </span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EmployerDashboard;
