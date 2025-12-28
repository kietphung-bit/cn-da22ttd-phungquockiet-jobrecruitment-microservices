import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Save, X, Loader2, DollarSign, MapPin, Calendar, Users, Briefcase } from 'lucide-react';
import { toast } from 'react-toastify';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import jobService from '../../services/job.service';

/**
 * JobEditor Component
 * Create and edit job postings
 * 
 * Features:
 * - Create new job: POST /api/v1/jobs
 * - Update existing job: PUT /api/v1/jobs/{id}
 * - Rich text editors for description and requirements (TipTap)
 * - Form validation (dates, salary, required fields)
 * - Category dropdown from API
 * - Currency formatting preview
 * 
 * Validation Rules:
 * - Title, Category, Salary, Location: Required
 * - Salary: Must be > 0 (RBGTN)
 * - End Date >= Start Date (RBNT)
 * - Max Candidates: >= 0 (RBSL)
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
  });
  const [errors, setErrors] = useState({});

  // TipTap editor for job description
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

  // TipTap editor for job requirements
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

  useEffect(() => {
    fetchCategories();
    if (isEditMode) {
      fetchJobData();
    }
  }, [id]);

  // Update editors when description/requirement changes
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

      setFormData({
        jcId: job.jobCategory?.jcId || '',
        jobTitle: job.jobTitle || '',
        jobSalary: job.jobSalary || '',
        jobLocation: job.jobLocation || '',
        startDate: job.startDate || '',
        endDate: job.endDate || '',
        maxCandidates: job.maxCandidates || '',
        jobDescription: job.jobDescription || '',
        jobRequirement: job.jobRequirement || '',
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
        jobSalary: Number(formData.jobSalary),
        jobLocation: formData.jobLocation,
        startDate: formData.startDate,
        endDate: formData.endDate,
        maxCandidates: Number(formData.maxCandidates) || 1,
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

  // TipTap toolbar component
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
                  <DollarSign className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
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
                  <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
                  <input
                    type="text"
                    name="jobLocation"
                    value={formData.jobLocation}
                    onChange={handleInputChange}
                    className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary focus:border-primary ${
                      errors.jobLocation ? 'border-red-500' : 'border-neutral-300'
                    }`}
                    placeholder="TP. Hồ Chí Minh"
                  />
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
              disabled={saving}
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
