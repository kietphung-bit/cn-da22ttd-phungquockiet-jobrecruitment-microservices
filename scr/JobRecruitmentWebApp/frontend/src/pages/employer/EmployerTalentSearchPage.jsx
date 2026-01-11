import React, { useState, useEffect } from 'react';
import TalentDetailModal from '../../components/features/talent/TalentDetailModal';
import seekingPostService from '../../services/seekingPost.service';
import { 
  MapPin, 
  Banknote, 
  Calendar, 
  Eye, 
  Mail,
  Phone,
  Search,
  Filter,
  X,
  Send,
  User,
  Loader2,
  CheckCircle2
} from 'lucide-react';
import { toast } from 'react-hot-toast';

// Hàm định dạng lương
const formatSalary = (salary) => {
  if (!salary || salary === 0) return 'Thỏa thuận';
  if (typeof salary === 'string') return salary;
  return new Intl.NumberFormat('vi-VN', { 
    style: 'currency', 
    currency: 'VND' 
  }).format(salary);
};

/**
 * Định dạng chuỗi địa điểm
 * Xử lý: Chuỗi, Mảng, Chuỗi JSON Mảng, null/undefined
 */
const formatLocation = (location) => {
  if (!location) return 'Toàn quốc';
  
  // Nếu đã là chuỗi (không phải chuỗi JSON mảng)
  if (typeof location === 'string') {
    // Kiểm tra nếu là chuỗi JSON mảng như "['Hanoi', 'HCM']"
    if (location.startsWith('[') && location.endsWith(']')) {
      try {
        const parsed = JSON.parse(location);
        if (Array.isArray(parsed) && parsed.length > 0) {
          return parsed.join(', ');
        }
      } catch (e) {
        // Nếu phân tích cú pháp thất bại, trả về nguyên bản
        return location;
      }
    }
    return location;
  }
  
  // Nếu là mảng
  if (Array.isArray(location)) {
    return location.length > 0 ? location.join(', ') : 'Toàn quốc';
  }
  
  return 'Toàn quốc';
};

/**
 * EmployerTalentSearchPage - Trang tìm kiếm ứng viên cho Nhà tuyển dụng
 * 
 * Trang này hiển thị danh sách ứng viên đang tìm việc cho nhà tuyển dụng đã đăng nhập.
 * Hiển thị đầy đủ thông tin và có thể mời ứng tuyển.
 * 
 * Tính năng:
 * - Grid layout hiển thị danh sách ứng viên
 * - Hiển thị tên đầy đủ ứng viên
 * - Sidebar filter (Location, Skills)
 * - Hiển thị đầy đủ thông tin liên hệ
 * - Nút "Mời ứng tuyển" cho mỗi ứng viên
 * - Search và filter nâng cao
 */
