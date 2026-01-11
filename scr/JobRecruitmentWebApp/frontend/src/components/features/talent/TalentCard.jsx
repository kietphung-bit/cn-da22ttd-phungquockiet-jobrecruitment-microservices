import React from 'react';
import { 
  MapPin, 
  DollarSign, 
  Calendar, 
  Eye, 
  Mail,
  Phone,
  Send,
  CheckCircle2
} from 'lucide-react';

/**
 * TalentCard - Card component hiển thị thông tin tóm tắt ứng viên
 * 
 * Component này hiển thị thông tin tóm tắt của ứng viên trong danh sách tìm kiếm.
 * Có thể click vào card để xem chi tiết hoặc click nút để gửi lời mời.
 * 
 * Props:
 * - post: Object chứa thông tin ứng viên
 * - isInvited: Boolean cho biết đã gửi lời mời chưa
 * - onCardClick: Function xử lý click vào card
 * - onInviteClick: Function xử lý click nút mời ứng tuyển
 * - formatSalary: Function format tiền lương
 * - formatLocation: Function format địa điểm
 */
const TalentCard = ({ 
  post, 
  isInvited, 
  onCardClick, 
  onInviteClick,
  formatSalary,
  formatLocation
}) => {
  return (
    <div 
      onClick={() => onCardClick(post)}
      className="bg-white rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow duration-200 cursor-pointer"
    >
      {/* Card Header */}
      <div className="p-6 border-b border-gray-100">
        <div className="flex items-start gap-4">
          <img 
            src={post.avatarUrl || 'https://via.placeholder.com/150'} 
            alt={post.candidateName}
            className="w-16 h-16 rounded-full border-2 border-blue-100 object-cover"
          />
          <div className="flex-1 min-w-0">
            <h3 className="font-bold text-lg text-gray-900 mb-1 truncate">
              {post.candidateName}
            </h3>
            <div className="flex items-center gap-4 text-sm text-gray-600 mb-2">
              {post.views !== undefined && (
                <div className="flex items-center gap-1">
                  <Eye className="w-4 h-4" />
                  <span>{post.views} lượt xem</span>
                </div>
              )}
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
            <DollarSign className="w-4 h-4 text-green-600 flex-shrink-0" />
            <span className="font-medium text-green-600 truncate">
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
        <p className="text-sm text-gray-600 mb-4 line-clamp-3">
          {post.introduction}
        </p>

        {/* Contact Info - Displayed for employers */}
        {(post.candidateEmail || post.candidatePhone) && (
          <div className="bg-gray-50 rounded-lg p-4 mb-4 space-y-2">
            {post.candidateEmail && (
              <div className="flex items-center gap-2 text-sm text-gray-700">
                <Mail className="w-4 h-4 text-gray-400" />
                <span className="truncate">{post.candidateEmail}</span>
              </div>
            )}
            {post.candidatePhone && (
              <div className="flex items-center gap-2 text-sm text-gray-700">
                <Phone className="w-4 h-4 text-gray-400" />
                <span>{post.candidatePhone}</span>
              </div>
            )}
          </div>
        )}

        {/* Action Button */}
        <button
          onClick={(e) => {
            e.stopPropagation(); // Prevent card click
            if (!isInvited) {
              onInviteClick(post.id, post.candidateName);
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
  );
};

export default TalentCard;
