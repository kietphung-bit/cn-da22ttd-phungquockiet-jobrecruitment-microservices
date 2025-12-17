package com.jobrecruitment.backend.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ApiResponse - DTO chuẩn cho tất cả API Response
 * 
 * Mô tả:
 * - Định dạng chuẩn cho mọi API response trong hệ thống
 * - Bao gồm: status (HTTP code), message (thông báo), data (dữ liệu)
 * - Generic Type T: Cho phép trả về bất kỳ kiểu dữ liệu nào
 * 
 * Format:
 * {
 *   "status": 200,
 *   "message": "Success",
 *   "data": { ... }
 * }
 * 
 * Quy ước HTTP Status Code:
 * - 200: Success (GET, PUT, DELETE thành công)
 * - 201: Created (POST tạo mới thành công)
 * - 400: Bad Request (Validation error)
 * - 401: Unauthorized (Chưa đăng nhập)
 * - 403: Forbidden (Không có quyền)
 * - 404: Not Found (Không tìm thấy resource)
 * - 500: Internal Server Error (Lỗi hệ thống)
 * 
 * Tham khảo: Section 5.1 - API Response Standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    /**
     * HTTP status code
     * - 200: Success
     * - 400: Bad Request
     * - 401: Unauthorized
     * - 403: Forbidden
     * - 404: Not Found
     * - 500: Internal Server Error
     */
    private int status;
    
    /**
     * Thông báo
     * - Tiếng Việt: "Đăng nhập thành công"
     * - English: "Success", "Error"
     */
    private String message;
    
    /**
     * Dữ liệu trả về
     * - Generic Type T: Có thể là Object, List, String...
     * - Có thể null (khi chỉ cần trả về message)
     */
    private T data;
    
    /**
     * Tạo success response với data
     * - Status: 200
     * - Message: "Success"
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", data);
    }
    
    /**
     * Tạo success response với custom message
     * - Status: 200
     * - Message: Tùy chỉnh
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }
    
    /**
     * Create success response without data
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(200, message, null);
    }
    
    /**
     * Create error response
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
    
    /**
     * Create error response with data
     */
    public static <T> ApiResponse<T> error(int status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }
}
