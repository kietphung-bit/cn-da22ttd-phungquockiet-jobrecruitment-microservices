import React, { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import seekingPostService from '../../services/seekingPost.service';
import { formatCurrency } from '../../utils/formatters';
import { 
  MapPin, 
  Banknote, 
  Calendar, 
  Eye, 
  User,
  Lock,
  Search,
  SlidersHorizontal,
  Loader2
} from 'lucide-react';

/**
 * PublicTalentPage - Trang tìm kiếm ứng viên cho Public (Guest)
 * 
 * Trang này hiển thị danh sách ứng viên đang tìm việc cho người dùng chưa đăng nhập.
 * Thông tin ứng viên bị ẩn một phần để khuyến khích đăng nhập.
 */
const PublicTalentPage = () => {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedLocation, setSelectedLocation] = useState('Tất cả');
  const [selectedSkills, setSelectedSkills] = useState([]);
  const [selectedSalary, setSelectedSalary] = useState('Tất cả');

  // Phân trang
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const pageSize = 12;

  // Theo dõi nếu tải ban đầu đã hoàn thành để tránh gọi lại hai lần do StrictMode
  const [initialLoadDone, setInitialLoadDone] = useState(false);

  /**
   * Lấy danh sách bài đăng tìm kiếm từ API
   * Lưu ý: Backend chỉ hỗ trợ bộ lọc địa điểm và kỹ năng, KHÔNG hỗ trợ tìm kiếm từ khóa
   * Chỉ gọi khi: trang thay đổi, địa điểm thay đổi, hoặc người dùng nhấn nút Tìm kiếm
   */
  const fetchSeekingPosts = async () => {
    try {
      setLoading(true);
      const params = {
        page: currentPage,
        size: pageSize,
        sort: 'createdAt,desc'
      };

      if (selectedLocation !== 'Tất cả') {
        params.location = selectedLocation;
      }

      // Note: Backend does NOT support keyword search for seeking posts
      // Only location and skills filters are available
      // if (searchTerm) {
      //   params.keyword = searchTerm;
      // }

      const response = await seekingPostService.searchSeekingPosts(params);
      const data = response.data?.data || response.data;

      if (data.content) {
        setPosts(data.content);
        setTotalPages(data.totalPages);
      } else if (Array.isArray(data)) {
        setPosts(data);
      } else {
        setPosts([]);
      }
      
      if (!initialLoadDone) {
        setInitialLoadDone(true);
      }
    } catch (error) {
      setPosts([]);
    } finally {
      setLoading(false);
    }
  };

  // Lấy danh sách bài đăng tìm kiếm khi mount và khi trang/địa điểm thay đổi
  // AbortController để hủy các yêu cầu trước đó nếu có yêu cầu mới được kích hoạt
  useEffect(() => {
    // Bỏ qua nếu đây là lần gọi trùng lặp trong lần mount ban đầu (React.StrictMode)
    if (!initialLoadDone || currentPage > 0 || selectedLocation !== 'Tất cả') {
      fetchSeekingPosts();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage, selectedLocation]);

  // Chuyển đổi kỹ năng từ dữ liệu backend (phải định nghĩa trước khi useMemo sử dụng)
  const parseSkills = (skillsData) => {
    if (!skillsData) return [];
    if (Array.isArray(skillsData)) return skillsData;
    
    try {
      const parsed = JSON.parse(skillsData);
      if (Array.isArray(parsed)) return parsed;
    } catch (e) {
      // Không phải JSON
    }
    
    return skillsData.split(',').map(s => s.trim()).filter(s => s);
  };

  // Lưu locations và skills để tránh re-render không cần thiết
  const locations = useMemo(
    () => ['Tất cả', ...new Set(posts.map(post => post.location || post.desiredLocation).filter(Boolean))],
    [posts]
  );
  
  const allSkills = useMemo(
    () => [...new Set(posts.flatMap(post => parseSkills(post.skPostSkills)))].sort(),
    [posts]
  );
  
  const salaryRanges = [
    'Tất cả',
    '0đ - 10 triệu',
    '10 - 20 triệu',
    '20 - 30 triệu',
    '30 - 50 triệu',
    '50 triệu+',
  ];

  // Định dạng lương sử dụng hàm tiện ích
  const formatSalary = (salary) => {
    if (!salary) return 'Thỏa thuận';
    if (typeof salary === 'string' && isNaN(salary)) return salary;
    
    const numericSalary = Number(salary);
    if (!isNaN(numericSalary)) {
      return formatCurrency(numericSalary);
    }
    
    return salary;
  };

  // Định dạng ngày tháng
  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
  };

  // Lọc bài đăng dựa trên kỹ năng, từ khóa và mức lương (phía client)
  const filteredPosts = posts.filter(post => {
    // Bộ lọc kỹ năng
    if (selectedSkills.length > 0) {
      const postSkills = parseSkills(post.skPostSkills);
      if (!selectedSkills.every(skill => postSkills.includes(skill))) {
        return false;
      }
    }

    // Tìm kiếm từ khóa (tìm trong tiêu đề, kỹ năng, và giới thiệu)
    if (searchTerm.trim()) {
      const searchLower = searchTerm.toLowerCase();
      const postSkills = parseSkills(post.skPostSkills).join(' ').toLowerCase();
      const title = (post.skPostTitle || '').toLowerCase();
      const intro = (post.introduction || '').toLowerCase();
      const name = (post.candidateName || '').toLowerCase();
      
      const matchesSearch = title.includes(searchLower) || 
                           postSkills.includes(searchLower) || 
                           intro.includes(searchLower) ||
                           name.includes(searchLower);
      
      if (!matchesSearch) return false;
    }

    // Bộ lọc mức lương
    if (selectedSalary !== 'Tất cả') {
      const salaryStr = (post.desiredSalary || '').toString().toLowerCase();
      
      // Phân tích khoảng lương
      if (selectedSalary === '0đ - 10 triệu') {
        // Kiểm tra nếu lương chứa số nhỏ hơn 10 triệu
        if (!salaryStr.includes('triệu') || !salaryStr.match(/[0-9]/) || parseInt(salaryStr) > 10) {
          return false;
        }
      } else if (selectedSalary === '10 - 20 triệu') {
        if (!salaryStr.includes('10') && !salaryStr.includes('15') && !salaryStr.includes('20')) {
          return false;
        }
      } else if (selectedSalary === '20 - 30 triệu') {
        if (!salaryStr.includes('20') && !salaryStr.includes('25') && !salaryStr.includes('30')) {
          return false;
        }
      } else if (selectedSalary === '30 - 50 triệu') {
        if (!salaryStr.includes('30') && !salaryStr.includes('40') && !salaryStr.includes('50')) {
          return false;
        }
      } else if (selectedSalary === '50 triệu+') {
        if (!salaryStr.includes('50') && !salaryStr.match(/[5-9][0-9]/)) {
          return false;
        }
      }
    }

    return true;
  });

  const handleSkillToggle = (skill) => {
    setSelectedSkills(prev => 
      prev.includes(skill) 
        ? prev.filter(s => s !== skill)
        : [...prev, skill]
    );
  };

  const clearFilters = () => {
    setSearchTerm('');
    setSelectedLocation('Tất cả');
    setSelectedSkills([]);
    setSelectedSalary('Tất cả');
    setCurrentPage(0);
    setSearchTerm('');
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentPage(0);
    fetchSeekingPosts();
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

      {/* Skills Filter
      <div className="mb-6">
        <label className="block text-sm font-semibold text-neutral-700 mb-3">
          Kỹ năng
        </label>
        <div className="space-y-2 max-h-48 overflow-y-auto">
          {allSkills.map((skill) => (
            <label key={skill} className="flex items-center cursor-pointer group">
              <input
                type="checkbox"
                checked={selectedSkills.includes(skill)}
                onChange={() => handleSkillToggle(skill)}
                className="w-4 h-4 text-primary border-neutral-300 rounded focus:ring-2 focus:ring-primary cursor-pointer"
              />
              <span className="ml-3 text-sm text-neutral-700 group-hover:text-neutral-900">
                {skill}
              </span>
            </label>
          ))}
        </div>
      </div> */}

      {/* Salary Range Filter */}
      <div className="mb-6">
        <label className="block text-sm font-semibold text-neutral-700 mb-3">
          Mức lương mong muốn
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
    </div>
  );

  return (
    <div className="bg-neutral-50 min-h-screen py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Page Header */}
        <div className="mb-8">
          <h1 className="text-3xl lg:text-4xl font-bold text-neutral-900 mb-2">
            Tìm kiếm ứng viên tài năng
          </h1>
          <p className="text-lg text-neutral-600">
            Kết nối với các ứng viên chất lượng đang tìm kiếm cơ hội mới
          </p>
        </div>

          {/* Search Bar */}
          <div className="bg-white rounded-lg shadow-md p-4 mb-8">
            <form onSubmit={handleSearch} className="flex flex-col md:flex-row gap-3">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-neutral-400 w-5 h-5" />
                <input
                  type="text"
                  placeholder="Tìm theo kỹ năng, vị trí hoặc tên ứng viên..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent text-neutral-900"
                />
              </div>
              <button
                type="submit"
                className="px-6 py-3 bg-primary text-white rounded-lg font-semibold hover:bg-primary-600 shadow-md hover:shadow-lg transition-all duration-200"
              >
                Tìm kiếm
              </button>
            </form>
          </div>

          {/* Main Layout: Sidebar + Candidate List */}
          <div className="flex flex-col lg:flex-row gap-8">
            {/* Left Sidebar - Filters */}
            <aside className="lg:w-80 flex-shrink-0">
              <FilterSidebar />
            </aside>

            {/* Right Main Content - Candidate List */}
            <main className="flex-1">
              {/* Results Header */}
              <div className="mb-6">
                <h2 className="text-xl font-semibold text-neutral-900">
                  Tìm thấy {filteredPosts.length} ứng viên
                </h2>
                <p className="text-sm text-neutral-600 mt-1">
                  Đăng nhập để xem thông tin liên hệ và mời ứng tuyển
                </p>
              </div>

              {/* Loading State */}
              {loading && (
                <div className="bg-white rounded-lg shadow-md p-12">
                  <div className="flex flex-col items-center justify-center">
                    <Loader2 className="w-12 h-12 text-primary animate-spin mb-4" />
                    <p className="text-neutral-600">Đang tải danh sách ứng viên...</p>
                  </div>
                </div>
              )}

              {/* Candidate Cards List */}
              {!loading && (
                <div className="space-y-4">
                  {filteredPosts.length === 0 ? (
                    <div className="bg-white rounded-lg shadow-md p-12 text-center">
                      <User className="w-16 h-16 text-neutral-300 mx-auto mb-4" />
                      <h3 className="text-xl font-semibold text-neutral-900 mb-2">
                        Không tìm thấy ứng viên phù hợp
                      </h3>
                      <p className="text-neutral-600">
                        Thử điều chỉnh bộ lọc hoặc tìm kiếm với từ khóa khác
                      </p>
                    </div>
                  ) : (
                    filteredPosts.map(post => {
                      const skills = parseSkills(post.skPostSkills);
                      
                      return (
                        <div 
                          key={post.skPostId} 
                          className="bg-white rounded-lg shadow-md hover:shadow-lg transition-all duration-200 border border-neutral-200 hover:border-primary"
                        >
                          <div className="p-6">
                            <div className="flex gap-6">
                              {/* Left: Avatar */}
                              <div className="flex-shrink-0">
                                <div className="w-20 h-20 rounded-full bg-neutral-200 flex items-center justify-center">
                                  <User className="w-10 h-10 text-neutral-400" />
                                </div>
                              </div>

                              {/* Right: Content */}
                              <div className="flex-1 min-w-0">
                                {/* Name and Views (MASKED by backend) */}
                                <div className="flex items-start justify-between gap-4 mb-3">
                                  <div className="flex-1">
                                    <h3 className="text-xl font-bold text-neutral-900 flex items-center gap-2 mb-1">
                                      {post.candidateName || 'Ứng viên ***'}
                                      <Lock className="w-4 h-4 text-neutral-400" />
                                    </h3>
                                    <div className="flex items-center gap-4 text-sm text-neutral-600">
                                      {/* Views icon removed - no database column exists yet */}
                                      <div className="flex items-center gap-1">
                                        <Calendar className="w-4 h-4" />
                                        <span>Đăng {formatDate(post.createdDate)}</span>
                                      </div>
                                    </div>
                                  </div>
                                </div>

                                {/* Title */}
                                <h4 className="font-semibold text-lg text-neutral-900 mb-3">
                                  {post.skPostTitle}
                                </h4>

                                {/* Skills */}
                                <div className="flex flex-wrap gap-2 mb-3">
                                  {skills.slice(0, 5).map((skill, index) => (
                                    <span 
                                      key={index}
                                      className="px-3 py-1 bg-blue-50 text-blue-700 text-sm font-medium rounded-full"
                                    >
                                      {skill}
                                    </span>
                                  ))}
                                  {skills.length > 5 && (
                                    <span className="px-3 py-1 bg-neutral-100 text-neutral-600 text-sm font-medium rounded-full">
                                      +{skills.length - 5} kỹ năng khác
                                    </span>
                                  )}
                                </div>

                                {/* Location and Salary */}
                                <div className="flex flex-wrap gap-4 text-neutral-700">
                                  <div className="flex items-center gap-2">
                                    <MapPin className="w-4 h-4 text-primary" />
                                    <span>{post.location || post.desiredLocation || 'Không xác định'}</span>
                                  </div>
                                  <div className="flex items-center gap-2">
                                    <Banknote className="w-4 h-4 text-primary" />
                                    <span className="font-medium">{formatSalary(post.desiredSalary)}</span>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      );
                    })
                  )}
                </div>
              )}

              {/* Pagination */}
              {!loading && filteredPosts.length > 0 && totalPages > 1 && (
                <div className="mt-8 flex justify-center gap-2">
                  <button
                    onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                    disabled={currentPage === 0}
                    className="px-4 py-2 bg-white border border-neutral-300 rounded-lg font-medium text-neutral-700 hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Trước
                  </button>
                  <span className="px-4 py-2 bg-white border border-neutral-300 rounded-lg font-medium text-neutral-700">
                    Trang {currentPage + 1} / {totalPages}
                  </span>
                  <button
                    onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                    disabled={currentPage >= totalPages - 1}
                    className="px-4 py-2 bg-white border border-neutral-300 rounded-lg font-medium text-neutral-700 hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Sau
                  </button>
                </div>
              )}
            </main>
          </div>

          {/* Login CTA Section */}
          <div className="mt-16 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl shadow-2xl overflow-hidden">
            <div className="px-8 py-12 text-center text-white">
              <h2 className="text-3xl font-bold mb-4">
                Mở khóa toàn bộ thông tin ứng viên
              </h2>
              <p className="text-xl text-blue-100 mb-8 max-w-2xl mx-auto">
                Đăng nhập để xem thông tin liên hệ đầy đủ, mời ứng viên ứng tuyển và kết nối với tài năng
              </p>
              <div className="flex gap-4 justify-center">
                <Link
                  to="/login"
                  className="px-8 py-4 bg-white text-blue-600 rounded-lg font-bold hover:bg-blue-50 shadow-lg hover:shadow-xl transition-all duration-200"
                >
                  Đăng nhập ngay
                </Link>
                <Link
                  to="/register"
                  className="px-8 py-4 bg-blue-500 text-white rounded-lg font-bold hover:bg-blue-400 border-2 border-white shadow-lg hover:shadow-xl transition-all duration-200"
                >
                  Đăng ký tài khoản
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
  );
};

export default PublicTalentPage;
