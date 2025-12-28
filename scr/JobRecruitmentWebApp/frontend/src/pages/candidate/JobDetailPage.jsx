import React, { useState, useEffect } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { MapPin, DollarSign, Briefcase, Clock, CheckCircle, Building2, Globe, Users, Calendar, Share2, Heart } from 'lucide-react';
import { toast } from 'react-toastify';
import JobCard from '../../components/common/JobCard';
import ApplyModal from '../../components/features/job/ApplyModal';
import jobService from '../../services/job.service';
import savedJobService from '../../services/savedJob.service';
import applicationService from '../../services/application.service';
import { useAuth } from '../../contexts/AuthContext';
import { formatVND, formatDate } from '../../utils/formatters';

/**
 * JobDetailPage Component
 * Based on Wireframe Specification:
 * - Header: Job Title, Salary, "Apply Now" button prominently, Save/Bookmark button
 * - Two-column layout: Left (Description, Requirements), Right (Company Info Card)
 * - Related jobs at bottom
 */
const JobDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();
  
  const [job, setJob] = useState(null);
  const [relatedJobs, setRelatedJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isSaved, setIsSaved] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [showApplyModal, setShowApplyModal] = useState(false);
  const [hasApplied, setHasApplied] = useState(false);
  const [checkingApplication, setCheckingApplication] = useState(false);

  const getBackendFileUrl = (filePath) => {
    if (!filePath) return null;
    if (filePath.startsWith('http')) return filePath;
    const baseUrl = import.meta.env.VITE_API_URL?.replace('/api/v1', '') || 'http://localhost:8080';
    return `${baseUrl}${filePath}`;
  };

  // Fetch job details on component mount
  useEffect(() => {
    const fetchJobDetail = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const response = await jobService.getJobDetail(id);
        const jobData = response.data;
        
        // Map API response to component format
        const mappedJob = {
          jobId: jobData.jobId,
          title: jobData.jobTitle,
          company: jobData.companyName || 'Tên công ty',
          companyLogo: jobData.logoURL || null,
          companyId: jobData.companyId || null,
          salary: jobData.jobSalary ? formatVND(jobData.jobSalary) : 'Thỏa thuận',
          location: jobData.jobLocation || 'Chưa cập nhật',
          experience: jobData.experience || 'Chưa xác định',
          posted: formatDate(jobData.createdAt, 'relative'),
          deadline: jobData.endDate ? formatDate(jobData.endDate, 'long') : 'Tuyển cho đến khi đủ người',
          vacancies: jobData.maxCandidates || 1,
          description: jobData.jobDescription || 'Chưa có mô tả.',
          requirements: jobData.jobRequirement ? jobData.jobRequirement.split('\n').filter(r => r.trim()) : [],
          responsibilities: jobData.jobResponsibilities ? jobData.jobResponsibilities.split('\n').filter(r => r.trim()) : [],
          benefits: jobData.benefits ? jobData.benefits.split('\n').filter(b => b.trim()) : [],
          companyInfo: {
            name: jobData.companyName || 'Tên công ty',
            logo: jobData.companyLogo || null,
            description: jobData.companyDescription || 'Chưa có mô tả công ty.',
            size: jobData.companySize || 'Chưa xác định',
            industry: jobData.industry || 'Chưa xác định',
            website: jobData.companyWebsite || null,
            founded: jobData.companyFounded || 'Chưa xác định',
            location: jobData.companyLocation || jobData.jobLocation || 'Chưa cập nhật',
          },
        };
        
        setJob(mappedJob);

        // Check if job is saved
        if (isAuthenticated && user?.role === 'UV' && id) {
          try {
            const jobIdNum = parseInt(id, 10);
            console.log('JobDetailPage - Checking if saved:', { id, jobIdNum, user });
            const saved = await savedJobService.checkIsSaved(jobIdNum);
            console.log('JobDetailPage - Is saved result:', saved);
            setIsSaved(saved);
          } catch (err) {
            console.error('Error checking saved status:', err);
          }
        }

        // Fetch related jobs if jcId is available
        if (jobData.jcId) {
          try {
            const relatedResponse = await jobService.getRelatedJobs(jobData.jcId, jobData.jobId, 4);
            const mappedRelatedJobs = relatedResponse.data.content.map(relJob => ({
              jobId: relJob.jobId,
              title: relJob.jobTitle,
              company: relJob.companyName || 'Tên công ty',
              logoURL: relJob.logoURL || null,
              salary: relJob.jobSalary ? formatVND(relJob.jobSalary) : 'Thỏa thuận',
              location: relJob.jobLocation || 'Chưa cập nhật',
            }));
            setRelatedJobs(mappedRelatedJobs);
          } catch (relErr) {
            console.error('Error fetching related jobs:', relErr);
            // Don't fail the whole page if related jobs fail
          }
        }
      } catch (err) {
        console.error('Error fetching job details:', err);
        if (err.response && err.response.status === 404) {
          setError('Không tìm thấy công việc');
        } else {
          setError('Không thể tải thông tin công việc. Vui lòng thử lại sau.');
        }
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchJobDetail();
    }
  }, [id, isAuthenticated, user]);

  // Check if user has already applied to this job
  useEffect(() => {
    const checkApplicationStatus = async () => {
      if (isAuthenticated && user?.role === 'UV' && id) {
        try {
          setCheckingApplication(true);
          const jobIdNum = parseInt(id, 10);
          console.log('JobDetailPage - Checking application status for job:', jobIdNum);
          const applied = await applicationService.hasApplied(jobIdNum);
          console.log('JobDetailPage - Has applied result:', applied);
          setHasApplied(applied);
        } catch (err) {
          console.error('Error checking application status:', err);
        } finally {
          setCheckingApplication(false);
        }
      }
    };

    checkApplicationStatus();
  }, [id, isAuthenticated, user]);

  // Handle apply button click
  const handleApply = () => {
    console.log('JobDetailPage - Apply button clicked', { isAuthenticated, userRole: user?.role });
    
    // Check authentication
    if (!isAuthenticated) {
      console.log('❌ Not authenticated, redirecting to login');
      toast.info('Vui lòng đăng nhập để ứng tuyển');
      navigate('/login');
      return;
    }

    // Check if user is candidate
    if (user?.role !== 'UV') {
      console.log('❌ Wrong role:', user?.role);
      toast.warning('Chỉ ứng viên mới có thể ứng tuyển vào công việc');
      return;
    }

    // Check if already applied
    if (hasApplied) {
      console.log('ℹ️ Already applied to this job');
      toast.info('Bạn đã ứng tuyển vào công việc này rồi');
      return;
    }

    // Open apply modal
    console.log('✅ Opening apply modal');
    setShowApplyModal(true);
  };

  // Handle successful application
  const handleApplicationSuccess = () => {
    console.log('JobDetailPage - Application successful');
    setHasApplied(true);
    setShowApplyModal(false);
  };

  

  // Handle save/unsave job
  const handleSaveToggle = async () => {
    const jobIdNum = parseInt(id, 10);
    console.log('💾 JobDetailPage - Save button clicked', { id, jobIdNum, isAuthenticated, userRole: user?.role, user });
    
    if (!isAuthenticated) {
      console.log('❌ Not authenticated');
      toast.info('Vui lòng đăng nhập để lưu công việc');
      navigate('/login');
      return;
    }

    if (user?.role !== 'UV') {
      console.log('❌ Wrong role:', user?.role);
      toast.info('Chỉ ứng viên mới có thể lưu công việc');
      return;
    }

    setIsSaving(true);
    try {
      if (isSaved) {
        console.log('🗑️ Unsaving job:', jobIdNum);
        await savedJobService.unsaveJob(jobIdNum);
        setIsSaved(false);
        toast.success('Đã bỏ lưu công việc');
      } else {
        console.log('💾 Saving job:', jobIdNum);
        await savedJobService.saveJob(jobIdNum);
        setIsSaved(true);
        toast.success('Đã lưu công việc thành công');
      }
    } catch (error) {
      const errorMessage = error.response?.data?.message || 
                          (isSaved ? 'Không thể bỏ lưu công việc' : 'Không thể lưu công việc');
      toast.error(errorMessage);
    } finally {
      setIsSaving(false);
    }
  };



  // Show loading state
  if (loading) {
    return (
      <div className="bg-neutral-50 min-h-screen">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="animate-pulse">
            {/* Header skeleton */}
            <div className="bg-white rounded-lg p-8 mb-6">
              <div className="h-10 bg-neutral-200 rounded w-3/4 mb-4"></div>
              <div className="h-6 bg-neutral-200 rounded w-1/2 mb-6"></div>
              <div className="flex gap-4">
                <div className="h-8 bg-neutral-200 rounded w-32"></div>
                <div className="h-8 bg-neutral-200 rounded w-32"></div>
                <div className="h-8 bg-neutral-200 rounded w-32"></div>
              </div>
            </div>
            {/* Content skeleton */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              <div className="lg:col-span-2">
                <div className="bg-white rounded-lg p-8">
                  <div className="h-6 bg-neutral-200 rounded w-1/3 mb-4"></div>
                  <div className="space-y-2">
                    <div className="h-4 bg-neutral-200 rounded"></div>
                    <div className="h-4 bg-neutral-200 rounded"></div>
                    <div className="h-4 bg-neutral-200 rounded w-5/6"></div>
                  </div>
                </div>
              </div>
              <div>
                <div className="bg-white rounded-lg p-6">
                  <div className="h-20 w-20 bg-neutral-200 rounded-lg mb-4"></div>
                  <div className="h-6 bg-neutral-200 rounded w-2/3 mb-4"></div>
                  <div className="space-y-2">
                    <div className="h-4 bg-neutral-200 rounded"></div>
                    <div className="h-4 bg-neutral-200 rounded w-4/5"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Show error state (404 or other errors)
  if (error) {
    return (
      <div className="bg-neutral-50 min-h-screen flex items-center justify-center">
        <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8 text-center">
          <div className="text-6xl mb-4">{error === 'Job not found' ? '🔍' : '⚠️'}</div>
          <h1 className="text-3xl font-bold text-neutral-900 mb-4">
            {error === 'Không tìm thấy công việc' ? 'Không Tìm Thấy Công Việc' : 'Rất Tiếc! Đã Có Lỗi Xảy Ra'}
          </h1>
          <p className="text-neutral-600 mb-6">
            {error === 'Không tìm thấy công việc' 
              ? 'Công việc bạn đang tìm kiếm có thể đã bị xóa hoặc không còn khả dụng.'
              : error
            }
          </p>
          <div className="flex gap-4 justify-center">
            <button
              onClick={() => navigate(-1)}
              className="px-6 py-3 bg-neutral-200 text-neutral-700 rounded-lg font-semibold hover:bg-neutral-300 transition-colors"
            >
              Quay Lại
            </button>
            <Link
              to="/jobs"
              className="px-6 py-3 bg-primary text-white rounded-lg font-semibold hover:bg-primary-600 transition-colors"
            >
              Xem việc làm
            </Link>
          </div>
        </div>
      </div>
    );
  }

  // No job data
  if (!job) {
    return null;
  }

  return (
    <div className="bg-neutral-50 min-h-screen">
      {/* Hero Header with Job Title and Quick Apply */}
      <section className="bg-white border-b border-neutral-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-6">
            {/* Left: Job Info */}
            <div className="flex-1">
              {/* Job Title */}
              <h1 className="text-3xl lg:text-4xl font-bold text-neutral-900 mb-4">
                {job.title}
              </h1>

              {/* Company Link */}
              <Link
                to={`/companies/${job.companyId}`}
                className="inline-flex items-center gap-2 text-xl text-primary hover:text-primary-600 font-semibold mb-6 group"
              >
                <Building2 className="w-6 h-6" />
                {job.company}
                <svg className="w-5 h-5 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>

              {/* Job Meta Info */}
              <div className="flex flex-wrap gap-4 text-neutral-600">
                <div className="flex items-center gap-2 bg-neutral-100 px-4 py-2 rounded-lg">
                  <MapPin className="w-5 h-5 text-primary" />
                  <span className="font-medium">{job.location}</span>
                </div>
                <div className="flex items-center gap-2 bg-primary-50 px-4 py-2 rounded-lg">
                  {/* <DollarSign className="w-5 h-5 text-primary" /> */}
                  <span className="font-bold text-primary">{job.salary}</span>
                </div>
                {/* <div className="flex items-center gap-2 bg-neutral-100 px-4 py-2 rounded-lg">
                  <Briefcase className="w-5 h-5 text-primary" />
                  <span className="font-medium">{job.jobType}</span>
                </div> */}
                <div className="flex items-center gap-2 bg-neutral-100 px-4 py-2 rounded-lg">
                  <Clock className="w-5 h-5 text-primary" />
                  <span className="font-medium">Đăng {job.posted}</span>
                </div>
              </div>
            </div>

            {/* Right: Action Buttons */}
            <div className="flex flex-col sm:flex-row lg:flex-col gap-3 lg:flex-shrink-0">
              {/* Apply Button - Only show for candidates, hide for employers */}
              {(!isAuthenticated || user?.role === 'UV') && (
                <button
                  onClick={(e) => {
                    e.preventDefault();
                    console.log('🔥 APPLY BUTTON CLICKED', { hasApplied, checkingApplication, isAuthenticated, userRole: user?.role });
                    handleApply();
                  }}
                  disabled={hasApplied || checkingApplication}
                  className={`px-8 py-4 rounded-lg font-bold text-lg shadow-lg hover:shadow-xl transition-all duration-200 flex items-center justify-center gap-2 ${
                    hasApplied
                      ? 'bg-green-100 text-green-700 border-2 border-green-500 cursor-default'
                      : 'bg-primary text-white hover:bg-primary-600'
                  } ${checkingApplication ? 'opacity-50 cursor-wait' : ''}`}
                >
                  <Briefcase className="w-6 h-6" />
                  {checkingApplication ? 'Đang kiểm tra...' : hasApplied ? 'Đã ứng tuyển' : 'Ứng tuyển ngay'}
                </button>
              )}
              <button
                onClick={handleSaveToggle}
                disabled={isSaving}
                className={`px-8 py-4 rounded-lg font-semibold transition-all duration-200 flex items-center justify-center gap-2 ${
                  isSaved 
                    ? 'bg-red-50 border-2 border-red-500 text-red-600 hover:bg-red-100' 
                    : 'bg-white border-2 border-neutral-300 text-neutral-700 hover:border-primary hover:text-primary'
                } ${isSaving ? 'opacity-50 cursor-not-allowed' : ''}`}
              >
                <Heart className={`w-5 h-5 ${isSaved ? 'fill-current' : ''}`} />
                {isSaved ? 'Đã lưu' : 'Lưu việc'}
              </button>
              <button className="px-8 py-4 bg-white border-2 border-neutral-300 text-neutral-700 rounded-lg font-semibold hover:border-primary hover:text-primary transition-all duration-200 flex items-center justify-center gap-2">
                <Share2 className="w-5 h-5" />
                Chia sẻ
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* Main Content - Two Column Layout */}
      <section className="py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Left Column (2/3) - Job Details */}
            <div className="lg:col-span-2 space-y-6">
              {/* Job Description */}
              <div className="bg-white rounded-lg shadow-md p-6 lg:p-8">
                <h2 className="text-2xl font-bold text-neutral-900 mb-4 flex items-center gap-2">
                  <Briefcase className="w-6 h-6 text-primary" />
                  Mô tả công việc
                </h2>
                <div className="text-neutral-700 leading-relaxed whitespace-pre-line">
                  {job.description}
                </div>
              </div>

              {/* Key Responsibilities */}
              <div className="bg-white rounded-lg shadow-md p-6 lg:p-8">
                <h2 className="text-2xl font-bold text-neutral-900 mb-4">
                  Trách nhiệm chính
                </h2>
                <ul className="space-y-3">
                  {job.responsibilities.map((responsibility, index) => (
                    <li key={index} className="flex items-start gap-3">
                      <CheckCircle className="w-6 h-6 text-primary flex-shrink-0 mt-0.5" />
                      <span className="text-neutral-700">{responsibility}</span>
                    </li>
                  ))}
                </ul>
              </div>

              {/* Requirements */}
              <div className="bg-white rounded-lg shadow-md p-6 lg:p-8">
                <h2 className="text-2xl font-bold text-neutral-900 mb-4">
                  Yêu cầu & Trình độ
                </h2>
                <ul className="space-y-3">
                  {job.requirements.map((requirement, index) => (
                    <li key={index} className="flex items-start gap-3">
                      <CheckCircle className="w-6 h-6 text-blue-600 flex-shrink-0 mt-0.5" />
                      <span className="text-neutral-700">{requirement}</span>
                    </li>
                  ))}
                </ul>
              </div>

              {/* Benefits */}
              <div className="bg-white rounded-lg shadow-md p-6 lg:p-8">
                <h2 className="text-2xl font-bold text-neutral-900 mb-4">
                  Quyền lợi & Phúc lợi
                </h2>
                <ul className="space-y-3">
                  {job.benefits.map((benefit, index) => (
                    <li key={index} className="flex items-start gap-3">
                      <CheckCircle className="w-6 h-6 text-green-600 flex-shrink-0 mt-0.5" />
                      <span className="text-neutral-700">{benefit}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>

            {/* Right Column (1/3) - Company Info Sidebar */}
            <div className="lg:col-span-1">
              <div className="bg-white rounded-lg shadow-md p-6 sticky top-24">
                <h3 className="text-xl font-bold text-neutral-900 mb-6">
                  Về công ty
                </h3>

                {/* Company Logo */}
                <div className="mb-6">
                  {job.companyLogo ? (
                    <img
                      src={getBackendFileUrl(job.companyLogo)}
                      alt={job.companyInfo.name}
                      className="w-24 h-24 object-contain rounded-lg border border-neutral-200"
                    />
                  ) : (
                    <div className="w-24 h-24 bg-gradient-to-br from-primary-100 to-primary-200 rounded-lg flex items-center justify-center border-2 border-primary-300">
                      <span className="text-4xl font-bold text-primary">
                        {job.companyInfo.name.charAt(0)}
                      </span>
                    </div>
                  )}
                </div>

                {/* Company Name */}
                <h4 className="text-lg font-bold text-neutral-900 mb-4">
                  {job.companyInfo.name}
                </h4>

                {/* Company Description */}
                <p className="text-neutral-700 text-sm leading-relaxed mb-6">
                  {job.companyInfo.description}
                </p>

                {/* Company Details */}
                <div className="space-y-3 mb-6 border-t border-neutral-200 pt-6">
                  <div className="flex items-center gap-3 text-sm">
                    <Building2 className="w-5 h-5 text-neutral-400" />
                    <div>
                      <div className="text-neutral-500">Ngành Nghề</div>
                      <div className="font-semibold text-neutral-900">{job.companyInfo.industry}</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 text-sm">
                    <Users className="w-5 h-5 text-neutral-400" />
                    <div>
                      <div className="text-neutral-500">Quy mô</div>
                      <div className="font-semibold text-neutral-900">{job.companyInfo.size}</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 text-sm">
                    <Calendar className="w-5 h-5 text-neutral-400" />
                    <div>
                      <div className="text-neutral-500">Thành lập</div>
                      <div className="font-semibold text-neutral-900">{job.companyInfo.founded}</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 text-sm">
                    <MapPin className="w-5 h-5 text-neutral-400" />
                    <div>
                      <div className="text-neutral-500">Địa điểm</div>
                      <div className="font-semibold text-neutral-900">{job.companyInfo.location}</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 text-sm">
                    <Globe className="w-5 h-5 text-neutral-400" />
                    <div>
                      <div className="text-neutral-500">Trang web</div>
                      <a
                        href={job.companyInfo.website}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="font-semibold text-primary hover:text-primary-600"
                      >
                        Truy cập trang web
                      </a>
                    </div>
                  </div>
                </div>

                {/* View Company Profile Button */}
                <Link
                  to={`/companies/${job.companyId}`}
                  className="block w-full px-6 py-3 bg-white border-2 border-primary text-primary text-center rounded-lg font-semibold hover:bg-primary hover:text-white transition-all duration-200"
                >
                  Xem hồ sơ công ty
                </Link>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Related Jobs Section */}
      {relatedJobs.length > 0 && (
        <section className="py-12 bg-white">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex items-center justify-between mb-8">
              <h2 className="text-3xl font-bold text-neutral-900">
                Công Việc Tương Tự Bạn Có Thể Thích
              </h2>
              <Link
                to="/jobs"
                className="hidden md:inline-flex items-center gap-2 text-primary font-semibold hover:text-primary-600 transition-colors"
              >
                Xem Tất Cả Việc Làm
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>

            {/* Grid of 4 JobCard Components */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {relatedJobs.map((relatedJob) => (
                <JobCard key={relatedJob.jobId} job={relatedJob} />
              ))}
            </div>
          </div>
        </section>
      )}

      {/* Apply Modal */}
      <ApplyModal
        isOpen={showApplyModal}
        onClose={() => setShowApplyModal(false)}
        jobId={id}
        jobTitle={job?.title}
        onSuccess={handleApplicationSuccess}
      />
    </div>
  );
};

export default JobDetailPage;
