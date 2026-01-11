import React, { useState, useEffect } from 'react';
import seekingPostService from '../../services/seekingPost.service';
import { 
  Plus,
  Eye,
  Calendar,
  MapPin,
  Banknote,
  X,
  Edit,
  Trash2,
  Loader2
} from 'lucide-react';
import { toast } from 'react-hot-toast';
import { VIETNAM_PROVINCES } from '../../data/provinces';

/**
 * CandidatePostManager - Trang quản lý tin tìm việc của Ứng viên
 * 
 * Trang này cho phép ứng viên quản lý các tin đăng tìm việc của mình.
 * Ứng viên có thể tạo mới, chỉnh sửa, ẩn/hiện và xóa tin đăng.
 * 
 * Features:
 * - Bảng hiển thị danh sách tin đăng (API: GET /seeking-posts/my)
 * - Tạo tin mới qua Modal (API: POST /seeking-posts)
 * - Toggle trạng thái Active/Hidden (API: PUT /seeking-posts/{id})
 * - Xóa tin đăng (API: DELETE /seeking-posts/{id})
 * - Hiển thị số lượt xem
 * 
 * API Integration:
 * - Uses seekingPostService for all CRUD operations
 * - Real-time data from backend
 * - Automatic refresh after mutations
 */
const CandidatePostManager = () => {
  const [myPosts, setMyPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingPost, setEditingPost] = useState(null); // null = create mode, object = edit mode
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [initialLoadDone, setInitialLoadDone] = useState(false);
  const [formData, setFormData] = useState({
    title: '',
    desiredSalary: '',
    location: '',
    skills: '',
    introduction: '',
    expiryDate: ''
  });

  // const [formData, setFormData] = useState({
  //   skPostTitle: '',
  //   desiredSalary: '',
  //   desiredLocation: '',
  //   skPostSkills: '',
  //   skPostIntro: '',
  //   expiryDate: ''
  // });

  // Lấy danh sách tin tìm việc của ứng viên khi component được mount
  useEffect(() => {
    if (!initialLoadDone) {
      fetchMyPosts();
      setInitialLoadDone(true);
    }
  }, [initialLoadDone]);

  /**
   * Lấy danh sách tin tìm việc của ứng viên từ API
   */
  const fetchMyPosts = async () => {
    try {
      setLoading(true);
      const response = await seekingPostService.getMySeekingPosts({
        sort: 'createdAt,desc'
      });
      const data = response.data?.data || response.data;
      
      // Xử lý cả phản hồi phân trang và không phân trang
      if (data.content) {
        setMyPosts(data.content);
      } else if (Array.isArray(data)) {
        setMyPosts(data);
      } else {
        setMyPosts([]);
      }
    } catch (error) {
      toast.error('Không thể tải danh sách tin đăng');
    } finally {
      setLoading(false);
    }
  };

  // Mở modal ở chế độ tạo mới
  const handleOpenCreateModal = () => {
    setEditingPost(null);
    setFormData({
      title: '',
      desiredSalary: '',
      location: '',
      skills: '',
      introduction: '',
      expiryDate: ''
    });
    setShowModal(true);
  };
  
  // Mở modal ở chế độ chỉnh sửa
  const handleOpenEditModal = (post) => {
    setEditingPost(post);
    
    // Chuyển đổi kỹ năng từ mảng thành chuỗi phân cách bằng dấu phẩy
    const skillsStr = Array.isArray(post.skills) 
      ? post.skills.join(', ') 
      : post.skills;
    
    const editFormData = {
      title: post.title || '',
      desiredSalary: post.desiredSalary || '',
      location: post.location || '',
      skills: skillsStr || '',
      introduction: post.introduction || '',
      expiryDate: post.expiryDate || ''
    };
    
    setFormData(editFormData);
    setShowModal(true);
  };
  
  // Đóng modal và đặt lại trạng thái
  const handleCloseModal = () => {
    setShowModal(false);
    setEditingPost(null);
    setFormData({
      title: '',
      desiredSalary: '',
      location: '',
      skills: '',
      introduction: '',
      expiryDate: ''
    });
  };

  // Xử lý thay đổi input trong form
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Xử lý submit form (Tạo mới hoặc Chỉnh sửa)
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Xác thực các trường bắt buộc
    if (!formData.title?.trim()) {
      toast.error('Vui lòng nhập tiêu đề tin đăng');
      return;
    }
    
    if (!formData.skills?.trim()) {
      toast.error('Vui lòng nhập kỹ năng');
      return;
    }
    
    if (!formData.introduction?.trim() || formData.introduction.length < 50) {
      toast.error('Giới thiệu bản thân phải có ít nhất 50 ký tự');
      return;
    }
    
    // Xác thực mức lương nếu được cung cấp
    if (formData.desiredSalary && isNaN(Number(formData.desiredSalary))) {
      // Cho phép văn bản như "Thỏa thuận"
      if (!formData.desiredSalary.match(/[a-zA-Z]/)) {
        toast.error('Mức lương không hợp lệ');
        return;
      }
    }

    try {
      setIsSubmitting(true);
      
      // Đặt ngày hết hạn mặc định nếu không được cung cấp (30 ngày kể từ bây giờ)
      const expiryDate = formData.expiryDate || 
        new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

      // Chuyển đổi mức lương thành số nếu là số, ngược lại giữ nguyên chuỗi
      let salaryValue = formData.desiredSalary || 'Thỏa thuận';
      if (formData.desiredSalary && !isNaN(formData.desiredSalary)) {
        salaryValue = Number(formData.desiredSalary);
      }

      // Chuyển đổi kỹ năng từ chuỗi phân cách bằng dấu phẩy thành mảng
      const skillsArray = formData.skills
        .split(',')
        .map(skill => skill.trim())
        .filter(skill => skill.length > 0);

      // ⚠️ QUAN TRỌNG: Backend mong đợi các tên trường này (phù hợp với JobSeekPostRequest DTO)
      const postData = {
        title: formData.title.trim(),                   
        desiredSalary: salaryValue,
        location: formData.location?.trim() || 'Không xác định',  
        skills: skillsArray,                             
        introduction: formData.introduction.trim(),      
        expiryDate: expiryDate
      };

      if (editingPost) {
        // Chỉnh sửa
        await seekingPostService.updateSeekingPost(editingPost.id, postData);
        toast.success('Đã cập nhật tin tìm việc thành công!');
      } else {
        // Tạo mới
        await seekingPostService.createSeekingPost(postData);
        toast.success('Đã tạo tin tìm việc thành công!');
      }
      
      // Đặt lại form và đóng modal
      handleCloseModal();
      
      // Tải lại các tin đăng của ứng viên
      fetchMyPosts();
    } catch (error) {
      const errorMsg = error.response?.data?.message || 
        (editingPost ? 'Không thể cập nhật tin đăng' : 'Không thể tạo tin đăng');
      toast.error(errorMsg);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Xử lý chuyển đổi trạng thái với cập nhật UI lạc quan
  const handleToggleStatus = async (postId, currentStatus) => {
    const newStatus = currentStatus === 'ACTIVE' ? 'HIDDEN' : 'ACTIVE';
    
    // Cập nhật UI
    setMyPosts(prev => prev.map(post => 
      post.id === postId ? { ...post, status: newStatus } : post
    ));
    
    try {
      await seekingPostService.toggleSeekingPostStatus(postId, newStatus);
      toast.success(`Đã ${newStatus === 'ACTIVE' ? 'hiện' : 'ẩn'} tin đăng`);
    } catch (error) {
      toast.error('Không thể thay đổi trạng thái tin đăng');
      
      // Hoàn tác khi có lỗi
      setMyPosts(prev => prev.map(post => 
        post.id === postId ? { ...post, status: currentStatus } : post
      ));
    }
  };

  // Xóa tin đăng
  const handleDeletePost = async (postId, postTitle) => {
    if (!window.confirm(`Bạn có chắc chắn muốn xóa tin "${postTitle}"?`)) {
      return;
    }

    try {
      await seekingPostService.deleteSeekingPost(postId);
      toast.success('Đã xóa tin đăng thành công');
      
      // Xóa khỏi trạng thái local
      setMyPosts(prev => prev.filter(post => post.id !== postId));
    } catch (error) {
      toast.error('Không thể xóa tin đăng');
    }
  };

  // Định dạng lương để hiển thị
  const formatSalary = (salary) => {
    // Nếu đã được định dạng (chứa văn bản), trả về nguyên bản  
    if (!salary) return 'Thỏa thuận';
    if (typeof salary === 'string' && isNaN(salary)) return salary;
    
    // Nếu là số, định dạng theo VND
    const numericSalary = Number(salary);
    if (!isNaN(numericSalary)) {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(numericSalary);
    }
    
    return salary;
  };

  // Định dạng ngày để hiển thị
  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  };

  // Chuyển đổi kỹ năng từ chuỗi (phân tách bằng dấu phẩy hoặc mảng JSON) thành mảng
  const parseSkills = (skillsData) => {
    if (!skillsData) return [];
    
    // Nếu đã là mảng, trả về nguyên bản
    if (Array.isArray(skillsData)) return skillsData;
    
    // Thử phân tích JSON
    try {
      const parsed = JSON.parse(skillsData);
      if (Array.isArray(parsed)) return parsed;
    } catch (e) {
      // Không phải JSON, xử lý như chuỗi phân tách bằng dấu phẩy
    }
    
    // Xử lý như chuỗi phân tách bằng dấu phẩy
    return skillsData.split(',').map(s => s.trim()).filter(s => s);
  };

  return (
    <div className="min-h-screen bg-neutral-50">
        {/* Header */}
        <div className="bg-white border-b border-gray-200 mb-6">
          <div className="px-6 py-6">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-3xl font-bold text-gray-900 mb-2">
                  Quản lý tin tìm việc
                </h1>
                <p className="text-gray-600">
                  Đăng tin để nhà tuyển dụng tìm thấy bạn dễ dàng hơn
                </p>
              </div>
              <button
                onClick={handleOpenCreateModal}
                className="bg-green-600 hover:bg-green-700 text-white px-6 py-3 rounded-lg font-medium flex items-center gap-2 shadow-sm transition-colors"
              >
                <Plus className="w-5 h-5" />
                Tạo tin mới
              </button>
            </div>
          </div>
        </div>

        <div className="px-6 pb-6">
          {/* Statistics Cards */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 mb-1">Tổng số tin</p>
                  <p className="text-3xl font-bold text-gray-900">{myPosts.length}</p>
                </div>
                <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                  <Eye className="w-6 h-6 text-blue-600" />
                </div>
              </div>
            </div>

            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 mb-1">Đang hiển thị</p>
                  <p className="text-3xl font-bold text-green-600">
                    {myPosts.filter(p => p.skPostStatus === 'ACTIVE').length}
                  </p>
                </div>
                <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                  <Eye className="w-6 h-6 text-green-600" />
                </div>
              </div>
            </div>

            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 mb-1">Tổng lượt xem</p>
                  <p className="text-3xl font-bold text-gray-900">
                    {myPosts.reduce((sum, post) => sum + (post.views || 0), 0)}
                  </p>
                </div>
                <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                  <Eye className="w-6 h-6 text-purple-600" />
                </div>
              </div>
            </div>
          </div>

          {/* Loading State */}
          {loading && (
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-12">
              <div className="flex flex-col items-center justify-center">
                <Loader2 className="w-12 h-12 text-blue-600 animate-spin mb-4" />
                <p className="text-gray-600">Đang tải danh sách tin đăng...</p>
              </div>
            </div>
          )}

          {/* Posts Table */}
          {!loading && myPosts.length > 0 && (
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gray-50 border-b border-gray-200">
                    <tr>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">
                        Tiêu đề
                      </th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">
                        Ngày tạo
                      </th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">
                        Lượt xem
                      </th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">
                        Trạng thái
                      </th>
                      <th className="px-6 py-4 text-right text-sm font-semibold text-gray-900">
                        Hành động
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200">
                    {(() => {
                      return myPosts.map(post => {
                        const skills = parseSkills(post.skills);
                        
                        return (
                          <tr key={post.id} className="hover:bg-gray-50 transition-colors">
                          {/* Title */}
                          <td className="px-6 py-4">
                            <div className="max-w-md">
                              <h3 className="font-medium text-gray-900 mb-1">
                                {post.title}
                              </h3>
                              <div className="flex flex-wrap gap-2 mb-2">
                                {skills.slice(0, 3).map((skill) => (
                                  <span 
                                    key={skill}
                                    className="px-2 py-0.5 bg-blue-50 text-blue-700 text-xs rounded"
                                  >
                                    {skill}
                                  </span>
                                ))}
                                {skills.length > 3 && (
                                  <span className="px-2 py-0.5 bg-gray-100 text-gray-600 text-xs rounded">
                                    +{skills.length - 3}
                                  </span>
                                )}
                              </div>
                              <div className="flex items-center gap-4 text-sm text-gray-600">
                                <div key="salary" className="flex items-center gap-1">
                                  <Banknote className="w-4 h-4" />
                                  <span>{formatSalary(post.desiredSalary)}</span>
                                </div>
                                <div key="location" className="flex items-center gap-1">
                                  <MapPin className="w-4 h-4" />
                                  <span>{post.location}</span>
                                </div>
                              </div>
                            </div>
                          </td>

                          {/* Created Date */}
                          <td className="px-6 py-4">
                            <div className="flex items-center gap-2 text-sm text-gray-600">
                              <Calendar className="w-4 h-4" />
                              <span>{formatDate(post.createdDate)}</span>
                            </div>
                          </td>

                          {/* Views */}
                          <td className="px-6 py-4">
                            <div className="flex items-center gap-2">
                              <Eye className="w-4 h-4 text-gray-400" />
                              <span className="font-medium text-gray-900">{post.views || 0}</span>
                            </div>
                          </td>

                          {/* Status Toggle */}
                          <td className="px-6 py-4">
                            <button
                              onClick={() => handleToggleStatus(post.id, post.status)}
                              className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 ${
                                post.status === 'ACTIVE' 
                                  ? 'bg-green-600' 
                                  : 'bg-gray-300'
                              }`}
                            >
                              <span
                                className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                                  post.status === 'ACTIVE' 
                                    ? 'translate-x-6' 
                                    : 'translate-x-1'
                                }`}
                              />
                            </button>
                            <div className="mt-1">
                              <span className={`text-xs font-medium ${
                                post.status === 'ACTIVE' 
                                  ? 'text-green-600' 
                                  : 'text-gray-500'
                              }`}>
                                {post.status === 'ACTIVE' ? 'Đang hiện' : 'Đã ẩn'}
                              </span>
                            </div>
                          </td>

                          {/* Actions */}
                          <td className="px-6 py-4">
                            <div className="flex items-center justify-end gap-2">
                              <button
                                onClick={() => handleOpenEditModal(post)}
                                className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                title="Chỉnh sửa"
                              >
                                <Edit className="w-4 h-4" />
                              </button>
                              <button
                                onClick={() => handleDeletePost(post.id, post.title)}
                                className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                title="Xóa"
                              >
                                <Trash2 className="w-4 h-4" />
                              </button>
                            </div>
                          </td>
                        </tr>
                      );
                    });
                    })()}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Empty State */}
          {!loading && myPosts.length === 0 && (
            <div className="bg-white rounded-lg shadow-sm border border-gray-200">
              <div className="text-center py-16">
                <Eye className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                <h3 className="text-xl font-semibold text-gray-900 mb-2">
                  Chưa có tin tìm việc nào
                </h3>
                <p className="text-gray-600 mb-6">
                  Tạo tin đăng đầu tiên để nhà tuyển dụng tìm thấy bạn
                </p>
                <button
                  onClick={handleOpenCreateModal}
                  className="bg-green-600 hover:bg-green-700 text-white px-6 py-3 rounded-lg font-medium inline-flex items-center gap-2"
                >
                  <Plus className="w-5 h-5" />
                  Tạo tin mới
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Create/Edit Post Modal */}
        {showModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
              {/* Modal Header */}
              <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
                <h2 className="text-2xl font-bold text-gray-900">
                  {editingPost ? 'Chỉnh sửa tin tìm việc' : 'Tạo tin tìm việc mới'}
                </h2>
                <button
                  onClick={handleCloseModal}
                  className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                  disabled={isSubmitting}
                >
                  <X className="w-5 h-5 text-gray-500" />
                </button>
              </div>

              {/* Modal Body */}
              <form onSubmit={handleSubmit} className="p-6 space-y-6">
                {/* Title */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Tiêu đề tin <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    name="title"
                    value={formData.title}
                    onChange={handleInputChange}
                    placeholder="VD: Fullstack Java Developer tìm việc tại TP.HCM"
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                    required
                  />
                  <p className="mt-1 text-xs text-gray-500">
                    Tiêu đề rõ ràng giúp nhà tuyển dụng tìm thấy bạn dễ dàng hơn
                  </p>
                </div>

                {/* Salary and Location */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Mức lương mong muốn <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      name="desiredSalary"
                      value={formData.desiredSalary}
                      onChange={handleInputChange}
                      placeholder="VD: 20-30 triệu hoặc Thỏa thuận"
                      className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Địa điểm <span className="text-red-500">*</span>
                    </label>
                    <select
                      name="location"
                      value={formData.location}
                      onChange={handleInputChange}
                      className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent appearance-none bg-white"
                      required
                    >
                      <option value="">Chọn địa điểm...</option>
                      <option value="Remote">Remote</option>
                      <option value="Tất cả">Tất cả tỉnh thành</option>
                      {VIETNAM_PROVINCES.map((province) => (
                        <option key={province} value={province}>
                          {province}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                {/* Skills */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Kỹ năng <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    name="skills"
                    value={formData.skills}
                    onChange={handleInputChange}
                    placeholder="VD: Java, Spring Boot, React, Docker (phân cách bằng dấu phẩy)"
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                    required
                  />
                  <p className="mt-1 text-xs text-gray-500">
                    Liệt kê các kỹ năng chính của bạn, cách nhau bằng dấu phẩy
                  </p>
                </div>

                {/* Introduction */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Giới thiệu bản thân <span className="text-red-500">*</span>
                  </label>
                  <textarea
                    name="introduction"
                    value={formData.introduction}
                    onChange={handleInputChange}
                    rows={6}
                    placeholder="Viết về kinh nghiệm làm việc, điểm mạnh, mục tiêu nghề nghiệp của bạn..."
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent resize-none"
                    required
                  />
                  <div className="mt-1 flex justify-between items-center">
                    <p className="text-xs text-gray-500">
                      Tối thiểu 50 ký tự. Hãy viết nội dung chuyên nghiệp và thu hút.
                    </p>
                    <p className={`text-xs font-medium ${
                      (formData.introduction?.length || 0) >= 50 
                        ? 'text-green-600' 
                        : 'text-red-500'
                    }`}>
                      {formData.introduction?.length || 0}/50
                    </p>
                  </div>
                </div>

                {/* Expiry Date (Optional) */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Ngày hết hạn (tùy chọn)
                  </label>
                  <input
                    type="date"
                    name="expiryDate"
                    value={formData.expiryDate}
                    onChange={handleInputChange}
                    min={new Date().toISOString().split('T')[0]}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  />
                  <p className="mt-1 text-xs text-gray-500">
                    Nếu không chọn, tin đăng sẽ tự động hết hạn sau 30 ngày
                  </p>
                </div>

                {/* Note */}
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <p className="text-sm text-blue-800">
                    <strong>Lưu ý:</strong> Tin đăng của bạn sẽ được hiển thị công khai. 
                    Đảm bảo thông tin chính xác và tuân thủ quy định cộng đồng.
                  </p>
                </div>

                {/* Modal Footer */}
                <div className="flex items-center gap-3 pt-4 border-t border-gray-200">
                  <button
                    type="button"
                    onClick={handleCloseModal}
                    disabled={isSubmitting}
                    className="flex-1 px-6 py-3 border border-gray-300 text-gray-700 font-medium rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Hủy
                  </button>
                  <button
                    type="submit"
                    disabled={isSubmitting}
                    className="flex-1 px-6 py-3 bg-green-600 hover:bg-green-700 text-white font-medium rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                  >
                    {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
                    {isSubmitting 
                      ? (editingPost ? 'Đang cập nhật...' : 'Đang đăng...') 
                      : (editingPost ? 'Cập nhật' : 'Đăng tin')
                    }
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
  );
};

export default CandidatePostManager;
