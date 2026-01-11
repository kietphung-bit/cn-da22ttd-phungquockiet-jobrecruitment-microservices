import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Save, X, Loader2, Banknote, MapPin, Calendar, Users, Briefcase, AlertCircle } from 'lucide-react';
import { toast } from 'react-toastify';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import jobService from '../../services/job.service';
import { VIETNAM_PROVINCES } from '../../data/provinces';

/**
 * JobEditor Component
 * Tạo và chỉnh sửa bài đăng việc làm cho nhà tuyển dụng
 * 
 * Tính năng:
 * - Tạo bài đăng việc làm mới: POST /api/v1/jobs
 * - Cập nhật bài đăng việc làm hiện có: PUT /api/v1/jobs/{id}
 * - Trình soạn thảo rich text cho mô tả và yêu cầu (TipTap)
 * - Xác thực biểu mẫu (ngày tháng, lương, trường bắt buộc)
 * - Dropdown danh mục từ API
 * - Xem trước định dạng tiền tệ
 * 
 * Quy tắc xác thực:
 * - Tiêu đề, Danh mục, Lương, Địa điểm: Bắt buộc
 * - Lương: Phải > 0 (RBGTN)
 * - Ngày kết thúc >= Ngày bắt đầu (RBNT)
 * - Số lượng ứng viên tối đa: >= 0 (RBSL)
 */
