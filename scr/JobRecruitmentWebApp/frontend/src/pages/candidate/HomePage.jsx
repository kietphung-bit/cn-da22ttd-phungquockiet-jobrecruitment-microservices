import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Search, MapPin, Briefcase, TrendingUp } from 'lucide-react';
import JobCard from '../../components/common/JobCard';
import CompanyCard from '../../components/common/CompanyCard';
import jobService from '../../services/job.service';
import companyService from '../../services/company.service';
import { formatVND, formatRelativeDate } from '../../utils/formatters';

/**
 * HomePage Component
 * Based on Wireframe Specification:
 * - Hero Section: Centered with search bar
 * - Featured Companies: Horizontal scrolling/grid
 * - Hot Jobs: 4-column grid on desktop
 */
const HomePage = () => {
  const navigate = useNavigate();
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchLocation, setSearchLocation] = useState('');
  const [hotJobs, setHotJobs] = useState([]);
  const [featuredCompanies, setFeaturedCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [companiesLoading, setCompaniesLoading] = useState(true);
  const [error, setError] = useState(null);

  // Fetch hot jobs on component mount
  useEffect(() => {
    const fetchHotJobs = async () => {
      try {
        setLoading(true);
        const response = await jobService.getHotJobs(8);
        
        // Map API response to component format
        const mappedJobs = response.data.content.map(job => ({
          jobId: job.jobId,
          title: job.jobTitle,
          company: job.companyName || 'Tên công ty',
          logoURL: job.logoURL || null,
          salary: job.jobSalary ? formatVND(job.jobSalary) : 'Thỏa thuận',
          location: job.jobLocation || 'Chưa cập nhật',
          postedAt: job.createdAt,
        }));
        
        setHotJobs(mappedJobs);
        setError(null);
      } catch (err) {
        console.error('Error loading hot jobs:', err);
        setError('Không thể tải việc làm nổi bật. Vui lòng thử lại sau.');
      } finally {
        setLoading(false);
      }
    };

    fetchHotJobs();
  }, []);

  // Fetch featured companies on component mount
  useEffect(() => {
    const fetchFeaturedCompanies = async () => {
      try {
        setCompaniesLoading(true);
        const response = await companyService.getFeaturedCompanies(5);
        
        // Map API response to component format
        const mappedCompanies = response.data.content.map(company => ({
          companyId: company.companyId,
          name: company.companyName,
          logo: company.logoURL || null,
          jobCount: 0, // Will be updated if API provides this
        }));
        
        setFeaturedCompanies(mappedCompanies);
      } catch (err) {
        console.error('Error loading featured companies:', err);
      } finally {
        setCompaniesLoading(false);
      }
    };

    fetchFeaturedCompanies();
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    // Navigate to JobSearchPage with search params
    const params = new URLSearchParams();
    if (searchKeyword) params.append('keyword', searchKeyword);
    if (searchLocation) params.append('location', searchLocation);
    navigate(`/jobs?${params.toString()}`);
  };

  return (
    <div className="bg-white">
      {/* Hero Section */}
      <section className="relative bg-gradient-to-br from-primary-600 via-primary-500 to-primary-400 text-white py-24 lg:py-32 overflow-hidden">
        {/* Background Pattern */}
        <div className="absolute inset-0 opacity-10">
          <div className="absolute inset-0" style={{
            backgroundImage: 'radial-gradient(circle at 2px 2px, white 1px, transparent 0)',
            backgroundSize: '48px 48px'
          }}></div>
        </div>

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="text-center max-w-4xl mx-auto">
            {/* Main Heading */}
            <h1 className="text-3xl sm:text-4xl lg:text-5xl font-bold mb-6 leading-tight">
              Tìm kiếm cơ hội nghề nghiệp mơ ước
            </h1>
            
            {/* Description */}
            <p className="text-lg sm:text-xl text-white/90 mb-10 leading-relaxed">
              Khám phá hàng nghìn cơ hội việc làm từ các công ty hàng đầu. <br />
              Bước tiếp theo trong sự nghiệp của bạn bắt đầu từ đây.
            </p>

            {/* Search Bar */}
            <form onSubmit={handleSearch} className="bg-white rounded-2xl shadow-2xl p-4 max-w-4xl mx-auto">
              <div className="flex flex-col md:flex-row gap-3">
                {/* Keyword Input */}
                <div className="flex-1 relative">
                  <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 text-neutral-400 w-5 h-5" />
                  <input
                    type="text"
                    placeholder="Tên công việc, từ khóa hoặc công ty..."
                    value={searchKeyword}
                    onChange={(e) => setSearchKeyword(e.target.value)}
                    className="w-full pl-12 pr-4 py-4 text-neutral-900 rounded-xl border border-neutral-200 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                  />
                </div>

                {/* Location Input */}
                <div className="flex-1 relative">
                  <MapPin className="absolute left-4 top-1/2 transform -translate-y-1/2 text-neutral-400 w-5 h-5" />
                  <input
                    type="text"
                    placeholder="Thành phố, khu vực hoặc từ xa"
                    value={searchLocation}
                    onChange={(e) => setSearchLocation(e.target.value)}
                    className="w-full pl-12 pr-4 py-4 text-neutral-900 rounded-xl border border-neutral-200 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                  />
                </div>

                {/* Search Button */}
                <button
                  type="submit"
                  className="px-8 py-4 bg-primary text-white rounded-xl font-semibold hover:bg-primary-600 shadow-lg hover:shadow-xl transition-all duration-200"
                >
                  Tìm việc
                </button>
              </div>
            </form>

            {/* Quick Stats */}
            <div className="mt-12 grid grid-cols-3 gap-8 max-w-2xl mx-auto">
              <div>
                <div className="text-3xl font-bold">10,000+</div>
                <div className="text-white/80 text-sm mt-1">Việc Làm</div>
              </div>
              <div>
                <div className="text-3xl font-bold">5,000+</div>
                <div className="text-white/80 text-sm mt-1">Công Ty</div>
              </div>
              <div>
                <div className="text-3xl font-bold">50,000+</div>
                <div className="text-white/80 text-sm mt-1">Ứng Viên</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Featured Companies Section */}
      <section className="py-16 bg-neutral-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Section Header */}
          <div className="text-center mb-12">
            <h2 className="text-3xl lg:text-4xl font-bold text-neutral-900 mb-4">
              Công ty nổi bật
            </h2>
            <p className="text-lg text-neutral-600 max-w-2xl mx-auto">
              Khám phá cơ hội từ các tổ chức hàng đầu đang tích cực tuyển dụng nhân tài
            </p>
          </div>

          {/* Companies Grid */}
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
            {featuredCompanies.map((company) => (
              <CompanyCard key={company.companyId} company={company} />
            ))}
          </div>

          {/* View All Button */}
          <div className="text-center mt-10">
            <Link
              to="/companies"
              className="inline-flex items-center gap-2 px-8 py-3 bg-white border-2 border-primary text-primary rounded-lg font-semibold hover:bg-primary hover:text-white shadow-md hover:shadow-lg transition-all duration-200"
            >
              <Briefcase className="w-5 h-5" />
              Xem tất cả công ty
            </Link>
          </div>
        </div>
      </section>

      {/* Hot Jobs Section */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Section Header */}
          <div className="flex items-center justify-between mb-12">
            <div>
              <div className="flex items-center gap-3 mb-3">
                <TrendingUp className="w-8 h-8 text-primary" />
                <h2 className="text-3xl lg:text-4xl font-bold text-neutral-900">
                  Việc làm nổi bật
                </h2>
              </div>
              <p className="text-lg text-neutral-600">
                Các cơ hội việc làm đang thịnh hành bạn không nên bỏ lỡ
              </p>
            </div>
            <Link
              to="/jobs"
              className="hidden md:inline-flex items-center gap-2 text-primary font-semibold hover:text-primary-600 transition-colors"
            >
              Xem tất cả việc làm
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </Link>
          </div>

          {/* Jobs Grid - 4 columns on desktop */}
          {loading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {[...Array(8)].map((_, index) => (
                <div key={index} className="bg-white rounded-xl p-6 shadow-md animate-pulse">
                  <div className="h-12 w-12 bg-neutral-200 rounded-lg mb-4"></div>
                  <div className="h-6 bg-neutral-200 rounded w-3/4 mb-2"></div>
                  <div className="h-4 bg-neutral-200 rounded w-1/2 mb-4"></div>
                  <div className="h-4 bg-neutral-200 rounded w-full mb-2"></div>
                  <div className="h-4 bg-neutral-200 rounded w-5/6"></div>
                </div>
              ))}
            </div>
          ) : error ? (
            <div className="bg-red-50 border border-red-200 text-red-700 px-6 py-4 rounded-lg text-center">
              <p className="font-medium">⚠️ {error}</p>
            </div>
          ) : hotJobs.length === 0 ? (
            <div className="bg-neutral-50 border border-neutral-200 text-neutral-600 px-6 py-8 rounded-lg text-center">
              <p className="text-lg font-medium">Hiện tại chưa có việc làm nổi bật.</p>
              <p className="mt-2">Hãy quay lại sau để khám phá cơ hội mới!</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {hotJobs.map((job) => (
                <JobCard key={job.jobId} job={job} />
              ))}
            </div>
          )}

          {/* Mobile View All Button */}
          <div className="text-center mt-10 md:hidden">
            <Link
              to="/jobs"
              className="inline-flex items-center gap-2 px-8 py-3 bg-primary text-white rounded-lg font-semibold hover:bg-primary-600 shadow-md hover:shadow-lg transition-all duration-200"
            >
              Xem tất cả việc làm
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </Link>
          </div>
        </div>
      </section>

      {/* Call to Action Section */}
      <section className="py-20 bg-gradient-to-r from-primary-600 to-primary-400 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl lg:text-4xl font-bold mb-6">
            Sẵn sàng bước tiếp trong sự nghiệp của bạn?
          </h2>
          <p className="text-xl text-white/90 mb-10 max-w-2xl mx-auto">
            Tham gia cùng hàng ngàn chuyên gia đã tìm thấy công việc mơ ước qua nền tảng của chúng tôi
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to="/register"
              className="px-8 py-4 bg-white text-primary rounded-lg font-semibold hover:bg-neutral-50 shadow-lg hover:shadow-xl transition-all duration-200"
            >
              Tạo tài khoản miễn phí
            </Link>
            <Link
              to="/jobs"
              className="px-8 py-4 bg-transparent border-2 border-white text-white rounded-lg font-semibold hover:bg-white hover:text-primary transition-all duration-200"
            >
              Xem việc l  àm
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
};

export default HomePage;
