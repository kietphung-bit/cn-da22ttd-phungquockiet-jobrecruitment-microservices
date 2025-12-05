package com.jobrecruitment.backend.services.impl;

import com.jobrecruitment.backend.dtos.request.CandidateRegisterRequest;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final CodeGenerator codeGenerator;

    @Override
    @Transactional
    public AuthResponse registerCompany(CompanyRegisterRequest request) {
        // Validate email uniqueness
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

    @Override
    @Transactional
    public AuthResponse registerCandidate(CandidateRegisterRequest request) {
        // Validate email uniqueness
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

    @Override
    public AuthResponse login(LoginRequest request) {
        // Authenticate user
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

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .userCode(user.getUserCode())
                .roleCode(user.getRole().getRoleCode())
                .roleName(user.getRole().getRoleName())
                .message("Login successful")
                .build();
    }
}
