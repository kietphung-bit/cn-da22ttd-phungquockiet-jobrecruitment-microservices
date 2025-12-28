package com.jobrecruitment.backend.services.impl;

import java.time.LocalDateTime;

import com.jobrecruitment.backend.dtos.request.CandidateRegisterRequest;
import com.jobrecruitment.backend.dtos.request.ChangePasswordRequest;
import com.jobrecruitment.backend.dtos.request.CompanyRegisterRequest;
import com.jobrecruitment.backend.dtos.request.LoginRequest;
import com.jobrecruitment.backend.dtos.response.AuthResponse;
import com.jobrecruitment.backend.entities.Candidate;
import com.jobrecruitment.backend.entities.Company;
import com.jobrecruitment.backend.entities.Role;
import com.jobrecruitment.backend.entities.User;
import com.jobrecruitment.backend.enums.CompanyStatus;
import com.jobrecruitment.backend.exceptions.ResourceNotFoundException;
import com.jobrecruitment.backend.exceptions.ValidationException;
import com.jobrecruitment.backend.repositories.CandidateRepository;
import com.jobrecruitment.backend.repositories.CompanyRepository;
import com.jobrecruitment.backend.repositories.RoleRepository;
import com.jobrecruitment.backend.repositories.UserRepository;
import com.jobrecruitment.backend.services.AuthService;
import com.jobrecruitment.backend.utils.CodeGenerator;
import com.jobrecruitment.backend.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthServiceImpl - Service xử lý logic nghiệp vụ cho Xác thực và Đăng ký.
 * 
 * Chức năng chính:
 * - Đăng ký Doanh nghiệp (Company Registration)
 * - Đăng ký Ứng viên (Candidate Registration)
 * - Đăng nhập User (User Login)
 * 
 * Đặc điểm kỹ thuật:
 * - Transaction management với @Transactional
 * - UserCode synchronization (UserCode = CompanyCode/CandidateCode)
 * - BCrypt password hashing
 * - JWT token generation
 * - Code generation với uniqueness check
 * 
 * Dependencies:
 * - UserRepository: Quản lý User entity
 * - RoleRepository: Lấy thông tin Role (ADM, DN, UV)
 * - CompanyRepository: Quản lý Company profile
 * - CandidateRepository: Quản lý Candidate profile
 * - PasswordEncoder: Mã hóa password (BCrypt)
 * - AuthenticationManager: Xác thực login credentials
 * - JwtUtils: Tạo JWT token
 * - CodeGenerator: Tạo mã ngẫu nhiên (CompanyCode, CandidateCode)
 * 
 * @see AuthService
 * @see AuthControllerV1
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final CodeGenerator codeGenerator;

    /**
     * Đăng ký tài khoản Doanh nghiệp mới.
     * 
     * Quy trình thực hiện (Transaction - Rollback nếu lỗi):
     * 1. Kiểm tra email chưa tồn tại (User table và Company table)
     * 2. Lấy Role "DN" từ database
     * 3. Tạo CompanyCode unique: "DN" + 8 số ngẫu nhiên
     * 4. Tạo User entity với:
     *    - UserCode = CompanyCode (đồng bộ mã - Section 4.5.C.2)
     *    - Username = CompanyEmail
     *    - Password = BCrypt hash
     *    - Role = DN
     * 5. Lưu User vào database
     * 6. Tạo Company entity liên kết với User
     * 7. CompanyStatus = PENDING (chờ admin duyệt)
     * 8. Lưu Company vào database
     * 9. Tạo JWT token cho user vừa đăng ký
     * 10. Trả về AuthResponse với token và thông tin user
     * 
     * @param request CompanyRegisterRequest chứa thông tin đăng ký
     * @return AuthResponse chứa JWT token và thông tin user/company
     * @throws ValidationException Nếu email đã tồn tại
     * @throws ResourceNotFoundException Nếu Role "DN" chưa được seed
     */
    @Override
    @Transactional
    public AuthResponse registerCompany(CompanyRegisterRequest request) {
        // Kiểm tra email chưa tồn tại
        if (userRepository.existsByUsername(request.getCompanyEmail())) {
            throw new ValidationException("Email already registered");
        }
        
        if (companyRepository.existsByCompanyEmail(request.getCompanyEmail())) {
            throw new ValidationException("Company email already exists");
        }

        // Get DN role
        Role dnRole = roleRepository.findByRoleCode("DN")
                .orElseThrow(() -> new ResourceNotFoundException("Role DN not found"));

        // Generate unique CompanyCode
        String companyCode = codeGenerator.generateCompanyCode(code -> companyRepository.existsByCompanyCode(code));

        // Create User with UserCode = CompanyCode (Section 4.5.C)
        User user = new User();
        user.setUserCode(companyCode);
        user.setUsername(request.getCompanyEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(dnRole);
        User savedUser = userRepository.save(user);

        // Create Company
        Company company = new Company();
        company.setUser(savedUser);
        company.setCompanyCode(companyCode);
        company.setCompanyName(request.getCompanyName());
        company.setCompanyDescription(request.getCompanyDescription());
        company.setCompanyAddress(request.getCompanyAddress());
        company.setCompanyWebsite(request.getCompanyWebsite());
        company.setCompanyEmail(request.getCompanyEmail());
        company.setLogoURL(request.getLogoURL());
        company.setCompanyStatus(CompanyStatus.PENDING);
        companyRepository.save(company);

        // Generate JWT token
        String token = jwtUtils.generateToken(savedUser.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(savedUser.getUsername())
                .userCode(savedUser.getUserCode())
                .roleCode(dnRole.getRoleCode())
                .roleName(dnRole.getRoleName())
                .message("Company registration successful. Status: PENDING")
                .build();
    }

    /**
     * Đăng ký tài khoản Ứng viên mới.
     * 
     * Quy trình thực hiện (Transaction - Rollback nếu lỗi):
     * 1. Kiểm tra email chưa tồn tại (User table và Candidate table)
     * 2. Lấy Role "UV" từ database
     * 3. Tạo CandidateCode unique: "UV" + 8 số ngẫu nhiên
     * 4. Tạo User entity với:
     *    - UserCode = CandidateCode (đồng bộ mã - Section 4.5.C.3)
     *    - Username = CandidateEmail
     *    - Password = BCrypt hash
     *    - Role = UV
     * 5. Lưu User vào database
     * 6. Tạo Candidate entity liên kết với User
     * 7. Lưu Candidate vào database
     * 8. Tạo JWT token cho user vừa đăng ký
     * 9. Trả về AuthResponse với token và thông tin user/candidate
     * 
     * Lưu ý: RBNS validation (tuổi >= 18) được xử lý trong DTO validation (@WorkingAge annotation)
     * 
     * @param request CandidateRegisterRequest chứa thông tin đăng ký
     * @return AuthResponse chứa JWT token và thông tin user/candidate
     * @throws ValidationException Nếu email đã tồn tại
     * @throws ResourceNotFoundException Nếu Role "UV" chưa được seed
     */
    @Override
    @Transactional
    public AuthResponse registerCandidate(CandidateRegisterRequest request) {
        // Kiểm tra email chưa tồn tại
        if (userRepository.existsByUsername(request.getCandidateEmail())) {
            throw new ValidationException("Email already registered");
        }
        
        if (candidateRepository.existsByCandidateEmail(request.getCandidateEmail())) {
            throw new ValidationException("Candidate email already exists");
        }

        // Get UV role
        Role uvRole = roleRepository.findByRoleCode("UV")
                .orElseThrow(() -> new ResourceNotFoundException("Role UV not found"));

        // Generate unique CandidateCode
        String candidateCode = codeGenerator.generateCandidateCode(code -> candidateRepository.existsByCandidateCode(code));

        // Create User with UserCode = CandidateCode (Section 4.5.C)
        User user = new User();
        user.setUserCode(candidateCode);
        user.setUsername(request.getCandidateEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(uvRole);
        User savedUser = userRepository.save(user);

        // Create Candidate
        Candidate candidate = new Candidate();
        candidate.setUser(savedUser);
        candidate.setCandidateCode(candidateCode);
        candidate.setCandidateName(request.getCandidateName());
        candidate.setCandidateDescription(request.getCandidateDescription());
        candidate.setCandidateGender(request.getCandidateGender());
        candidate.setCandidateBirthdate(request.getCandidateBirthdate());
        candidate.setCandidatePhone(request.getCandidatePhone());
        candidate.setCandidateEmail(request.getCandidateEmail());
        candidate.setCandidateEducation(request.getCandidateEducation());
        candidate.setCandidateExp(request.getCandidateExp());
        candidate.setCandidateSkills(request.getCandidateSkills());
        candidateRepository.save(candidate);

        // Generate JWT token
        String token = jwtUtils.generateToken(savedUser.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(savedUser.getUsername())
                .userCode(savedUser.getUserCode())
                .roleCode(uvRole.getRoleCode())
                .roleName(uvRole.getRoleName())
                .message("Candidate registration successful")
                .build();
    }

    /**
     * Đăng nhập user vào hệ thống.
     * 
     * Quy trình thực hiện:
     * 1. Gọi AuthenticationManager.authenticate() với username và password
     * 2. Spring Security tự động:
     *    - Load user từ database (UserDetailsService)
     *    - So sánh password với BCrypt hash
     *    - Nếu khớp, trả về Authentication object
     *    - Nếu không khớp, throw BadCredentialsException
     * 3. Lấy thông tin user từ database theo username
     * 4. Tạo JWT token với username
     * 5. Trả về AuthResponse với token và thông tin user
     * 
     * Lưu ý:
     * - Method này KHÔNG cần @Transactional (chỉ read data)
     * - BadCredentialsException sẽ được GlobalExceptionHandler bắt và trả về 401
     * 
     * @param request LoginRequest chứa username (email) và password
     * @return AuthResponse chứa JWT token và thông tin user
     * @throws BadCredentialsException Nếu username hoặc password sai (xử lý bởi Spring Security)
     * @throws ResourceNotFoundException Nếu user không tồn tại (không bình thường - chỉ xảy ra nếu database inconsistent)
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        // Xác thực user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Get authenticated username from the authentication object
        String authenticatedUsername = authentication.getName();

        // Get authenticated user
        User user = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Generate JWT token
        String token = jwtUtils.generateToken(authenticatedUsername);

        // Build base response
        AuthResponse.AuthResponseBuilder responseBuilder = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .userCode(user.getUserCode())
                .roleCode(user.getRole().getRoleCode())
                .roleName(user.getRole().getRoleName())
                .message("Login successful");

        // Add role-specific information
        if ("UV".equals(user.getRole().getRoleCode())) {
            // Candidate - fetch candidate info
            candidateRepository.findByUserUserId(user.getUserId())
                    .ifPresent(candidate -> {
                        responseBuilder.candidateName(candidate.getCandidateName());
                        responseBuilder.candidateCode(candidate.getCandidateCode());
                    });
        } else if ("DN".equals(user.getRole().getRoleCode())) {
            // Employer - fetch company info
            companyRepository.findByUserUserId(user.getUserId())
                    .ifPresent(company -> {
                        responseBuilder.companyName(company.getCompanyName());
                        responseBuilder.companyCode(company.getCompanyCode());
                    });
        }

        return responseBuilder.build();
    }

    /**
     * Đổi mật khẩu cho user đang đăng nhập.
     * 
     * Quy trình thực hiện:
     * 1. Lấy User từ database theo username (authenticated user)
     * 2. Verify oldPassword khớp với BCrypt hash hiện tại
     * 3. Validate newPassword khác oldPassword
     * 4. Validate newPassword == confirmPassword
     * 5. Hash newPassword bằng BCrypt
     * 6. Update User.password và save
     * 
     * @param request ChangePasswordRequest
     * @param username Username từ JWT token
     * @throws ValidationException nếu old password sai hoặc validation fail
     * @throws ResourceNotFoundException nếu User không tồn tại
     */
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request, String username) {
        log.info("Changing password for user: {}", username);
        
        // Get user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ValidationException("Mật khẩu cũ không đúng");
        }
        
        // Validate new password != old password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ValidationException("Mật khẩu mới phải khác mật khẩu cũ");
        }
        
        // Validate new password == confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Xác nhận mật khẩu không khớp");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", username);
    }

    /**
     * Đăng xuất khỏi tất cả thiết bị (vô hiệu hóa tất cả JWT tokens cũ).
     * 
     * Quy trình thực hiện:
     * 1. Lấy User từ database theo username
     * 2. Set User.lastLogout = LocalDateTime.now()
     * 3. Save user
     * 4. Tất cả JWT tokens có issuedAt < lastLogout sẽ bị reject bởi JwtAuthenticationFilter
     * 
     * @param username Username từ JWT token
     * @throws ResourceNotFoundException nếu User không tồn tại
     */
    @Override
    @Transactional
    public void logoutAllSessions(String username) {
        log.info("Logout all sessions for user: {}", username);
        
        // Get user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        // Update lastLogout timestamp
        user.setLastLogout(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("All sessions logged out for user: {}", username);
    }
}
