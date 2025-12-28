import React, { useState, useEffect } from 'react';
import { Heart, Search } from 'lucide-react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import JobCard from '../../components/common/JobCard';
import savedJobService from '../../services/savedJob.service';

/**
 * SavedJobsPage Component
 * Displays candidate's saved/bookmarked jobs using JobCard component
 * 
 * Features:
 * - Grid layout of saved job cards
 * - Reuses existing JobCard component with bookmark functionality
 * - Pagination support
 * - Empty state with call-to-action
 * - API: GET /api/v1/saved-jobs/me
 * 
 * Note: Based on SYSTEM_DESIGN.md SavedJob entity structure
 */
const SavedJobsPage = () => {
  const [savedJobs, setSavedJobs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [pagination, setPagination] = useState({
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    size: 12,
  });

  useEffect(() => {
    fetchSavedJobs(0);
  }, []);

  const fetchSavedJobs = async (page = 0) => {
    setLoading(true);
    setError('');
    try {
      const response = await savedJobService.getMySavedJobs({
        page,
        size: 12,
        sort: 'savedTime,desc',
      });

      console.log('SavedJobs API Response:', response);
      
      // Check if response has nested data (ApiResponse wrapper) or direct Page data
      let pageData;
      if (response.data && response.data.data) {
        // Wrapped in ApiResponse
        pageData = response.data.data;
      } else if (response.data) {
        // Direct Page data
        pageData = response.data;
      } else {
        // Response itself is the Page
        pageData = response;
      }
      
      console.log('Page Data:', pageData);
      
      setSavedJobs(pageData.content || []);
      setPagination({
        currentPage: pageData.number || 0,
        totalPages: pageData.totalPages || 0,
        totalElements: pageData.totalElements || 0,
        size: pageData.size || 12,
      });
    } catch (err) {
      console.error('Error fetching saved jobs:', err);
      setError('Không thể tải danh sách việc làm đã lưu. Vui lòng thử lại.');
      toast.error('Không thể tải danh sách việc làm đã lưu');
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pagination.totalPages) {
      fetchSavedJobs(newPage);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  const handleUnsave = async (jobId) => {
    try {
      await savedJobService.unsaveJob(jobId);
      toast.success('Đã bỏ lưu công việc');
      // Refresh the current page
      fetchSavedJobs(pagination.currentPage);
    } catch (err) {
      console.error('Error removing saved job:', err);
      const errorMessage = err.response?.data?.message || 'Không thể bỏ lưu công việc';
      toast.error(errorMessage);
    }
  };

  // Transform SavedJobResponse to JobCard compatible format
  const transformToJobCardFormat = (savedJob) => {
    // The backend returns SavedJobResponse with nested job object
    const jobData = savedJob.job || savedJob;
    
    console.log('Transforming saved job:', { savedJob, jobData });
    
    return {
      jobId: jobData.jobId || savedJob.jobId,
      title: jobData.jobTitle || savedJob.jobTitle,
      company: jobData.companyName || savedJob.companyName,
      logoURL: jobData.logoURL || savedJob.logoURL || null,
      salary: (jobData.jobSalary || savedJob.jobSalary)
        ? `${(jobData.jobSalary || savedJob.jobSalary).toLocaleString('vi-VN')} VNĐ`
        : 'Thỏa thuận',
      location: jobData.jobLocation || savedJob.jobLocation || 'Không xác định',
    };
  };

  return (
    <div className="min-h-screen bg-neutral-50">
      {/* Header */}
      <div className="bg-white border-b border-neutral-200">
        <div className="container-custom py-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-neutral-900">Việc làm đã lưu</h1>
              <p className="text-neutral-600 mt-2">
                {pagination.totalElements > 0 
                  ? `${pagination.totalElements} công việc đã lưu`
                  : 'Chưa có công việc nào được lưu'
                }
              </p>
            </div>
            <Heart className="w-12 h-12 text-red-500" fill="currentColor" />
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="container-custom py-8">
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}

        {loading ? (
          <div className="flex items-center justify-center py-20">
            <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-primary"></div>
          </div>
        ) : savedJobs.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <div className="max-w-md mx-auto">
              <Heart className="w-20 h-20 text-neutral-300 mx-auto mb-6" />
              <h2 className="text-2xl font-bold text-neutral-900 mb-3">
                Chưa có việc làm đã lưu
              </h2>
              <p className="text-neutral-600 mb-6">
                Khi bạn lưu một công việc, nó sẽ xuất hiện ở đây để bạn có thể dễ dàng quay lại xem sau
              </p>
              <Link
                to="/jobs"
                className="inline-flex items-center gap-2 px-8 py-3 bg-primary text-white rounded-lg hover:bg-primary-600 transition-colors font-medium"
              >
                <Search className="w-5 h-5" />
                Tìm kiếm việc làm
              </Link>
            </div>
          </div>
        ) : (
          <>
            {/* Jobs Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {savedJobs.map((savedJob) => (
                <JobCard 
                  key={savedJob.sjId}
                  job={transformToJobCardFormat(savedJob)}
                  isSaved={true}
                  onSaveToggle={(jobId) => handleUnsave(jobId)}
                />
              ))}
            </div>

            {/* Pagination */}
            {pagination.totalPages > 1 && (
              <div className="flex items-center justify-center gap-4 mt-12">
                <button
                  onClick={() => handlePageChange(pagination.currentPage - 1)}
                  disabled={pagination.currentPage === 0}
                  className="px-6 py-3 border border-neutral-300 text-neutral-700 rounded-lg hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-medium"
                >
                  Trang trước
                </button>
                
                <div className="flex items-center gap-2">
                  {[...Array(pagination.totalPages)].map((_, index) => (
                    <button
                      key={index}
                      onClick={() => handlePageChange(index)}
                      className={`w-10 h-10 rounded-lg font-medium transition-colors ${
                        pagination.currentPage === index
                          ? 'bg-primary text-white'
                          : 'border border-neutral-300 text-neutral-700 hover:bg-neutral-50'
                      }`}
                    >
                      {index + 1}
                    </button>
                  ))}
                </div>

                <button
                  onClick={() => handlePageChange(pagination.currentPage + 1)}
                  disabled={pagination.currentPage >= pagination.totalPages - 1}
                  className="px-6 py-3 border border-neutral-300 text-neutral-700 rounded-lg hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-medium"
                >
                  Trang sau
                </button>
              </div>
            )}

            {/* Footer Info */}
            <div className="text-center mt-8 text-sm text-neutral-600">
              Hiển thị {savedJobs.length} trong tổng số {pagination.totalElements} việc làm đã lưu
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default SavedJobsPage;
