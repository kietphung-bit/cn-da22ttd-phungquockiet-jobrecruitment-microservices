package com.jobrecruitment.backend.exceptions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global Exception Handler - Bộ xử lý ngoại lệ toàn cục
 * 
 * Chức năng:
 * - Bắt và xử lý tất cả các exception xảy ra trong ứng dụng
 * - Trả về response chuẩn với HTTP status code phù hợp
 * - Ghi log lỗi để hỗ trợ debug và bảo trì
 * 
 * API Response Standard (Section 5.1):
 * {
 *   "status": 200,
 *   "message": "Success",
 *   "data": { ... }
 * }
 * 
 * Lỗi trả về dạng:
 * {
 *   "status": 400/401/404/500,
 *   "message": "Error message",
 *   "timestamp": "2025-12-17T18:00:00",
 *   "path": "/api/v1/..."
 * }
 * 
 * Các exception được xử lý:
 * - ResourceNotFoundException: 404 NOT FOUND
 * - ValidationException: 400 BAD REQUEST
 * - MethodArgumentNotValidException: 400 BAD REQUEST (Jakarta Validation)
 * - AuthenticationException: 401 UNAUTHORIZED
 * - BadCredentialsException: 401 UNAUTHORIZED
 * - IllegalStateException: 409 CONFLICT
 * - IllegalArgumentException: 400 BAD REQUEST
 * - Exception: 500 INTERNAL SERVER ERROR (Fallback)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Xử lý ResourceNotFoundException khi không tìm thấy resource.
     * 
     * Trường hợp sử dụng:
     * - Tìm job theo ID nhưng không tồn tại
     * - Tìm company theo ID nhưng đã bị xóa
     * - Tìm user theo username nhưng không có trong database
     * 
     * @param ex ResourceNotFoundException object chứa thông tin lỗi
     * @param request WebRequest chứa thông tin request (URL path, method, etc.)
     * @return ResponseEntity với HTTP 404 NOT FOUND và error details
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, 
            WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Xử lý ValidationException khi dữ liệu không hợp lệ.
     * 
     * Trường hợp sử dụng:
     * - Email đã tồn tại trong hệ thống (duplicate)
     * - Phạm vi lương không hợp lệ (minSalary > maxSalary)
     * - Định dạng số điện thoại sai (không phải 10-11 số)
     * - Ngày không hợp lệ (StartDate > EndDate)
     * 
     * @param ex ValidationException object chứa thông tin lỗi
     * @param request WebRequest chứa thông tin request
     * @return ResponseEntity với HTTP 400 BAD REQUEST và error details
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            ValidationException ex, 
            WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        if (ex.getField() != null) {
            Map<String, Object> details = new HashMap<>();
            details.put("field", ex.getField());
            details.put("rejectedValue", ex.getRejectedValue());
            response.put("details", details);
        }
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Xử lý Jakarta Validation errors từ @Valid annotation.
     * 
     * Tự động bắt lỗi từ các annotation trong DTO:
     * - @NotNull, @NotBlank: Trường bắt buộc
     * - @Email: Định dạng email
     * - @Min, @Max: Giới hạn số
     * - @Size: Độ dài chuỗi
     * - @Pattern: Regex pattern
     * 
     * @param ex MethodArgumentNotValidException chứa danh sách field errors
     * @param request WebRequest chứa thông tin request
     * @return ResponseEntity với HTTP 400 BAD REQUEST và map các field lỗi
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", "Validation failed");
        response.put("errors", errors);
        response.put("timestamp", LocalDateTime.now());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Xử lý AuthenticationException khi xác thực thất bại.
     * 
     * Trường hợp sử dụng:
     * - JWT token không hợp lệ hoặc đã hết hạn
     * - User chưa đăng nhập (missing token)
     * - Token signature sai (bị giả mạo)
     * 
     * @param ex AuthenticationException chứa thông tin lỗi xác thực
     * @param request WebRequest chứa thông tin request
     * @return ResponseEntity với HTTP 401 UNAUTHORIZED
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex,
            WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("message", "Authentication failed: " + ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Xử lý BadCredentialsException khi đăng nhập sai thông tin.
     * 
     * Trường hợp sử dụng:
     * - Email không tồn tại trong hệ thống
     * - Mật khẩu không khớp với mã BCrypt hash trong database
     * - Account bị khóa/vô hiệu hóa
     * 
     * @param ex BadCredentialsException từ Spring Security
     * @param request WebRequest chứa thông tin request
     * @return ResponseEntity với HTTP 401 UNAUTHORIZED và message "Invalid username or password"
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException ex,
            WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("message", "Invalid username or password");
        response.put("timestamp", LocalDateTime.now());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Xử lý IllegalStateException khi trạng thái không hợp lệ.
     * 
     * Trường hợp sử dụng:
     * - Cố gắng apply vào job đã đóng (JobStatus = CLOSED)
     * - Cố gắng chỉnh sửa profile đang bị khóa
     * - Thao tác không đúng workflow (ví dụ: approve job đã approved)
     * 
     * @param ex IllegalStateException chứa thông tin lỗi
     * @param request WebRequest chứa thông tin request
     * @return ResponseEntity với HTTP 409 CONFLICT
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(
            IllegalStateException ex,
            WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
    
    /**
     * Xử lý IllegalArgumentException khi tham số không hợp lệ.
     * 
     * Trường hợp sử dụng:
     * - ID âm (jobId < 0)
     * - Enum không hợp lệ (JobStatus = "INVALID")
     * - Tham số null cho method không chấp nhận null
     * 
     * @param ex IllegalArgumentException chứa thông tin lỗi
     * @param request WebRequest chứa thông tin request
     * @return ResponseEntity với HTTP 400 BAD REQUEST
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Xử lý tất cả các exception chưa được xử lý (Fallback handler).
     * 
     * Trường hợp sử dụng:
     * - Lỗi không lường trước trong code
     * - Database connection error
     * - OutOfMemoryError
     * - Null Pointer Exception
     * 
     * Lưu ý: Exception stack trace sẽ được in ra console để debug.
     * 
     * @param ex Exception gốc (bất kỳ loại nào)
     * @param request WebRequest chứa thông tin request
     * @return ResponseEntity với HTTP 500 INTERNAL SERVER ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex,
            WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("message", "An unexpected error occurred");
        response.put("error", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        // Log the exception for debugging
        ex.printStackTrace();
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
