import React, { useState, useEffect } from 'react';
import { Users, Briefcase, FileText, Building2, TrendingUp, Loader2 } from 'lucide-react';
import { toast } from 'react-toastify';
import {
  AreaChart,
  Area,
  PieChart,
  Pie,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Cell,
} from 'recharts';
import adminService from '../../services/admin.service';

/**
 * AdminDashboard Component
 * Tổng quan thống kê hệ thống với dữ liệu thời gian thực và các biểu đồ trực quan
 * 
 * Tính năng:
 * - Thống kê thời gian thực từ API backend
 * - Biểu đồ tương tác: Tăng trưởng người dùng (Area), Phân bố tin đăng (Pie), Xu hướng ứng tuyển (Bar)
 * - Bố cục lưới đáp ứng với các khung xương tải
 * - Thẻ thống kê có mã màu
 * 
 * Tích hợp API:
 * - GET /api/v1/admin/dashboard/stats - Thống kê toàn diện với dữ liệu biểu đồ
 * 
 * Biểu đồ:
 * - Biểu đồ Area: Người dùng mới trong 6 tháng qua
 * - Biểu đồ Pie: Phân bố tin đăng theo danh mục
 * - Biểu đồ Bar: Xu hướng ứng tuyển trong 6 tháng qua
 */
