/**
 * Danh sách tỉnh thành Việt Nam
 * 
 * Danh sách tĩnh gồm 63 tỉnh thành và thành phố trực thuộc trung ương ở Việt Nam
 * được tổ chức theo vùng để cải thiện trải nghiệm người dùng trong các dropdown
 * 
 * Cách sử dụng:
 * import { VIETNAM_PROVINCES, VIETNAM_PROVINCES_BY_REGION } from '@/data/provinces';
 * 
 * Lưu ý: Đây là danh sách tĩnh để tránh phụ thuộc vào API bên ngoài
 * Cập nhật lần cuối: 2026 (trước khi tái cấu trúc hành chính thành 34 tỉnh)
 */

// Danh sách phẳng tất cả các tỉnh (được sắp xếp theo bảng chữ cái)
export const VIETNAM_PROVINCES = [
  "An Giang",
  "Bà Rịa - Vũng Tàu",
  "Bắc Giang",
  "Bắc Kạn",
  "Bạc Liêu",
  "Bắc Ninh",
  "Bến Tre",
  "Bình Định",
  "Bình Dương",
  "Bình Phước",
  "Bình Thuận",
  "Cà Mau",
  "Cần Thơ",
  "Cao Bằng",
  "Đà Nẵng",
  "Đắk Lắk",
  "Đắk Nông",
  "Điện Biên",
  "Đồng Nai",
  "Đồng Tháp",
  "Gia Lai",
  "Hà Giang",
  "Hà Nam",
  "Hà Nội",
  "Hà Tĩnh",
  "Hải Dương",
  "Hải Phòng",
  "Hậu Giang",
  "Hòa Bình",
  "Hưng Yên",
  "Khánh Hòa",
  "Kiên Giang",
  "Kon Tum",
  "Lai Châu",
  "Lâm Đồng",
  "Lạng Sơn",
  "Lào Cai",
  "Long An",
  "Nam Định",
  "Nghệ An",
  "Ninh Bình",
  "Ninh Thuận",
  "Phú Thọ",
  "Phú Yên",
  "Quảng Bình",
  "Quảng Nam",
  "Quảng Ngãi",
  "Quảng Ninh",
  "Quảng Trị",
  "Sóc Trăng",
  "Sơn La",
  "Tây Ninh",
  "Thái Bình",
  "Thái Nguyên",
  "Thanh Hóa",
  "Thừa Thiên Huế",
  "Tiền Giang",
  "TP Hồ Chí Minh",
  "Trà Vinh",
  "Tuyên Quang",
  "Vĩnh Long",
  "Vĩnh Phúc",
  "Yên Bái"
];

// Danh sách các tỉnh được nhóm theo vùng (để cải thiện trải nghiệm người dùng trong các dropdown có nhóm)
export const VIETNAM_PROVINCES_BY_REGION = {
  "Thành phố trực thuộc TW": [
    "Hà Nội",
    "TP Hồ Chí Minh",
    "Đà Nẵng",
    "Hải Phòng",
    "Cần Thơ"
  ],
  "Miền Bắc": [
    "Bắc Giang",
    "Bắc Kạn",
    "Bắc Ninh",
    "Cao Bằng",
    "Điện Biên",
    "Hà Giang",
    "Hà Nam",
    "Hà Tĩnh",
    "Hải Dương",
    "Hòa Bình",
    "Hưng Yên",
    "Lai Châu",
    "Lạng Sơn",
    "Lào Cai",
    "Nam Định",
    "Nghệ An",
    "Ninh Bình",
    "Phú Thọ",
    "Quảng Ninh",
    "Sơn La",
    "Thái Bình",
    "Thái Nguyên",
    "Thanh Hóa",
    "Tuyên Quang",
    "Vĩnh Phúc",
    "Yên Bái"
  ],
  "Miền Trung": [
    "Bình Định",
    "Bình Thuận",
    "Đắk Lắk",
    "Đắk Nông",
    "Gia Lai",
    "Khánh Hòa",
    "Kon Tum",
    "Lâm Đồng",
    "Ninh Thuận",
    "Phú Yên",
    "Quảng Bình",
    "Quảng Nam",
    "Quảng Ngãi",
    "Quảng Trị",
    "Thừa Thiên Huế"
  ],
  "Miền Nam": [
    "An Giang",
    "Bà Rịa - Vũng Tàu",
    "Bạc Liêu",
    "Bến Tre",
    "Bình Dương",
    "Bình Phước",
    "Cà Mau",
    "Đồng Nai",
    "Đồng Tháp",
    "Hậu Giang",
    "Kiên Giang",
    "Long An",
    "Sóc Trăng",
    "Tây Ninh",
    "Tiền Giang",
    "Trà Vinh",
    "Vĩnh Long"
  ]
};

// Hàm trợ giúp để lấy tất cả các tỉnh dưới dạng các tùy chọn cho các input select
export const getProvinceOptions = () => {
  return VIETNAM_PROVINCES.map(province => ({
    value: province,
    label: province
  }));
};

// Hàm trợ giúp để lấy các tỉnh được nhóm theo vùng (cho optgroup trong select)
export const getGroupedProvinceOptions = () => {
  return Object.entries(VIETNAM_PROVINCES_BY_REGION).map(([region, provinces]) => ({
    label: region,
    options: provinces.map(province => ({
      value: province,
      label: province
    }))
  }));
};

export default VIETNAM_PROVINCES;
