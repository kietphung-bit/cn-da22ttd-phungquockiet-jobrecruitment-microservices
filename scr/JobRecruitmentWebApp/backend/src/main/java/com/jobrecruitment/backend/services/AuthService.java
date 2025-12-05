package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.dtos.request.CandidateRegisterRequest;
import com.jobrecruitment.backend.dtos.request.CompanyRegisterRequest;
import com.jobrecruitment.backend.dtos.request.LoginRequest;
import com.jobrecruitment.backend.dtos.response.AuthResponse;

public interface AuthService {
    
    /**
     * Register a new Company account (Role = DN)
     * Creates User AND Company records with synchronized UserCode = CompanyCode
     */
    AuthResponse registerCompany(CompanyRegisterRequest request);
    
    /**
     * Register a new Candidate account (Role = UV)
     * Creates User AND Candidate records with synchronized UserCode = CandidateCode
     */
    AuthResponse registerCandidate(CandidateRegisterRequest request);
    
    /**
     * Authenticate user and return JWT token
     */
    AuthResponse login(LoginRequest request);
}