const JobEditor = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditMode = Boolean(id);

  const [loading, setLoading] = useState(isEditMode);
  const [saving, setSaving] = useState(false);
  const [categories, setCategories] = useState([]);
  const [formData, setFormData] = useState({
    jcId: '',
    jobTitle: '',
    jobSalary: '',
    jobLocation: '',
    startDate: '',
    endDate: '',
    maxCandidates: '',
    jobDescription: '',
    jobRequirement: '',
    jobResponsibilities: '',
    jobBenefits: '',
    termsAgreed: false,
  });
  const [errors, setErrors] = useState({});

  // Trình soạn thảo TipTap cho mô tả công việc
  const descriptionEditor = useEditor({
    extensions: [StarterKit],
    content: '',
    editorProps: {
      attributes: {
        class: 'prose max-w-none focus:outline-none min-h-[200px] p-4 border border-neutral-300 rounded-lg',
      },
    },
    onUpdate: ({ editor }) => {
      setFormData(prev => ({ ...prev, jobDescription: editor.getHTML() }));
    },
  });

  // Trình soạn thảo TipTap cho yêu cầu công việc
  const requirementEditor = useEditor({
    extensions: [StarterKit],
    content: '',
    editorProps: {
      attributes: {
        class: 'prose max-w-none focus:outline-none min-h-[200px] p-4 border border-neutral-300 rounded-lg',
      },
    },
    onUpdate: ({ editor }) => {
      setFormData(prev => ({ ...prev, jobRequirement: editor.getHTML() }));
    },
  });

  // Trình soạn thảo TipTap cho trách nhiệm công việc
  const responsibilitiesEditor = useEditor({
    extensions: [StarterKit],
    content: '',
    editorProps: {
      attributes: {
        class: 'prose max-w-none focus:outline-none min-h-[200px] p-4 border border-neutral-300 rounded-lg',
      },
    },
    onUpdate: ({ editor }) => {
      setFormData(prev => ({ ...prev, jobResponsibilities: editor.getHTML() }));
    },
  });

  // Trình soạn thảo TipTap cho phúc lợi công việc
  const benefitsEditor = useEditor({
    extensions: [StarterKit],
    content: '',
    editorProps: {
      attributes: {
        class: 'prose max-w-none focus:outline-none min-h-[200px] p-4 border border-neutral-300 rounded-lg',
      },
    },
    onUpdate: ({ editor }) => {
      setFormData(prev => ({ ...prev, jobBenefits: editor.getHTML() }));
    },
  });

  useEffect(() => {
    fetchCategories();
    if (isEditMode) {
      fetchJobData();
    }
  }, [id]);

  // Cập nhật trình soạn thảo khi mô tả/yêu cầu thay đổi
  useEffect(() => {
    if (descriptionEditor && formData.jobDescription !== descriptionEditor.getHTML()) {
      descriptionEditor.commands.setContent(formData.jobDescription);
    }
  }, [descriptionEditor, formData.jobDescription]);

  useEffect(() => {
    if (requirementEditor && formData.jobRequirement !== requirementEditor.getHTML()) {
      requirementEditor.commands.setContent(formData.jobRequirement);
    }
  }, [requirementEditor, formData.jobRequirement]);

  useEffect(() => {
    if (responsibilitiesEditor && formData.jobResponsibilities !== responsibilitiesEditor.getHTML()) {
      responsibilitiesEditor.commands.setContent(formData.jobResponsibilities);
    }
  }, [responsibilitiesEditor, formData.jobResponsibilities]);

  useEffect(() => {
    if (benefitsEditor && formData.jobBenefits !== benefitsEditor.getHTML()) {
      benefitsEditor.commands.setContent(formData.jobBenefits);
    }
  }, [benefitsEditor, formData.jobBenefits]);

  const fetchCategories = async () => {
    try {
      const response = await jobService.getJobCategories();
      setCategories(response.data || []);
    } catch (error) {
      console.error('Error fetching categories:', error);
      toast.error('Không thể tải danh mục công việc');
    }
  };

  const fetchJobData = async () => {
    try {
      setLoading(true);
      const response = await jobService.getJobDetail(id);
      const job = response.data;

      // Lưu ý: Backend trả về JobResponse dạng phẳng với jcId và jcName trực tiếp
      // Không lồng như job.jobCategory.jcId (xem JobResponse.java)
      setFormData({
        jcId: job.jcId || '',
        jobTitle: job.jobTitle || '',
        jobSalary: job.jobSalary || '',
        jobLocation: job.jobLocation || '',
        startDate: job.startDate || '',
        endDate: job.endDate || '',
        maxCandidates: job.maxCandidates || '',
        jobDescription: job.jobDescription || '',
        jobRequirement: job.jobRequirement || '',
        jobResponsibilities: job.jobResponsibilities || '',
        jobBenefits: job.jobBenefits || '',
      });
    } catch (error) {
      console.error('Error fetching job data:', error);
      toast.error('Không thể tải thông tin tin tuyển dụng');
      navigate('/employer/jobs');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.jobTitle.trim()) {
      newErrors.jobTitle = 'Tiêu đề công việc là bắt buộc';
    }

    if (!formData.jcId) {
      newErrors.jcId = 'Danh mục công việc là bắt buộc';
    }

    if (!formData.jobSalary) {
      newErrors.jobSalary = 'Mức lương là bắt buộc';
    } else if (Number(formData.jobSalary) <= 0) {
      newErrors.jobSalary = 'Mức lương phải lớn hơn 0';
    }

    if (!formData.jobLocation.trim()) {
      newErrors.jobLocation = 'Địa điểm làm việc là bắt buộc';
    }

    if (!formData.startDate) {
      newErrors.startDate = 'Ngày bắt đầu là bắt buộc';
    }

    if (!formData.endDate) {
      newErrors.endDate = 'Ngày kết thúc là bắt buộc';
    }

    if (formData.startDate && formData.endDate) {
      if (new Date(formData.endDate) < new Date(formData.startDate)) {
        newErrors.endDate = 'Ngày kết thúc phải sau ngày bắt đầu';
      }
    }

    if (formData.maxCandidates && Number(formData.maxCandidates) < 0) {
      newErrors.maxCandidates = 'Số lượng ứng viên không thể âm';
    }

    if (!formData.jobDescription || formData.jobDescription === '<p></p>') {
      newErrors.jobDescription = 'Mô tả công việc là bắt buộc';
    }

    if (!formData.jobRequirement || formData.jobRequirement === '<p></p>') {
      newErrors.jobRequirement = 'Yêu cầu công việc là bắt buộc';
    }

    if (!isEditMode && !formData.termsAgreed) {
      newErrors.termsAgreed = 'Bạn phải đồng ý với các điều khoản để đăng tin';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      toast.error('Vui lòng kiểm tra lại thông tin');
      return;
    }

    try {
      setSaving(true);

      const jobData = {
        jcId: Number(formData.jcId),
        jobTitle: formData.jobTitle,
        jobDescription: formData.jobDescription,
        jobRequirement: formData.jobRequirement,
        jobResponsibilities: formData.jobResponsibilities,
        jobBenefits: formData.jobBenefits,
        jobSalary: Number(formData.jobSalary),
        jobLocation: formData.jobLocation,
        startDate: formData.startDate,
        endDate: formData.endDate,
        maxCandidates: Number(formData.maxCandidates) || 1,
        termsAgreed: !isEditMode ? formData.termsAgreed : undefined,
      };

      if (isEditMode) {
        await jobService.updateJob(id, jobData);
        toast.success('Cập nhật tin tuyển dụng thành công');
      } else {
        await jobService.createJob(jobData);
        toast.success('Đăng tin tuyển dụng thành công');
      }

      navigate('/employer/jobs');
    } catch (error) {
      console.error('Error saving job:', error);
      const errorMsg = error.response?.data?.message || 'Không thể lưu tin tuyển dụng';
      toast.error(errorMsg);
    } finally {
      setSaving(false);
    }
  };

  const formatCurrency = (amount) => {
    if (!amount) return '';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(amount);
  };

  // Thành phần thanh công cụ TipTap
  const EditorToolbar = ({ editor, label }) => {
    if (!editor) return null;

    return (
      <div className="mb-2">
        <label className="block text-sm font-semibold text-neutral-700 mb-2">
          {label} <span className="text-red-500">*</span>
        </label>
        <div className="border border-neutral-300 rounded-lg overflow-hidden">
          <div className="flex flex-wrap gap-2 p-2 border-b border-neutral-300 bg-neutral-50">
            <button
              type="button"
              onClick={() => editor.chain().focus().toggleBold().run()}
              className={`px-3 py-1 text-sm font-medium rounded ${
                editor.isActive('bold') ? 'bg-primary text-white' : 'bg-white text-neutral-700 hover:bg-neutral-100'
              }`}
            >
              B
            </button>
            <button
              type="button"
              onClick={() => editor.chain().focus().toggleItalic().run()}
              className={`px-3 py-1 text-sm font-medium italic rounded ${
                editor.isActive('italic') ? 'bg-primary text-white' : 'bg-white text-neutral-700 hover:bg-neutral-100'
              }`}
            >
              I
            </button>
            <button
              type="button"
              onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
              className={`px-3 py-1 text-sm font-medium rounded ${
                editor.isActive('heading', { level: 2 }) ? 'bg-primary text-white' : 'bg-white text-neutral-700 hover:bg-neutral-100'
              }`}
            >
              H2
            </button>
            <button
              type="button"
              onClick={() => editor.chain().focus().toggleHeading({ level: 3 }).run()}
              className={`px-3 py-1 text-sm font-medium rounded ${
                editor.isActive('heading', { level: 3 }) ? 'bg-primary text-white' : 'bg-white text-neutral-700 hover:bg-neutral-100'
              }`}
            >
              H3
            </button>
            <button
              type="button"
              onClick={() => editor.chain().focus().toggleBulletList().run()}
              className={`px-3 py-1 text-sm font-medium rounded ${
                editor.isActive('bulletList') ? 'bg-primary text-white' : 'bg-white text-neutral-700 hover:bg-neutral-100'
              }`}
            >
              • List
            </button>
            <button
              type="button"
              onClick={() => editor.chain().focus().toggleOrderedList().run()}
              className={`px-3 py-1 text-sm font-medium rounded ${
                editor.isActive('orderedList') ? 'bg-primary text-white' : 'bg-white text-neutral-700 hover:bg-neutral-100'
              }`}
            >
              1. List
            </button>
          </div>
          <EditorContent editor={editor} />
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-neutral-50 flex items-center justify-center">
        <div className="text-center">
          <Loader2 className="w-12 h-12 text-primary animate-spin mx-auto mb-4" />
          <p className="text-neutral-600">Đang tải thông tin...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-neutral-50">
      {/* Header */}
      <div className="bg-white border-b border-neutral-200">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <h1 className="text-3xl font-bold text-neutral-900">
            {isEditMode ? 'Chỉnh sửa tin tuyển dụng' : 'Đăng tin tuyển dụng mới'}
          </h1>
          <p className="text-neutral-600 mt-1">
            {isEditMode ? 'Cập nhật thông tin tin tuyển dụng' : 'Điền thông tin để đăng tin tuyển dụng'}
          </p>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Basic Information */}
          <div className="bg-white rounded-lg shadow-sm p-6 border border-neutral-200">
            <h2 className="text-lg font-bold text-neutral-900 mb-4">Thông tin cơ bản</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Job Title */}
              <div className="md:col-span-2">
                <label className="block text-sm font-semibold text-neutral-700 mb-2">
                  Tiêu đề công việc <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Briefcase className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
                  <input
                    type="text"
                    name="jobTitle"
                    value={formData.jobTitle}
                    onChange={handleInputChange}
                    className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary focus:border-primary ${
                      errors.jobTitle ? 'border-red-500' : 'border-neutral-300'
                    }`}
                    placeholder="Ví dụ: Lập trình viên Java Senior"
                  />
                </div>
                {errors.jobTitle && (
                  <p className="text-red-500 text-sm mt-1">{errors.jobTitle}</p>
                )}
              </div>

              {/* Job Category */}
              <div>
                <label className="block text-sm font-semibold text-neutral-700 mb-2">
                  Danh mục công việc <span className="text-red-500">*</span>
                </label>
                <select
                  name="jcId"
                  value={formData.jcId}
                  onChange={handleInputChange}
                  className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary focus:border-primary ${
                    errors.jcId ? 'border-red-500' : 'border-neutral-300'
                  }`}
                >
                  <option value="">-- Chọn danh mục --</option>
                  {categories.map((category) => (
                    <option key={category.jcId} value={category.jcId}>
                      {category.jcName}
                    </option>
                  ))}
                </select>
                {errors.jcId && (
                  <p className="text-red-500 text-sm mt-1">{errors.jcId}</p>
                )}
              </div>

              {/* Salary */}
              <div>
                <label className="block text-sm font-semibold text-neutral-700 mb-2">
                  Mức lương (VNĐ) <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Banknote className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
                  <input
                    type="number"
                    name="jobSalary"
                    value={formData.jobSalary}
                    onChange={handleInputChange}
                    min="0"
                    step="100000"
                    className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary focus:border-primary ${
                      errors.jobSalary ? 'border-red-500' : 'border-neutral-300'
                    }`}
                    placeholder="15000000"
                  />
                </div>
                {formData.jobSalary && (
                  <p className="text-xs text-neutral-500 mt-1">
                    Preview: {formatCurrency(formData.jobSalary)}
                  </p>
                )}
                {errors.jobSalary && (
                  <p className="text-red-500 text-sm mt-1">{errors.jobSalary}</p>
                )}
              </div>

              {/* Location */}
              <div className="md:col-span-2">
                <label className="block text-sm font-semibold text-neutral-700 mb-2">
                  Địa điểm làm việc <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400 pointer-events-none" />
                  <select
                    name="jobLocation"
                    value={formData.jobLocation}
                    onChange={handleInputChange}
                    className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary focus:border-primary appearance-none bg-white ${
                      errors.jobLocation ? 'border-red-500' : 'border-neutral-300'
                    }`}
                  >
                    <option value="">Chọn địa điểm...</option>
                    <option value="Remote">Remote</option>
                    {VIETNAM_PROVINCES.map((province) => (
                      <option key={province} value={province}>
                        {province}
                      </option>
                    ))}
                  </select>
                </div>
                {errors.jobLocation && (
                  <p className="text-red-500 text-sm mt-1">{errors.jobLocation}</p>
                )}
              </div>
            </div>
          </div>

          {/* Timeline & Quantity */}
          <div className="bg-white rounded-lg shadow-sm p-6 border border-neutral-200">
            <h2 className="text-lg font-bold text-neutral-900 mb-4">Thời gian & Số lượng</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {/* Start Date */}
              <div>
                <label className="block text-sm font-semibold text-neutral-700 mb-2">
                  Ngày bắt đầu <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
                  <input
                    type="date"
                    name="startDate"
                    value={formData.startDate}
                    onChange={handleInputChange}
                    className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary focus:border-primary ${
                      errors.startDate ? 'border-red-500' : 'border-neutral-300'
                    }`}
                  />
                </div>
                {errors.startDate && (
                  <p className="text-red-500 text-sm mt-1">{errors.startDate}</p>
                )}
              </div>

              {/* End Date */}
              <div>
                <label className="block text-sm font-semibold text-neutral-700 mb-2">
                  Ngày kết thúc <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
                  <input
                    type="date"
                    name="endDate"
                    value={formData.endDate}
                    onChange={handleInputChange}
                    className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary focus:border-primary ${
                      errors.endDate ? 'border-red-500' : 'border-neutral-300'
                    }`}
                  />
                </div>
                {errors.endDate && (
                  <p className="text-red-500 text-sm mt-1">{errors.endDate}</p>
                )}
              </div>

              {/* Max Candidates */}
              <div>
                <label className="block text-sm font-semibold text-neutral-700 mb-2">
                  Số lượng cần tuyển
                </label>
                <div className="relative">
                  <Users className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
                  <input
                    type="number"
                    name="maxCandidates"
                    value={formData.maxCandidates}
                    onChange={handleInputChange}
                    min="0"
                    className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary focus:border-primary ${
                      errors.maxCandidates ? 'border-red-500' : 'border-neutral-300'
                    }`}
                    placeholder="1"
                  />
                </div>
                {errors.maxCandidates && (
                  <p className="text-red-500 text-sm mt-1">{errors.maxCandidates}</p>
                )}
              </div>
            </div>
          </div>

          {/* Job Description */}
          <div className="bg-white rounded-lg shadow-sm p-6 border border-neutral-200">
            <h2 className="text-lg font-bold text-neutral-900 mb-4">Mô tả công việc</h2>
            <EditorToolbar editor={descriptionEditor} label="Mô tả chi tiết" />
            {errors.jobDescription && (
              <p className="text-red-500 text-sm mt-1">{errors.jobDescription}</p>
            )}
          </div>

          {/* Job Requirements */}
          <div className="bg-white rounded-lg shadow-sm p-6 border border-neutral-200">
            <h2 className="text-lg font-bold text-neutral-900 mb-4">Yêu cầu công việc</h2>
            <EditorToolbar editor={requirementEditor} label="Yêu cầu ứng viên" />
            {errors.jobRequirement && (
              <p className="text-red-500 text-sm mt-1">{errors.jobRequirement}</p>
            )}
          </div>

          {/* Job Responsibilities */}
          <div className="bg-white rounded-lg shadow-sm p-6 border border-neutral-200">
            <h2 className="text-lg font-bold text-neutral-900 mb-4">Trách nhiệm công việc</h2>
            <EditorToolbar editor={responsibilitiesEditor} label="Mô tả các trách nhiệm chính" />
          </div>

          {/* Job Benefits */}
          <div className="bg-white rounded-lg shadow-sm p-6 border border-neutral-200">
            <h2 className="text-lg font-bold text-neutral-900 mb-4">Quyền lợi & Phúc lợi</h2>
            <EditorToolbar editor={benefitsEditor} label="Quyền lợi và chế độ đãi ngộ" />
          </div>

          {/* Terms Agreement (only for new posts) */}
          {!isEditMode && (
            <div className="bg-amber-50 border border-amber-200 rounded-lg shadow-sm p-6">
              <h2 className="text-lg font-bold text-neutral-900 mb-4 flex items-center gap-2">
                <AlertCircle className="w-5 h-5 text-amber-600" />
                Cam kết tuân thủ
              </h2>
              <label className="flex items-start gap-3 cursor-pointer">
                <input
                  type="checkbox"
                  checked={formData.termsAgreed}
                  onChange={(e) => setFormData(prev => ({ ...prev, termsAgreed: e.target.checked }))}
                  className="mt-1 w-5 h-5 text-primary focus:ring-primary border-neutral-300 rounded"
                />
                <div className="flex-1">
                  <p className="text-neutral-800 leading-relaxed">
                    Tôi cam kết tin tuyển dụng này là có thật và tuân thủ các quy định pháp luật Việt Nam. 
                    Tôi chịu hoàn toàn trách nhiệm về nội dung đăng tải và hiểu rằng mọi thông tin sai lệch 
                    hoặc vi phạm sẽ bị xử lý theo quy định của pháp luật.
                  </p>
                  {errors.termsAgreed && (
                    <p className="text-red-500 text-sm mt-2 font-medium">{errors.termsAgreed}</p>
                  )}
                </div>
              </label>
            </div>
          )}

          {/* Submit Buttons */}
          <div className="flex justify-end gap-4">
            <button
              type="button"
              onClick={() => navigate('/employer/jobs')}
              className="px-6 py-3 border border-neutral-300 text-neutral-700 rounded-lg hover:bg-neutral-50 transition-colors font-medium flex items-center gap-2"
            >
              <X className="w-5 h-5" />
              Hủy
            </button>
            <button
              type="submit"
              disabled={saving || (!isEditMode && !formData.termsAgreed)}
              className="px-6 py-3 bg-primary text-white rounded-lg hover:bg-primary-600 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            >
              {saving ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Đang lưu...
                </>
              ) : (
                <>
                  <Save className="w-5 h-5" />
                  {isEditMode ? 'Cập nhật' : 'Đăng tin'}
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default JobEditor;