const AdminDashboard = () => {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState(null);

  // Lấy thống kê dashboard khi component được mount
  useEffect(() => {
    fetchStatistics();
  }, []);

  /**
   * Lấy thống kê toàn diện hệ thống từ backend
   */
  const fetchStatistics = async () => {
    try {
      setLoading(true);
      const response = await adminService.getDashboardStats();
      setStats(response.data);
    } catch (error) {
      console.error('Error fetching dashboard statistics:', error);
      toast.error('Không thể tải thống kê hệ thống');
    } finally {
      setLoading(false);
    }
  };

  // Bảng màu cho biểu đồ (Chủ đề Xanh dương chính)
  const CHART_COLORS = ['#4F46E5', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#06B6D4', '#EC4899', '#14B8A6'];

  // Cấu hình thẻ thống kê
  const statCards = stats ? [
    {
      title: 'Tổng người dùng',
      value: stats.totalUsers || 0,
      icon: Users,
      bgColor: 'bg-blue-100',
      textColor: 'text-blue-600',
      iconBg: 'bg-blue-500',
    },
    {
      title: 'Tổng tin đăng',
      value: stats.totalJobs || 0,
      icon: Briefcase,
      bgColor: 'bg-green-100',
      textColor: 'text-green-600',
      iconBg: 'bg-green-500',
    },
    {
      title: 'Tổng đơn ứng tuyển',
      value: stats.totalApplications || 0,
      icon: FileText,
      bgColor: 'bg-purple-100',
      textColor: 'text-purple-600',
      iconBg: 'bg-purple-500',
    },
    {
      title: 'Doanh nghiệp',
      value: stats.totalEmployers || 0,
      icon: Building2,
      bgColor: 'bg-amber-100',
      textColor: 'text-amber-600',
      iconBg: 'bg-amber-500',
    },
  ] : [];

  // Khung xương tải
  if (loading) {
    return (
      <div className="space-y-6">
        {/* Header Skeleton */}
        <div className="mb-8">
          <div className="h-9 w-64 bg-neutral-200 rounded animate-pulse mb-2"></div>
          <div className="h-5 w-96 bg-neutral-200 rounded animate-pulse"></div>
        </div>

        {/* Stats Cards Skeleton */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="bg-white rounded-lg shadow-sm border border-neutral-200 p-6">
              <div className="flex items-center justify-between mb-4">
                <div className="w-12 h-12 bg-neutral-200 rounded-lg animate-pulse"></div>
                <div className="w-16 h-6 bg-neutral-200 rounded-full animate-pulse"></div>
              </div>
              <div className="h-4 w-24 bg-neutral-200 rounded animate-pulse mb-2"></div>
              <div className="h-8 w-20 bg-neutral-200 rounded animate-pulse"></div>
            </div>
          ))}
        </div>

        {/* Charts Skeleton */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="bg-white rounded-lg shadow-sm border border-neutral-200 p-6">
              <div className="h-6 w-48 bg-neutral-200 rounded animate-pulse mb-4"></div>
              <div className="h-80 bg-neutral-100 rounded animate-pulse"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (!stats) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="text-center">
          <p className="text-neutral-600 mb-4">Không thể tải dữ liệu thống kê</p>
          <button
            onClick={fetchStatistics}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            Thử lại
          </button>
        </div>
      </div>
    );
  }

  return (
    <div>
      {/* Page Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-neutral-900 flex items-center gap-2">
          <TrendingUp className="w-8 h-8 text-blue-600" />
          Tổng quan hệ thống
        </h1>
        <p className="text-neutral-600 mt-2">
          Thống kê tổng quan về người dùng, công việc và đơn ứng tuyển
        </p>
      </div>

      {/* Statistics Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {statCards.map((card, index) => {
          const Icon = card.icon;
          return (
            <div
              key={index}
              className="bg-white rounded-lg shadow-sm border border-neutral-200 p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between mb-4">
                <div className={`w-12 h-12 ${card.iconBg} rounded-lg flex items-center justify-center`}>
                  <Icon className="w-6 h-6 text-white" />
                </div>
              </div>
              <div>
                <p className="text-neutral-600 text-sm mb-1">{card.title}</p>
                <p className={`text-3xl font-bold ${card.textColor}`}>
                  {card.value.toLocaleString('vi-VN')}
                </p>
              </div>
            </div>
          );
        })}
      </div>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        {/* User Growth Chart (Area) */}
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-6 lg:col-span-2">
          <h2 className="text-lg font-bold text-neutral-900 mb-4">Tăng trưởng người dùng (6 tháng gần nhất)</h2>
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={stats.newUsersChart || []}>
              <defs>
                <linearGradient id="colorUsers" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#4F46E5" stopOpacity={0.8} />
                  <stop offset="95%" stopColor="#4F46E5" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" />
              <XAxis dataKey="date" stroke="#6B7280" style={{ fontSize: '12px' }} />
              <YAxis stroke="#6B7280" style={{ fontSize: '12px' }} />
              <Tooltip 
                contentStyle={{ backgroundColor: '#FFF', border: '1px solid #E5E7EB', borderRadius: '8px' }}
                labelStyle={{ color: '#374151', fontWeight: 'bold' }}
              />
              <Legend wrapperStyle={{ fontSize: '14px' }} />
              <Area
                type="monotone"
                dataKey="count"
                name="Người dùng mới"
                stroke="#4F46E5"
                fillOpacity={1}
                fill="url(#colorUsers)"
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        {/* Job Distribution Chart (Pie) */}
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-6">
          <h2 className="text-lg font-bold text-neutral-900 mb-4">Phân bố công việc theo ngành</h2>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={stats.jobsByCategory || []}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
              >
                {(stats.jobsByCategory || []).map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                ))}
              </Pie>
              <Tooltip 
                contentStyle={{ backgroundColor: '#FFF', border: '1px solid #E5E7EB', borderRadius: '8px' }}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>

        {/* Applications Trend Chart (Bar) */}
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-6">
          <h2 className="text-lg font-bold text-neutral-900 mb-4">Xu hướng đơn ứng tuyển (6 tháng)</h2>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={stats.applicationsChart || []}>
              <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" />
              <XAxis dataKey="date" stroke="#6B7280" style={{ fontSize: '12px' }} />
              <YAxis stroke="#6B7280" style={{ fontSize: '12px' }} />
              <Tooltip 
                contentStyle={{ backgroundColor: '#FFF', border: '1px solid #E5E7EB', borderRadius: '8px' }}
                labelStyle={{ color: '#374151', fontWeight: 'bold' }}
              />
              <Legend wrapperStyle={{ fontSize: '14px' }} />
              <Bar dataKey="count" name="Đơn ứng tuyển" fill="#10B981" radius={[8, 8, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Additional Stats Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Job Status Breakdown */}
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-6">
          <h2 className="text-lg font-bold text-neutral-900 mb-4">Trạng thái công việc</h2>
          <div className="space-y-3">
            <div className="flex justify-between items-center p-3 bg-green-50 rounded-lg">
              <span className="text-sm text-neutral-600">Đang hoạt động</span>
              <span className="text-sm font-bold text-green-600">{stats.activeJobs || 0}</span>
            </div>
            <div className="flex justify-between items-center p-3 bg-yellow-50 rounded-lg">
              <span className="text-sm text-neutral-600">Chờ duyệt</span>
              <span className="text-sm font-bold text-yellow-600">{stats.pendingJobs || 0}</span>
            </div>
            <div className="flex justify-between items-center p-3 bg-neutral-50 rounded-lg">
              <span className="text-sm text-neutral-600">Đã đóng</span>
              <span className="text-sm font-bold text-neutral-600">{stats.closedJobs || 0}</span>
            </div>
            <div className="flex justify-between items-center p-3 bg-neutral-50 rounded-lg">
              <span className="text-sm text-neutral-600">Đã ẩn</span>
              <span className="text-sm font-bold text-neutral-600">{stats.hiddenJobs || 0}</span>
            </div>
          </div>
        </div>

        {/* Company Status */}
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-6">
          <h2 className="text-lg font-bold text-neutral-900 mb-4">Trạng thái doanh nghiệp</h2>
          <div className="space-y-3">
            <div className="flex justify-between items-center p-3 bg-green-50 rounded-lg">
              <span className="text-sm text-neutral-600">Đang hoạt động</span>
              <span className="text-sm font-bold text-green-600">{stats.activeEmployers || 0}</span>
            </div>
            <div className="flex justify-between items-center p-3 bg-yellow-50 rounded-lg">
              <span className="text-sm text-neutral-600">Chờ duyệt</span>
              <span className="text-sm font-bold text-yellow-600">{stats.pendingEmployers || 0}</span>
            </div>
            <div className="flex justify-between items-center p-3 bg-red-50 rounded-lg">
              <span className="text-sm text-neutral-600">Bị khóa</span>
              <span className="text-sm font-bold text-red-600">{stats.blockedEmployers || 0}</span>
            </div>
          </div>
        </div>

        {/* Application Metrics */}
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-6">
          <h2 className="text-lg font-bold text-neutral-900 mb-4">Thống kê đơn ứng tuyển</h2>
          <div className="space-y-3">
            <div className="flex justify-between items-center p-3 bg-blue-50 rounded-lg">
              <span className="text-sm text-neutral-600">Hôm nay</span>
              <span className="text-sm font-bold text-blue-600">{stats.applicationsToday || 0}</span>
            </div>
            <div className="flex justify-between items-center p-3 bg-purple-50 rounded-lg">
              <span className="text-sm text-neutral-600">Tháng này</span>
              <span className="text-sm font-bold text-purple-600">{stats.applicationsThisMonth || 0}</span>
            </div>
            <div className="flex justify-between items-center p-3 bg-neutral-50 rounded-lg">
              <span className="text-sm text-neutral-600">Tổng cộng</span>
              <span className="text-sm font-bold text-neutral-900">{stats.totalApplications || 0}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
