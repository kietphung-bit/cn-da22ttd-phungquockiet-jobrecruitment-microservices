import React from 'react';
import { Link } from 'react-router-dom';
import { Building2 } from 'lucide-react';

/**
 * CompanyCard Component
 * Hình vuông với logo ở giữa và tên công ty bên dưới
 * 
 * @param {Object} props
 * @param {Object} props.company - Đối tượng công ty với companyId, name, logo
 */
const CompanyCard = ({ company }) => {
  // Hàm để tạo URL đầy đủ cho file tải lên từ backend
  const getBackendFileUrl = (filePath) => {
    if (!filePath) return null;
    if (filePath.startsWith('http')) return filePath;
    const baseUrl = import.meta.env.VITE_API_URL?.replace('/api/v1', '') || 'http://localhost:5000';
    return `${baseUrl}${filePath}`;
  };

  const {
    companyId = 1,
    name = 'Company Name',
    logo = null,
    jobCount = 0,
  } = company || {};

  return (
    <Link
      to={`/companies/${companyId}`}
      className="card p-6 hover:shadow-xl transition-all duration-300 flex flex-col items-center justify-center text-center"
    >
      {/* Company Logo */}
      <div className="w-24 h-24 bg-neutral-200 rounded-lg flex items-center justify-center overflow-hidden mb-4">
        {logo ? (
          <img
            src={getBackendFileUrl(logo)}
            alt={name}
            className="w-full h-full object-cover"
          />
        ) : (
          <Building2 className="w-12 h-12 text-neutral-400" />
        )}
      </div>

      {/* Company Name */}
      <h3 className="text-lg font-semibold text-neutral-900 mb-2">{name}</h3>

      {/* Job Count */}
      {jobCount > 0 && (
        <p className="text-sm text-neutral-600">
          {jobCount} việc làm
        </p>
      )}
    </Link>
  );
};

export default CompanyCard;