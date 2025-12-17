package com.jobrecruitment.backend.controllers;

import com.jobrecruitment.backend.dtos.request.CandidateRegisterRequest;
import com.jobrecruitment.backend.dtos.request.CompanyRegisterRequest;
import com.jobrecruitment.backend.dtos.request.LoginRequest;
import com.jobrecruitment.backend.dtos.response.ApiResponse;
import com.jobrecruitment.backend.dtos.response.AuthResponse;
import com.jobrecruitment.backend.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthControllerV1 - RESTful Authentication API (Version 1)
 * Xác thực và Đăng ký tài khoản
 * 
 * Base Path: /api/v1/auth
 * 
 * Chức năng chính:
 * 1. Đăng nhập (Login): Xác thực thông tin và trả về JWT token
 * 2. Đăng ký Doanh nghiệp (Employer Registration): Tạo tài khoản cho công ty tuyển dụng
 * 3. Đăng ký Ứng viên (Candidate Registration): Tạo tài khoản cho người tìm việc
 * 
 * Đặc điểm kỹ thuật:
 * - JWT Authentication: Stateless, không cần session
 * - BCrypt Password Hashing: Bảo mật mật khẩu
 * - UserCode Synchronization: UserCode = CompanyCode hoặc CandidateCode (Section 4.5.C)
 * - RESTful Response Standard: ApiResponse<T> wrapper cho tất cả responses
 * 
 * HTTP Status Codes:
 * - 200 OK: Đăng nhập thành công
 * - 201 Created: Đăng ký thành công
 * - 400 Bad Request: Dữ liệu không hợp lệ (validation error, email exists)
 * - 401 Unauthorized: Sai email hoặc mật khẩu
 * - 404 Not Found: Role không tồn tại (seeding issue)
 * 
 * Endpoints:
 * 1. POST   /api/v1/auth/login                 - Đăng nhập user
 * 2. POST   /api/v1/auth/register/employer     - Đăng ký doanh nghiệp
 * 3. POST   /api/v1/auth/register/candidate    - Đăng ký ứng viên
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication V1", description = "RESTful Authentication and Registration APIs")
public class AuthControllerV1 {

    private final AuthService authService;

