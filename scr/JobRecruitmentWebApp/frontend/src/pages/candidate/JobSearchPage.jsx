import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Search, Filter, X, SlidersHorizontal } from 'lucide-react';
import JobCard from '../../components/common/JobCard';
import Pagination from '../../components/common/Pagination';
import jobService from '../../services/job.service';
import categoryService from '../../services/category.service';
import companyService from '../../services/company.service';
import { formatVND } from '../../utils/formatters';
import { VIETNAM_PROVINCES } from '../../data/provinces';

/**
 * JobSearchPage Component
 */
const JobSearchPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [currentPage, setCurrentPage] = useState(1);
  const [searchKeyword, setSearchKeyword] = useState(searchParams.get('keyword') || '');
  const [isMobileFilterOpen, setIsMobileFilterOpen] = useState(false);
  
  // Trạng thái bộ lọc
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedLocation, setSelectedLocation] = useState(searchParams.get('location') || '');
  const [selectedSalary, setSelectedSalary] = useState('');
  // Lưu ý: trường jobType không tồn tại trong sơ đồ cơ sở dữ liệu (SYSTEM_DESIGN.md Mục 3.1)
  // const [selectedJobType, setSelectedJobType] = useState([]);

  // Trạng thái dữ liệu API
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Dữ liệu bộ lọc động từ API
  const [categories, setCategories] = useState([]);
  const [categoriesLoading, setCategoriesLoading] = useState(true);

  // Danh sách tỉnh thành Việt Nam + tùy chọn Remote
  const locations = ['Tất cả', 'Remote', ...VIETNAM_PROVINCES];

  const salaryRanges = [
    'Tất cả',
    '0đ - 10 triệu',
    '10 - 20 triệu',
    '20 - 30 triệu',
    '30 - 50 triệu',
    '50 triệu+',
  ];

  // Lưu ý: trường jobType không tồn tại trong sơ đồ cơ sở dữ liệu (SYSTEM_DESIGN.md Mục 3.1)
  // const jobTypes = ['Toàn thời gian', 'Bán thời gian', 'Hợp đồng', 'Thực tập'];

  // Lấy categories từ API
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        setCategoriesLoading(true);
        const response = await categoryService.getAllCategories();
        // Thêm tùy chọn "Tất cả danh mục"
        const allCategoriesOption = { jcId: '', jcName: 'Tất cả' };
        setCategories([allCategoriesOption, ...response.data]);
      } catch (err) {
        console.error('Error loading categories:', err);
        setCategories([{ jcId: '', jcName: 'Tất cả' }]);
      } finally {
        setCategoriesLoading(false);
      }
    };
    fetchCategories();
  }, []);

  // Chuyển đổi khoảng lương sang giá trị min/max (đơn vị VND, lưu dưới dạng số nguyên)
  const parseSalaryRange = (range) => {
    if (!range || range === 'Tất cả') return { min: undefined, max: undefined };
    
    const rangeMap = {
      '0đ - 10 triệu': { min: 0, max: 10000000 },
      '10 - 20 triệu': { min: 10000000, max: 20000000 },
      '20 - 30 triệu': { min: 20000000, max: 30000000 },
      '30 - 50 triệu': { min: 30000000, max: 50000000 },
      '50 triệu+': { min: 50000000, max: undefined },
    };
    
    return rangeMap[range] || { min: undefined, max: undefined };
  };

  // Lấy danh sách việc làm từ API
  const fetchJobs = async () => {
    try {
      setLoading(true);
      setError(null);

      const { min, max } = parseSalaryRange(selectedSalary);

      const filters = {
        page: currentPage - 1, // API sử dụng trang bắt đầu từ 0
        size: 10,
        sort: 'createdAt,desc',
      };

      // Thêm bộ lọc chỉ khi chúng có giá trị
      if (searchKeyword && searchKeyword.trim()) {
        filters.keyword = searchKeyword.trim();
      }
      if (selectedCategory) {
        filters.jcId = selectedCategory; 
      }
      if (selectedLocation && selectedLocation !== 'Tất cả') {
        filters.location = selectedLocation;
      }
      // Thêm bộ lọc khoảng lương nếu backend hỗ trợ
      if (min !== undefined) filters.minSalary = min;
      if (max !== undefined) filters.maxSalary = max;
      // if (selectedJobType.length > 0) filters.jobType = selectedJobType.join(',');

      const response = await jobService.searchJobs(filters);
      
      // Chuyển đổi API response sang định dạng component
      const mappedJobs = response.data.content.map(job => ({
        jobId: job.jobId,
        title: job.jobTitle,
        company: job.companyName || 'Tên công ty',
        logoURL: job.logoURL || null,
        salary: job.jobSalary ? formatVND(job.jobSalary) : 'Thỏa thuận',
        location: job.jobLocation || 'Chưa cập nhật',
      }));

      setJobs(mappedJobs);
      setTotalPages(response.data.totalPages || 1);
      setTotalElements(response.data.totalElements || 0);
    } catch (err) {
      console.error('Error fetching jobs:', err);
      setError('Không thể tải danh sách việc làm. Vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
  };

  // Lấy danh sách việc làm khi component được mount hoặc bộ lọc thay đổi
  useEffect(() => {
    fetchJobs();
  }, [currentPage, selectedCategory, selectedLocation, selectedSalary, searchKeyword]);

  const handlePageChange = (page) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentPage(1); // Đặt lại về trang đầu tiên khi tìm kiếm mới
    // fetchJobs sẽ được gọi tự động bởi useEffect khi searchKeyword thay đổi
  };

  // Handle job type toggle (commented out - jobType not in database schema)
  // const handleJobTypeToggle = (type) => {
  //   setSelectedJobType(prev =>
  //     prev.includes(type)
  //       ? prev.filter(t => t !== type)
  //       : [...prev, type]
  //   );
  //   setCurrentPage(1); // Reset to first page
  // };

  const clearFilters = () => {
    setSelectedCategory('');
    setSelectedLocation('');
    setSelectedSalary('');
    // setSelectedJobType([]);
    setSearchKeyword('');
    setCurrentPage(1);
  };

  // Sidebar Filter Component
  const FilterSidebar = () => (
    <div className="bg-white rounded-lg shadow-md p-6 sticky top-24">
      {/* Filters Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-2">
          <SlidersHorizontal className="w-5 h-5 text-primary" />
          <h3 className="text-lg font-bold text-neutral-900">Bộ Lọc</h3>
        </div>
        <button
          onClick={clearFilters}
          className="text-sm text-primary hover:text-primary-600 font-medium"
        >
          Xóa tất cả
        </button>
      </div>

      {/* Job Category Filter */}
      <div className="mb-6">
        <label className="block text-sm font-semibold text-neutral-700 mb-3">
          Danh mục công việc
        </label>
        <select
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
          className="w-full px-4 py-2.5 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
          disabled={categoriesLoading}
        >
          {categories.map((category) => (
            <option key={category.jcId || 'all'} value={category.jcId}>
              {category.jcName}
            </option>
          ))}
        </select>
      </div>

      {/* Location Filter */}
      <div className="mb-6">
        <label className="block text-sm font-semibold text-neutral-700 mb-3">
          Địa điểm
        </label>
        <select
          value={selectedLocation}
          onChange={(e) => setSelectedLocation(e.target.value)}
          className="w-full px-4 py-2.5 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
        >
          {locations.map((location) => (
            <option key={location} value={location}>
              {location}
            </option>
          ))}
        </select>
      </div>

      {/* Salary Range Filter */}
      <div className="mb-6">
        <label className="block text-sm font-semibold text-neutral-700 mb-3">
          Mức lương
        </label>
        <select
          value={selectedSalary}
          onChange={(e) => setSelectedSalary(e.target.value)}
          className="w-full px-4 py-2.5 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
        >
          {salaryRanges.map((range) => (
            <option key={range} value={range}>
              {range}
            </option>
          ))}
        </select>
      </div>

      {/* Job Type Filter - REMOVED: jobType field does not exist in database schema */}
      {/* 
      <div className="mb-6">
        <label className="block text-sm font-semibold text-neutral-700 mb-3">
          Loại Công Việc
        </label>
        <div className="space-y-3">
          {jobTypes.map((type) => (
            <label key={type} className="flex items-center cursor-pointer group">
              <input
                type="checkbox"
                checked={selectedJobType.includes(type)}
                onChange={() => handleJobTypeToggle(type)}
                className="w-4 h-4 text-primary border-neutral-300 rounded focus:ring-2 focus:ring-primary cursor-pointer"
              />
              <span className="ml-3 text-sm text-neutral-700 group-hover:text-neutral-900">
                {type}
              </span>
            </label>
          ))}
        </div>
      </div>
      */}

      {/* Apply Filters Button */}
      <button className="w-full px-6 py-3 bg-primary text-white rounded-lg font-semibold hover:bg-primary-600 shadow-md hover:shadow-lg transition-all duration-200">
        Áp dụng bộ lọc
      </button>
    </div>
  );

  return (
    <div className="bg-neutral-50 min-h-screen py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Page Header */}
        <div className="mb-8">
          <h1 className="text-3xl lg:text-4xl font-bold text-neutral-900 mb-2">
            Tìm kiếm công việc hoàn hảo
          </h1>
          <p className="text-lg text-neutral-600">
            Khám phá hàng nghìn cơ hội việc làm từ các công ty hàng đầu
          </p>
        </div>

        {/* Search Bar */}
        <div className="bg-white rounded-lg shadow-md p-4 mb-8">
          <form onSubmit={handleSearch} className="flex flex-col md:flex-row gap-3">
            {/* Search Input */}
            <div className="flex-1 relative">
              <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 text-neutral-400 w-5 h-5" />
              <input
                type="text"
                placeholder="Tên công việc, từ khóa hoặc công ty..."
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                className="w-full pl-12 pr-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
              />
            </div>

            {/* Mobile Filter Toggle */}
            <button
              type="button"
              onClick={() => setIsMobileFilterOpen(!isMobileFilterOpen)}
              className="md:hidden px-6 py-3 bg-white border-2 border-primary text-primary rounded-lg font-semibold hover:bg-primary-50 transition-all flex items-center justify-center gap-2"
            >
              <Filter className="w-5 h-5" />
              Bộ lọc
            </button>

            {/* Search Button */}
            <button
              type="submit"
              className="px-8 py-3 bg-primary text-white rounded-lg font-semibold hover:bg-primary-600 shadow-md hover:shadow-lg transition-all duration-200"
            >
              Tìm kiếm
            </button>
          </form>
        </div>

        {/* Mobile Filter Drawer */}
        {isMobileFilterOpen && (
          <div className="fixed inset-0 bg-black bg-opacity-50 z-50 md:hidden">
            <div className="absolute right-0 top-0 h-full w-80 bg-white shadow-xl overflow-y-auto">
              <div className="p-6">
                <div className="flex items-center justify-between mb-6">
                  <h3 className="text-xl font-bold">Bộ lọc</h3>
                  <button
                    onClick={() => setIsMobileFilterOpen(false)}
                    className="p-2 hover:bg-neutral-100 rounded-lg"
                  >
                    <X className="w-6 h-6" />
                  </button>
                </div>
                <FilterSidebar />
              </div>
            </div>
          </div>
        )}

        {/* Main Content Layout */}
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* Sidebar Filters (Desktop) */}
          <aside className="hidden lg:block lg:col-span-1">
            <FilterSidebar />
          </aside>

          {/* Job List (Main Column) */}
          <main className="lg:col-span-3">
            {/* Results Info */}
            <div className="flex items-center justify-between mb-6">
              <p className="text-neutral-700 font-medium">
                {loading ? (
                  'Đang tải việc làm...'
                ) : (
                  <>
                    Hiển thị <span className="font-bold text-neutral-900">{jobs.length}</span> trong{' '}
                    <span className="font-bold text-neutral-900">{totalElements}</span> việc làm
                  </>
                )}
              </p>
              {/* <select className="px-4 py-2 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary text-sm">
                <option>Phù hợp nhất</option>
                <option>Mới nhất</option>
                <option>Lương: cao đến thấp</option>
                <option>Lương: thấp đến cao</option>
              </select> */}
            </div>

            {/* Job Cards - Vertical List */}
            {loading ? (
              <div className="space-y-4">
                {[...Array(5)].map((_, index) => (
                  <div key={index} className="bg-white rounded-xl p-6 shadow-md animate-pulse">
                    <div className="flex gap-4">
                      <div className="h-16 w-16 bg-neutral-200 rounded-lg flex-shrink-0"></div>
                      <div className="flex-1">
                        <div className="h-6 bg-neutral-200 rounded w-3/4 mb-2"></div>
                        <div className="h-4 bg-neutral-200 rounded w-1/2 mb-3"></div>
                        <div className="flex gap-2">
                          <div className="h-6 bg-neutral-200 rounded w-20"></div>
                          <div className="h-6 bg-neutral-200 rounded w-24"></div>
                          <div className="h-6 bg-neutral-200 rounded w-16"></div>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : error ? (
              <div className="bg-red-50 border border-red-200 text-red-700 px-6 py-8 rounded-lg text-center">
                <p className="font-medium text-lg mb-2">⚠️ {error}</p>
                <button
                  onClick={fetchJobs}
                  className="mt-4 px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
                >
                  Thử lại
                </button>
              </div>
            ) : jobs.length === 0 ? (
              <div className="bg-neutral-50 border border-neutral-200 text-neutral-600 px-6 py-12 rounded-lg text-center">
                <p className="text-lg font-medium mb-2">Không tìm thấy việc làm phù hợp.</p>
                <p className="mb-4">Thử điều chỉnh bộ lọc hoặc từ khóa tìm kiếm của bạn.</p>
                <button
                  onClick={clearFilters}
                  className="px-6 py-2 bg-primary text-white rounded-lg hover:bg-primary-600 transition-colors"
                >
                  Xóa tất cả bộ lọc
                </button>
              </div>
            ) : (
              <div className="space-y-4">
                {jobs.map((job) => (
                  <div key={job.jobId} className="transform hover:scale-[1.02] transition-transform duration-200">
                    <JobCard job={job} />
                  </div>
                ))}
              </div>
            )}

            {/* Pagination */}
            {!loading && !error && jobs.length > 0 && (
              <div className="mt-10">
                <Pagination
                  currentPage={currentPage}
                  totalPages={totalPages}
                  onPageChange={handlePageChange}
                />
              </div>
            )}
          </main>
        </div>
      </div>
    </div>
  );
};

export default JobSearchPage;