const EmployerTalentSearchPage = () => {
  // Tìm kiếm và bộ lọc
  const [keyword, setKeyword] = useState('');
  const [location, setLocation] = useState('');
  const [skills, setSkills] = useState('');
  const [showFilters, setShowFilters] = useState(true);
  
  // Dữ liệu
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [totalResults, setTotalResults] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  
  // Trạng thái cho modal
  const [selectedPost, setSelectedPost] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  
  // Trạng thái theo dõi các ứng viên đã được mời
  const [invitedPosts, setInvitedPosts] = useState(new Set());

  /**
   * Lấy danh sách bài đăng từ API
   * Gọi khi component mount và khi bộ lọc thay đổi
   */
  const fetchPosts = async () => {
    try {
      setLoading(true);
      
      // Xây dựng đối tượng params - Backend mong đợi: location, skills (không có từ khóa!)
      const params = {
        page: currentPage,
        size: 10,
        sort: 'createdAt,desc'
      };
      
      // Thêm bộ lọc nếu có
      if (location) params.location = location;
      if (skills) params.skills = skills;
      
      const response = await seekingPostService.searchSeekingPosts(params);
      const data = response.data?.data || response.data;
      
      // Xử lý phản hồi phân trang
      if (data.content) {
        setPosts(data.content);
        setTotalResults(data.totalElements || 0);
        setTotalPages(data.totalPages || 0);
      } else if (Array.isArray(data)) {
        setPosts(data);
        setTotalResults(data.length);
        setTotalPages(1);
      } else {
        setPosts([]);
        setTotalResults(0);
        setTotalPages(0);
      }
    } catch (error) {
      toast.error('Không thể tải danh sách ứng viên');
      setPosts([]);
    } finally {
      setLoading(false);
    }
  };

  // Lấy bài đăng khi component mount và khi bộ lọc thay đổi
  useEffect(() => {
    fetchPosts();
  }, [location, skills, currentPage]);

  /**
   * Xử lý khi nhấn nút tìm kiếm
   * Lưu ý: Backend chưa có endpoint tìm kiếm theo từ khóa
   * Chúng ta sẽ lọc trên frontend tạm thời
   */
  const handleSearch = () => {
    // Cảnh báo re-fetch (sẽ áp dụng bộ lọc location/skills)
    fetchPosts();
  };

  /**
   * Xử lý khi nhấn Enter trong ô tìm kiếm
   */
  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  /**
   * Xử lý khi mời ứng viên
   * Trong thực tế, sẽ gọi API endpoint
   */
  const handleInvite = async (postId, candidateName) => {
    try {
      // Giả lập chờ API
      await new Promise(resolve => setTimeout(resolve, 800));
      
      // Đánh dấu đã mời
      setInvitedPosts(prev => new Set(prev).add(postId));
      
      // Hiển thị thông báo thành công
      toast.success(`Đã gửi lời mời phỏng vấn thành công đến ${candidateName}!`);
      
    } catch (error) {
      toast.error('Gửi lời mời thất bại. Vui lòng thử lại!');
      throw error;
    }
  };
  
  /**
   * Mở modal chi tiết ứng viên
   */
  const handleViewDetail = (post) => {
    setSelectedPost(post);
    setIsModalOpen(true);
  };
  
  /**
   * Đóng modal chi tiết ứng viên
   */
  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedPost(null);
  };

  const clearFilters = () => {
    setLocation('');
    setSkills('');
    setKeyword('');
    setCurrentPage(0);
  };

  // Lọc bài đăng trên frontend theo từ khóa (vì backend chưa hỗ trợ)
  const filteredPosts = keyword
    ? posts.filter(post => 
        post.title?.toLowerCase().includes(keyword.toLowerCase()) ||
        post.candidateName?.toLowerCase().includes(keyword.toLowerCase()) ||
        (Array.isArray(post.skills) && post.skills.some(s => s.toLowerCase().includes(keyword.toLowerCase())))
      )
    : posts;

  return (
    <>
      {/* Header */}
      <div className="bg-white border-b border-gray-200 mb-6">
        <div className="px-6 py-6">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Tìm kiếm ứng viên
          </h1>
          <p className="text-gray-600">
            Khám phá và kết nối với các ứng viên tài năng đang tìm kiếm cơ hội
          </p>
        </div>
      </div>

      <div className="px-6 pb-6">
        <div className="flex gap-6">
            {/* Sidebar Filters */}
            {showFilters && (
              <div className="w-80 flex-shrink-0">
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 sticky top-6">
                  {/* Filter Header */}
                  <div className="p-4 border-b border-gray-200 flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Filter className="w-5 h-5 text-blue-600" />
                      <h3 className="font-semibold text-gray-900">Bộ lọc</h3>
                    </div>
                    <button
                      onClick={clearFilters}
                      className="text-sm text-blue-600 hover:text-blue-700 font-medium"
                    >
                      Xóa tất cả
                    </button>
                  </div>

                  <div className="p-4 space-y-6 max-h-[calc(100vh-200px)] overflow-y-auto">
                    {/* Location Filter */}
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-3">
                        Địa điểm
                      </label>
                      <input
                        type="text"
                        placeholder="VD: Hồ Chí Minh, Hà Nội..."
                        value={location}
                        onChange={(e) => setLocation(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                      />
                      <p className="mt-2 text-xs text-gray-500">
                        Nhập địa điểm để lọc ứng viên
                      </p>
                    </div>

                    {/* Skills Filter (REMOVED CHECKBOXES) */}
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-3">
                        Kỹ năng
                      </label>
                      <input
                        type="text"
                        placeholder="VD: Java, Spring Boot, React..."
                        value={skills}
                        onChange={(e) => setSkills(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                      />
                      <p className="mt-2 text-xs text-gray-500">
                        Nhập kỹ năng để lọc ứng viên
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Main Content */}
            <div className="flex-1 min-w-0">
              {/* Search and Controls */}
              <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 mb-6">
                <div className="flex gap-3">
                  <div className="flex-1 relative">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                    <input
                      type="text"
                      placeholder="Tìm theo tên, kỹ năng, vị trí..."
                      value={keyword}
                      onChange={(e) => setKeyword(e.target.value)}
                      onKeyPress={handleKeyPress}
                      className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                  <button
                    onClick={handleSearch}
                    className="px-6 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium flex items-center gap-2 transition-colors"
                  >
                    <Search className="w-5 h-5" />
                    Tìm kiếm
                  </button>
                  <button
                    onClick={() => setShowFilters(!showFilters)}
                    className={`px-4 py-2.5 rounded-lg font-medium flex items-center gap-2 ${
                      showFilters 
                        ? 'bg-blue-600 text-white' 
                        : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                    }`}
                  >
                    <Filter className="w-5 h-5" />
                    {showFilters ? 'Ẩn bộ lọc' : 'Hiện bộ lọc'}
                  </button>
                </div>

                {/* Active Filters */}
                {(location || skills) && (
                  <div className="mt-3 flex flex-wrap gap-2">
                    {location && (
                      <span className="inline-flex items-center gap-1 px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-sm">
                        <MapPin className="w-3 h-3" />
                        {location}
                        <button
                          onClick={() => setLocation('')}
                          className="ml-1 hover:text-blue-900"
                        >
                          <X className="w-3 h-3" />
                        </button>
                      </span>
                    )}
                    {skills && (
                      <span className="inline-flex items-center gap-1 px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-sm">
                        {skills}
                        <button
                          onClick={() => setSkills('')}
                          className="ml-1 hover:text-blue-900"
                        >
                          <X className="w-3 h-3" />
                        </button>
                      </span>
                    )}
                  </div>
                )}
              </div>

              {/* Results Count */}
              <div className="mb-4">
                <p className="text-gray-600">
                  {loading ? (
                    'Đang tải...'
                  ) : (
                    <>
                      Tìm thấy <span className="font-semibold text-gray-900">{filteredPosts.length}</span> ứng viên
                    </>
                  )}
                </p>
              </div>

              {/* Loading State */}
              {loading && (
                <div className="flex justify-center items-center py-16">
                  <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
                </div>
              )}

              {/* Candidate Cards Grid */}
              {!loading && (
                <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
                {filteredPosts.map(post => {
                  const isInvited = invitedPosts.has(post.id);
                  
                  return (
                  <div 
                    key={post.id} 
                    onClick={() => handleViewDetail(post)}
                    className="bg-white rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow duration-200 cursor-pointer"
                  >
                    {/* Card Header */}
                    <div className="p-6 border-b border-gray-100">
                      <div className="flex items-start gap-4">
                        <div className="w-16 h-16 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
                          <User className="w-8 h-8 text-blue-600" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <h3 className="font-bold text-lg text-gray-900 mb-1">
                            {post.candidateName}
                          </h3>
                          <div className="flex items-center gap-4 text-sm text-gray-600 mb-2">
                            <div className="flex items-center gap-1">
                              <Eye className="w-4 h-4" />
                              <span>{post.views} lượt xem</span>
                            </div>
                            <div className="flex items-center gap-1">
                              <Calendar className="w-4 h-4" />
                              <span>{post.createdDate}</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Card Body */}
                    <div className="p-6">
                      <h4 className="font-semibold text-gray-900 mb-3 line-clamp-2">
                        {post.title}
                      </h4>

                      {/* Info Grid */}
                      <div className="grid grid-cols-2 gap-3 mb-4">
                        <div className="flex items-center gap-2 text-sm">
                          <Banknote className="w-4 h-4 text-green-600 flex-shrink-0" />
                          <span className="font-medium text-green-600">
                            {formatSalary(post.desiredSalary)}
                          </span>
                        </div>
                        <div className="flex items-center gap-2 text-sm text-gray-600">
                          <MapPin className="w-4 h-4 flex-shrink-0" />
                          <span className="truncate">{formatLocation(post.location)}</span>
                        </div>
                      </div>

                      {/* Skills */}
                      {post.skills && post.skills.length > 0 && (
                        <div className="mb-4">
                          <div className="flex flex-wrap gap-2">
                            {post.skills.slice(0, 5).map((skill, index) => (
                              <span 
                                key={index}
                                className="px-2.5 py-1 bg-blue-50 text-blue-700 text-xs font-medium rounded"
                              >
                                {skill}
                              </span>
                            ))}
                            {post.skills.length > 5 && (
                              <span className="px-2.5 py-1 bg-gray-100 text-gray-600 text-xs font-medium rounded">
                                +{post.skills.length - 5} kỹ năng khác
                              </span>
                            )}
                          </div>
                        </div>
                      )}

                      {/* Introduction */}
                      <div 
                        className="text-sm text-gray-600 mb-4 line-clamp-3 prose prose-sm max-w-none"
                        dangerouslySetInnerHTML={{ __html: post.introduction || '' }}
                      />

                      {/* Contact Info - Displayed for employers */}
                      {post.candidateEmail && post.candidatePhone && (
                        <div className="bg-gray-50 rounded-lg p-4 mb-4 space-y-2">
                          <div className="flex items-center gap-2 text-sm text-gray-700">
                            <Mail className="w-4 h-4 text-gray-400" />
                            <span>{post.candidateEmail}</span>
                          </div>
                          <div className="flex items-center gap-2 text-sm text-gray-700">
                            <Phone className="w-4 h-4 text-gray-400" />
                            <span>{post.candidatePhone}</span>
                          </div>
                        </div>
                      )}

                      {/* Action Buttons */}
                      <div className="space-y-2">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleViewDetail(post);
                          }}
                          className="w-full py-3 rounded-lg font-medium flex items-center justify-center gap-2 transition-colors bg-gray-100 hover:bg-gray-200 text-gray-700"
                        >
                          <Eye className="w-5 h-5" />
                          Xem chi tiết
                        </button>
                        <button
                          onClick={(e) => {
                            e.stopPropagation(); // Prevent card click
                            if (!isInvited) {
                              handleInvite(post.id, post.candidateName);
                            }
                          }}
                          disabled={isInvited}
                          className={`w-full py-3 rounded-lg font-medium flex items-center justify-center gap-2 transition-colors ${
                            isInvited
                              ? 'bg-green-600 text-white cursor-not-allowed'
                              : 'bg-blue-600 hover:bg-blue-700 text-white'
                          }`}
                        >
                          {isInvited ? (
                            <>
                              <CheckCircle2 className="w-5 h-5" />
                              Đã mời
                            </>
                          ) : (
                            <>
                              <Send className="w-5 h-5" />
                              Mời ứng tuyển
                            </>
                          )}
                        </button>
                      </div>
                    </div>
                  </div>
                  );
                })}
                </div>
              )}

              {/* Empty State */}
              {!loading && filteredPosts.length === 0 && (
                <div className="text-center py-16">
                  <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-12 max-w-md mx-auto">
                    <User className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                    <h3 className="text-xl font-semibold text-gray-900 mb-2">
                      Không tìm thấy ứng viên
                    </h3>
                    <p className="text-gray-600 mb-4">
                      Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc
                    </p>
                    <button
                      onClick={clearFilters}
                      className="text-blue-600 hover:text-blue-700 font-medium"
                    >
                      Xóa bộ lọc
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
    
      {/* Talent Detail Modal - Rendered outside layout to avoid duplication */}
      <TalentDetailModal
      post={selectedPost}
      isOpen={isModalOpen}
      onClose={handleCloseModal}
      onInvite={(postId) => handleInvite(postId, selectedPost?.candidateName)}
    />
  </>
  );
};

export default EmployerTalentSearchPage;
