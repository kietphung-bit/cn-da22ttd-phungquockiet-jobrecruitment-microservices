import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Building2, MapPin, Globe, Mail, Loader2, Briefcase, ArrowLeft } from 'lucide-react';
import companyService from '../../services/company.service';
import JobCard from '../../components/common/JobCard';
import { formatVND } from '../../utils/formatters';

/**
 * CompanyDetailPage Component
 * Hiển thị thông tin chi tiết về một công ty cụ thể
 * 
 * Tính năng:
 * - Thông tin hồ sơ công ty
 * - Mô tả công ty
 * - Danh sách công việc tại công ty này
 * - Hỗ trợ tiếng Việt
 */
const CompanyDetailPage = () => {
  const { id } = useParams();

  // Hàm tiện ích để lấy URL đầy đủ của tệp tải lên từ backend
  const getBackendFileUrl = (filePath) => {
    if (!filePath) return null;
    if (filePath.startsWith('http')) return filePath; // Already full URL
    const baseUrl = import.meta.env.VITE_API_URL?.replace('/api/v1', '') || 'http://localhost:8080';
    return `${baseUrl}${filePath}`;
  };

  const [company, setCompany] = useState(null);
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [jobsLoading, setJobsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Lấy thông tin chi tiết công ty
  useEffect(() => {
    const fetchCompanyDetails = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await companyService.getCompanyDetail(id);
        const companyData = response.data;

        // Ánh xạ phản hồi API sang định dạng component
        const mappedCompany = {
          companyId: companyData.companyId,
          code: companyData.companyCode,
          name: companyData.companyName || 'Tên công ty',
          description: companyData.companyDescription || 'Chưa có mô tả về công ty.',
          address: companyData.companyAddress || 'Chưa cập nhật',
          website: companyData.companyWebsite || null,
          email: companyData.companyEmail || null,
          logo: companyData.logoURL || null,
          status: companyData.companyStatus || 'ACTIVE',
        };

        setCompany(mappedCompany);

        // Lấy danh sách công việc của công ty này
        fetchCompanyJobs();
      } catch (err) {
        console.error('Error fetching company details:', err);
        setError('Không thể tải thông tin công ty. Vui lòng thử lại sau.');
      } finally {
        setLoading(false);
      }
    };

    const fetchCompanyJobs = async () => {
      try {
        setJobsLoading(true);
        const response = await companyService.getJobsByCompany(id, {
          page: 0,
          size: 6,
          sort: 'createdAt,desc',
        });

        // Chuyển đổi công việc sang định dạng hiển thị
        const mappedJobs = response.data.content.map(job => ({
          jobId: job.jobId,
          title: job.jobTitle,
          company: job.companyName || 'Tên công ty',
          logoURL: job.logoURL || null,
          salary: job.jobSalary ? formatVND(job.jobSalary) : 'Thỏa thuận',
          location: job.jobLocation || 'Chưa cập nhật',
        }));

        setJobs(mappedJobs);
      } catch (err) {
        console.error('Error fetching company jobs:', err);
        // Không hiển thị lỗi cho công việc, chỉ giữ mảng rỗng
        setJobs([]);
      } finally {
        setJobsLoading(false);
      }
    };

    if (id) {
      fetchCompanyDetails();
    }
  }, [id]);

  // Trạng thái tải
  if (loading) {
    return (
      <div className="min-h-screen bg-neutral-50 flex items-center justify-center">
        <div className="text-center">
          <Loader2 className="w-16 h-16 text-primary animate-spin mx-auto mb-4" />
          <p className="text-neutral-600">Đang tải thông tin công ty...</p>
        </div>
      </div>
    );
  }

  // Trạng thái lỗi hoặc không tìm thấy công ty
  if (error || !company) {
    return (
      <div className="min-h-screen bg-neutral-50 flex items-center justify-center">
        <div className="bg-white rounded-lg shadow-md p-8 max-w-md text-center">
          <Building2 className="w-16 h-16 text-red-400 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-neutral-900 mb-2">
            Không tìm thấy công ty
          </h2>
          <p className="text-neutral-600 mb-6">
            {error || 'Công ty không tồn tại hoặc đã bị xóa.'}
          </p>
          <Link
            to="/companies"
            className="inline-flex items-center gap-2 px-6 py-3 bg-primary text-white rounded-lg hover:bg-primary-600"
          >
            <ArrowLeft className="w-5 h-5" />
            Quay Lại Danh Sách
          </Link>
        </div>
      </div>
    );
  }

  // Nhãn trạng thái công ty
  const getStatusBadge = (status) => {
    const statusMap = {
      PENDING: { text: 'Chờ xét duyệt', color: 'bg-yellow-100 text-yellow-800' },
      ACTIVE: { text: 'Đang hoạt động', color: 'bg-green-100 text-green-800' },
      BLOCKED: { text: 'Bị khóa', color: 'bg-red-100 text-red-800' },
    };

    const statusInfo = statusMap[status] || statusMap.ACTIVE;

    return (
      <span className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${statusInfo.color}`}>
        {statusInfo.text}
      </span>
    );
  };

  return (
    <div className="min-h-screen bg-neutral-50 py-8">
      <div className="container-custom">
        {/* Back Button */}
        {/* <Link
          to="/companies"
          className="inline-flex items-center gap-2 text-primary hover:text-primary-600 mb-6"
        >
          <ArrowLeft className="w-5 h-5" />
          Quay lại danh sách công ty
        </Link> */}

        {/* Company Header */}
        <div className="bg-white rounded-lg shadow-md overflow-hidden mb-8">
          {/* Cover Image Placeholder */}
          <div className="h-48 bg-gradient-to-r from-primary to-primary-600"></div>

          {/* Company Info */}
          <div className="p-8">
            <div className="flex flex-col md:flex-row gap-6">
              {/* Company Logo */}
              <div className="flex-shrink-0">
                <div className="w-32 h-32 -mt-20 bg-white rounded-lg shadow-lg flex items-center justify-center overflow-hidden border-4 border-white">
                  {company.logo ? (
                    <img
                      src={getBackendFileUrl(company.logo)}
                      alt={company.name}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <Building2 className="w-16 h-16 text-neutral-400" />
                  )}
                </div>
              </div>

              {/* Company Details */}
              <div className="flex-1">
                <div className="flex items-start justify-between mb-4">
                  <div>
                    <h1 className="text-3xl font-bold text-neutral-900 mb-2">
                      {company.name}
                    </h1>
                    {company.code && (
                      <p className="text-sm text-neutral-500 mb-2">
                        Mã công ty: {company.code}
                      </p>
                    )}
                    {getStatusBadge(company.status)}
                  </div>
                </div>

                {/* Contact Information */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
                  {company.address && (
                    <div className="flex items-start gap-3">
                      <MapPin className="w-5 h-5 text-primary flex-shrink-0 mt-1" />
                      <div>
                        <p className="text-sm text-neutral-500">Địa chỉ</p>
                        <p className="text-neutral-900">{company.address}</p>
                      </div>
                    </div>
                  )}

                  {company.website && (
                    <div className="flex items-start gap-3">
                      <Globe className="w-5 h-5 text-primary flex-shrink-0 mt-1" />
                      <div>
                        <p className="text-sm text-neutral-500">Website</p>
                        <a
                          href={company.website}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-primary hover:text-primary-600 hover:underline"
                        >
                          {company.website}
                        </a>
                      </div>
                    </div>
                  )}

                  {company.email && (
                    <div className="flex items-start gap-3">
                      <Mail className="w-5 h-5 text-primary flex-shrink-0 mt-1" />
                      <div>
                        <p className="text-sm text-neutral-500">Email</p>
                        <a
                          href={`mailto:${company.email}`}
                          className="text-primary hover:text-primary-600 hover:underline"
                        >
                          {company.email}
                        </a>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Company Description */}
        <div className="bg-white rounded-lg shadow-md p-8 mb-8">
          <h2 className="text-2xl font-bold text-neutral-900 mb-4">
            Về công ty
          </h2>
          <div className="prose max-w-none text-neutral-700 whitespace-pre-line">
            {company.description}
          </div>
        </div>

        {/* Jobs at this Company */}
        <div className="bg-white rounded-lg shadow-md p-8">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-bold text-neutral-900 flex items-center gap-2">
              <Briefcase className="w-6 h-6 text-primary" />
              Việc làm tại công ty này
            </h2>
          </div>

          {jobsLoading ? (
            <div className="flex justify-center items-center py-12">
              <Loader2 className="w-10 h-10 text-primary animate-spin" />
            </div>
          ) : jobs.length > 0 ? (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {jobs.map(job => (
                  <JobCard key={job.jobId} job={job} />
                ))}
              </div>

              {/* View All Jobs Link */}
              <div className="text-center mt-8">
                <Link
                  to={`/jobs?company=${company.companyId}`}
                  className="inline-block px-6 py-3 border-2 border-primary text-primary rounded-lg font-semibold hover:bg-primary hover:text-white transition-all duration-200"
                >
                  Xem tất cả việc làm
                </Link>
              </div>
            </>
          ) : (
            <div className="text-center py-12">
              <Briefcase className="w-16 h-16 text-neutral-300 mx-auto mb-4" />
              <p className="text-neutral-600 text-lg">
                Công ty hiện không có việc làm nào đang tuyển dụng
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CompanyDetailPage;
