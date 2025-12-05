package com.jobrecruitment.backend.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobrecruitment.backend.dtos.response.ApiResponse;
import com.jobrecruitment.backend.utils.CodeGenerator;

/**
 * Health Check Controller
 * For testing infrastructure setup
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    @Autowired
    private CodeGenerator codeGenerator;
    
    /**
     * Health check endpoint
     * 
     * @return API response with system status
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "Job Recruitment Platform");
        data.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(ApiResponse.success("System is running", data));
    }
    
    /**
     * Test code generation
     * 
     * @return API response with sample generated codes
     */
    @GetMapping("/test-code-generator")
    public ResponseEntity<ApiResponse<Map<String, String>>> testCodeGenerator() {
        Map<String, String> codes = new HashMap<>();
        
        // Generate sample codes (using lambda that always returns false for uniqueness)
        codes.put("companyCode", codeGenerator.generateCompanyCode(code -> false));
        codes.put("candidateCode", codeGenerator.generateCandidateCode(code -> false));
        codes.put("jobCode", codeGenerator.generateJobCode(code -> false));
        codes.put("cvCode", codeGenerator.generateCVCode(code -> false));
        codes.put("applicationCode", codeGenerator.generateApplicationCode(code -> false));
        codes.put("adminCode", codeGenerator.generateAdminCode());
        
        return ResponseEntity.ok(ApiResponse.success("Code generation test successful", codes));
    }
}
