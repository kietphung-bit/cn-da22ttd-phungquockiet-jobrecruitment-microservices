/**
 * CandidateDetail.jsx - Chi tiết ứng viên (Employer view)
 * 
 * Tinh năng:
 * - Xem thông tin chi tiết ứng viên
 * - Hiển thị hồ sơ, kỹ năng, kinh nghiệm
 * - Quay lại danh sách ứng tuyển
 * 
 * API Endpoint:
 * - GET /api/v1/candidates/{id}
 */

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  ArrowLeft, 
  User, 
  Mail, 
  Phone, 
  Calendar, 
  MapPin,
  Briefcase,
  GraduationCap,
  Award,
  Loader2
} from 'lucide-react';
import { format } from 'date-fns';
import { toast } from 'react-toastify';
import axiosClient from '../../api/axiosClient';

const CandidateDetail = () => {
  const { candidateId } = useParams();
  const navigate = useNavigate();

  const [candidate, setCandidate] = useState(null);
  const [loading, setLoading] = useState(true);

  /**
   * Lấy chi tiết ứng viên
   */
  const fetchCandidateDetail = async () => {
    try {
      setLoading(true);
      const response = await axiosClient.get(`/candidates/${candidateId}`);
      
      // Xử lý ApiResponse wrapper
      const data = response.data?.data || response.data;
      setCandidate(data);
    } catch (error) {
      console.error('Failed to fetch candidate:', error);
      toast.error('Không thể tải thông tin ứng viên!');
      navigate(-1); // Quay lại khi có lỗi
    } finally {
      setLoading(false);
    }
  };

  /**
   * Định dạng ngày tháng
   */
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    try {
      return format(new Date(dateString), 'dd/MM/yyyy');
    } catch {
      return dateString;
    }
  };

  /**
   * Lấy nhãn giới tính
   */
  const getGenderLabel = (gender) => {
    const genderMap = {
      MALE: 'Nam',
      FEMALE: 'Nữ',
      OTHER: 'Khác'
    };
    return genderMap[gender] || 'Không xác định';
  };

  useEffect(() => {
    fetchCandidateDetail();
  }, [candidateId]);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
        <span className="ml-2 text-neutral-600">Đang tải...</span>
      </div>
    );
  }

  if (!candidate) {
    return (
      <div className="p-6">
        <div className="text-center">
          <h2 className="text-xl font-semibold text-neutral-800">Không tìm thấy ứng viên</h2>
          <button
            onClick={() => navigate(-1)}
            className="mt-4 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90"
          >
            Quay lại
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6">
      {/* Back Button */}
      <button
        onClick={() => navigate(-1)}
        className="inline-flex items-center gap-2 text-neutral-600 hover:text-neutral-800 mb-6"
      >
        <ArrowLeft className="w-5 h-5" />
        Quay lại
      </button>

      {/* Header */}
      <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-6 mb-6">
        <div className="flex items-start gap-4">
          <div className="w-20 h-20 bg-primary/10 rounded-full flex items-center justify-center">
            <User className="w-10 h-10 text-primary" />
          </div>
          
          <div className="flex-1">
            <h1 className="text-2xl font-bold text-neutral-800 mb-2">
              {candidate.candidateName}
            </h1>
            <p className="text-neutral-600 mb-4">
              Mã ứng viên: <span className="font-mono font-semibold">{candidate.candidateCode}</span>
            </p>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {/* Email */}
              {candidate.candidateEmail && (
                <div className="flex items-center gap-2 text-sm">
                  <Mail className="w-4 h-4 text-neutral-400" />
                  <span className="text-neutral-700">{candidate.candidateEmail}</span>
                </div>
              )}

              {/* Phone */}
              {candidate.candidatePhone && (
                <div className="flex items-center gap-2 text-sm">
                  <Phone className="w-4 h-4 text-neutral-400" />
                  <span className="text-neutral-700">{candidate.candidatePhone}</span>
                </div>
              )}

              {/* Gender & Birthdate */}
              <div className="flex items-center gap-4 text-sm">
                {candidate.candidateGender && (
                  <div className="flex items-center gap-2">
                    <User className="w-4 h-4 text-neutral-400" />
                    <span className="text-neutral-700">{getGenderLabel(candidate.candidateGender)}</span>
                  </div>
                )}
                
                {candidate.candidateBirthdate && (
                  <div className="flex items-center gap-2">
                    <Calendar className="w-4 h-4 text-neutral-400" />
                    <span className="text-neutral-700">{formatDate(candidate.candidateBirthdate)}</span>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Description Section */}
      {candidate.candidateDescription && (
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-6 mb-6">
          <h2 className="text-lg font-semibold text-neutral-800 mb-4 flex items-center gap-2">
            <User className="w-5 h-5 text-primary" />
            Giới thiệu bản thân
          </h2>
          <div 
            className="text-neutral-700 prose prose-sm max-w-none"
            dangerouslySetInnerHTML={{ __html: candidate.candidateDescription }}
          />
        </div>
      )}

      {/* Education Section */}
      {candidate.candidateEducation && (
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-6 mb-6">
          <h2 className="text-lg font-semibold text-neutral-800 mb-4 flex items-center gap-2">
            <GraduationCap className="w-5 h-5 text-primary" />
            Học vấn
          </h2>
          <div 
            className="text-neutral-700 prose prose-sm max-w-none"
            dangerouslySetInnerHTML={{ __html: candidate.candidateEducation }}
          />
        </div>
      )}

      {/* Experience Section */}
      {candidate.candidateExp && (
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-6 mb-6">
          <h2 className="text-lg font-semibold text-neutral-800 mb-4 flex items-center gap-2">
            <Briefcase className="w-5 h-5 text-primary" />
            Kinh nghiệm làm việc
          </h2>
          <div 
            className="text-neutral-700 prose prose-sm max-w-none"
            dangerouslySetInnerHTML={{ __html: candidate.candidateExp }}
          />
        </div>
      )}

      {/* Skills Section */}
      {candidate.candidateSkills && (
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-6">
          <h2 className="text-lg font-semibold text-neutral-800 mb-4 flex items-center gap-2">
            <Award className="w-5 h-5 text-primary" />
            Kỹ năng
          </h2>
          <div 
            className="text-neutral-700 prose prose-sm max-w-none"
            dangerouslySetInnerHTML={{ __html: candidate.candidateSkills }}
          />
        </div>
      )}

      {/* Empty State */}
      {!candidate.candidateDescription && 
       !candidate.candidateEducation && 
       !candidate.candidateExp && 
       !candidate.candidateSkills && (
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-12 text-center">
          <User className="w-16 h-16 mx-auto text-neutral-300 mb-4" />
          <p className="text-neutral-600">
            Ứng viên chưa cập nhật thông tin chi tiết
          </p>
        </div>
      )}
    </div>
  );
};

export default CandidateDetail;
