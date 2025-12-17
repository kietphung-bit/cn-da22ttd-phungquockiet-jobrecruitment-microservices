package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.dtos.request.CandidateRegisterRequest;
import com.jobrecruitment.backend.dtos.request.CompanyRegisterRequest;
import com.jobrecruitment.backend.dtos.request.LoginRequest;
import com.jobrecruitment.backend.dtos.response.AuthResponse;

/**
 * AuthService - Service interface cho xác thực và đăng ký
 * 
 * Chức năng:
 * - Đăng ký tài khoản Company (Role = DN)
 * - Đăng ký tài khoản Candidate (Role = UV)
 * - Đăng nhập và tạo JWT token
 * 
 * Business Rules:
 * - Mỗi User chỉ thuộc 1 Role (ADM/DN/UV)
 * - UserCode tự động generate và đồng bộ với CompanyCode/CandidateCode
 * - Password mã hóa bằng BCrypt (Spring Security)
 * - JWT token hết hạn sau 24h (config trong application.properties)
 * 
 * Security:
 * - Password never return in response
 * - JWT token chứa username, role, expiration
 * - Refresh token chưa implement (future feature)
 * 
 * @see AuthServiceImpl
 */
public interface AuthService {
    
    /**
     * Đăng ký tài khoản Company (Role = DN)
     * 
     * Sử dụng:
     * - API POST /api/v1/auth/register/company
     * 
     * Business Logic:
     * 1. Validate: Username (email) chưa tồn tại
     * 2. Tạo User với Role = DN, userCode = companyCode (synchronized)
     * 3. Mã hóa password bằng BCrypt
     * 4. Tạo Company với companyCode = DN + 8 số (unique)
     * 5. CompanyStatus = PENDING (chờ Admin duyệt)
     * 6. Return JWT token (auto login sau đăng ký)
     * 
     * @param request CompanyRegisterRequest (username, password, companyName, companyEmail...)
     * @return AuthResponse (jwt, user, role)
     * @throws ValidationException nếu username đã tồn tại
     */
    AuthResponse registerCompany(CompanyRegisterRequest request);
    
    /**
     * Đăng ký tài khoản Candidate (Role = UV)
     * 
     * Sử dụng:
     * - API POST /api/v1/auth/register/candidate
     * 
     * Business Logic:
     * 1. Validate: Username (email) chưa tồn tại
     * 2. Validate: Tuổi >= 18 (WorkingAge validator)
     * 3. Tạo User với Role = UV, userCode = candidateCode (synchronized)
     * 4. Mã hóa password bằng BCrypt
     * 5. Tạo Candidate với candidateCode = UV + 8 số (unique)
     * 6. Return JWT token (auto login sau đăng ký)
     * 
     * @param request CandidateRegisterRequest (username, password, fullName, birthDate, email, phone...)
     * @return AuthResponse (jwt, user, role)
     * @throws ValidationException nếu username đã tồn tại hoặc tuổi < 18
     */
    AuthResponse registerCandidate(CandidateRegisterRequest request);
    
    /**
     * Đăng nhập và tạo JWT token
     * 
     * Sử dụng:
     * - API POST /api/v1/auth/login
     * 
     * Business Logic:
     * 1. Xác thực username/password qua Spring Security
     * 2. Kiểm tra User có bị khóa không (User.locked = false)
     * 3. Tạo JWT token chứa: username, role, expiration (24h)
     * 4. Return JWT token + user info
     * 
     * @param request LoginRequest (username, password)
     * @return AuthResponse (jwt, user, role)
     * @throws BadCredentialsException nếu username/password sai
     * @throws ValidationException nếu User bị khóa
     */
    AuthResponse login(LoginRequest request);
}