    /**
     * Đăng nhập người dùng vào hệ thống.
     * 
     * Quy trình xử lý:
     * 1. Nhận LoginRequest chứa username (email) và password (plain text)
     * 2. AuthenticationManager xác thực thông tin với database (BCrypt)
     * 3. Nếu thành công, tạo JWT token với thông tin user
     * 4. Trả về AuthResponse chứa token và thông tin user
     * 
     * Request Body:
     * - username: Địa chỉ email đã đăng ký (varchar, unique)
     * - password: Mật khẩu dạng plain text (sẽ được so sánh với BCrypt hash)
     * 
     * Response (AuthResponse):
     * - token: JWT Bearer token để xác thực các request tiếp theo
     * - username: Email của user
     * - userCode: Mã người dùng (AD/DN/UV + 8 số)
     * - roleCode: Mã quyền (ADM, DN, UV)
     * - roleName: Tên quyền ("Quản trị viên", "Nhà tuyển dụng", "Ứng viên")
     * 
     * @param request LoginRequest chứa thông tin đăng nhập
     * @return ResponseEntity<ApiResponse<AuthResponse>> với HTTP 200 OK
     * @throws BadCredentialsException Nếu email hoặc mật khẩu không đúng
     */
    @PostMapping("/login")
    @Operation(
        summary = "User Login",
        description = "Authenticate user with email and password. Returns JWT token upon successful authentication. " +
                     "Token must be included in subsequent requests as 'Authorization: Bearer <token>'."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login successful - JWT token generated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid email or password"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation error (missing fields)"
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody @Parameter(description = "Login credentials (username=email, password)", required = true) 
            LoginRequest request
    ) {
        log.info("POST /api/v1/auth/login - Username: {}", request.getUsername());
        
        AuthResponse authResponse = authService.login(request);
        ApiResponse<AuthResponse> response = ApiResponse.success("Login successful", authResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Đăng ký tài khoản Doanh nghiệp (Đăng ký tài khoản Employer).
     * 
     * Quy trình xử lý (UserCode Synchronization - Section 4.5.C.2):
     * 1. Kiểm tra email chưa tồn tại trong hệ thống
     * 2. Tạo CompanyCode: "DN" + 8 chữ số ngẫu nhiên (ví dụ: DN12345678)
     * 3. Tạo bản ghi Company với CompanyCode vừa tạo
     * 4. Tạo bản ghi User với UserCode = CompanyCode (đồng bộ mã)
     * 5. Gán Role = "DN" (Doanh nghiệp)
     * 6. Mã hóa password bằng BCrypt
     * 7. CompanyStatus = PENDING (Chờ quản trị viên duyệt)
     * 8. Tạo JWT token và trả về cho phép login ngay
     * 
     * Request Body (CompanyRegisterRequest):
     * - username: Email đăng ký (sẽ trở thành User.Username)
     * - password: Mật khẩu plain text (sẽ được BCrypt hash)
     * - companyName: Tên công ty (RBHT validation - chỉ chữ và khoảng trắng)
     * - companyEmail: Email liên hệ công ty (RBEML validation)
     * - companyAddress: Địa chỉ công ty (optional)
     * - companyWebsite: Website công ty (optional)
     * - companyDescription: Mô tả công ty (optional)
     * - logoURL: URL logo công ty (optional)
     * 
     * Validation Rules:
     * - RBHT: Tên công ty chỉ chứa chữ và khoảng trắng
     * - RBEML: Email hợp lệ với @ và domain
     * 
     * @param request CompanyRegisterRequest chứa thông tin đăng ký
     * @return ResponseEntity<ApiResponse<AuthResponse>> với HTTP 201 Created
     * @throws ValidationException Nếu email đã tồn tại hoặc dữ liệu không hợp lệ
     * @throws ResourceNotFoundException Nếu Role DN chưa được seed vào database
     */
    @PostMapping("/register/employer")
    @Operation(
        summary = "Employer Registration",
        description = "Register a new company/employer account (Role: DN - Doanh nghiệp). " +
                     "Generates CompanyCode (DN + 8 digits) and synchronizes it with UserCode. " +
                     "Initial status: PENDING (requires admin approval). " +
                     "Returns JWT token for immediate login."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Company registered successfully - JWT token issued",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation error or email already exists"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Role DN not found in database (seeding issue)"
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> registerEmployer(
            @Valid @RequestBody @Parameter(description = "Company registration data", required = true) 
            CompanyRegisterRequest request
    ) {
        log.info("POST /api/v1/auth/register/employer - Email: {}, Company: {}", 
                request.getUsername(), request.getCompanyName());
        
        AuthResponse authResponse = authService.registerCompany(request);
        ApiResponse<AuthResponse> response = ApiResponse.success("Company registration successful", authResponse);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Đăng ký tài khoản Ứng viên (Candidate Registration).
     * 
     * Quy trình xử lý (UserCode Synchronization - Section 4.5.C.3):
     * 1. Kiểm tra email chưa tồn tại trong hệ thống
     * 2. Kiểm tra tuổi >= 18 (RBNS - Working Age validation)
     * 3. Tạo CandidateCode: "UV" + 8 chữ số ngẫu nhiên (ví dụ: UV87654321)
     * 4. Tạo bản ghi Candidate với CandidateCode vừa tạo
     * 5. Tạo bản ghi User với UserCode = CandidateCode (đồng bộ mã)
     * 6. Gán Role = "UV" (Ứng viên)
     * 7. Mã hóa password bằng BCrypt
     * 8. Tạo JWT token và trả về cho phép login ngay
     * 
     * Request Body (CandidateRegisterRequest):
     * - username: Email đăng ký (sẽ trở thành User.Username)
     * - password: Mật khẩu plain text (sẽ được BCrypt hash)
     * - candidateName: Họ tên đầy đủ (RBHT validation - chỉ chữ và khoảng trắng)
     * - candidateBirthdate: Ngày sinh (RBNS validation - phải đủ 18 tuổi)
     * - candidatePhone: Số điện thoại (RBSDT validation - 10-11 chữ số)
     * - candidateEmail: Email cá nhân (optional, RBEML validation nếu có)
     * - candidateGender: Giới tính (optional, MALE/FEMALE/OTHER)
     * - candidateEducation: Học vấn (optional)
     * - candidateExp: Kinh nghiệm làm việc (optional)
     * - candidateSkills: Kỹ năng (optional)
     * 
     * Validation Rules:
     * - RBHT: Tên chỉ chứa chữ và khoảng trắng (hỗ trợ tiếng Việt)
     * - RBNS: Tuổi làm việc >= 18 (Hiện tại - Năm sinh >= 18)
     * - RBSDT: Số điện thoại 10-11 chữ số
     * - RBEML: Email hợp lệ với @ và domain
     * 
     * @param request CandidateRegisterRequest chứa thông tin đăng ký
     * @return ResponseEntity<ApiResponse<AuthResponse>> với HTTP 201 Created
     * @throws ValidationException Nếu email đã tồn tại, tuổi < 18, hoặc dữ liệu không hợp lệ
     * @throws ResourceNotFoundException Nếu Role UV chưa được seed vào database
     */
    @PostMapping("/register/candidate")
    @Operation(
        summary = "Candidate Registration",
        description = "Register a new candidate/job seeker account (Role: UV - Ứng viên). " +
                     "Generates CandidateCode (UV + 8 digits) and synchronizes it with UserCode. " +
                     "Age validation: Must be 18 years or older (RBNS rule). " +
                     "Returns JWT token for immediate login."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Candidate registered successfully - JWT token issued",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation error, email exists, or age < 18"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Role UV not found in database (seeding issue)"
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> registerCandidate(
            @Valid @RequestBody @Parameter(description = "Candidate registration data", required = true) 
            CandidateRegisterRequest request
    ) {
        log.info("POST /api/v1/auth/register/candidate - Email: {}, Name: {}", 
                request.getUsername(), request.getCandidateName());
        
        AuthResponse authResponse = authService.registerCandidate(request);
        ApiResponse<AuthResponse> response = ApiResponse.success("Candidate registration successful", authResponse);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
