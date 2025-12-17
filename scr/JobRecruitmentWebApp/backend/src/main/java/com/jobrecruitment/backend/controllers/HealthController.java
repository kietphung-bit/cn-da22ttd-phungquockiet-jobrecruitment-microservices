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
 * HealthController - Controller kiểm tra sức khỏe hệ thống
 * 
 * Chức năng:
 * - Health check endpoint để kiểm tra hệ thống có đang chạy không
 * - Test endpoint để kiểm tra CodeGenerator có hoạt động đúng không
 * 
 * Sử dụng:
 * - Monitoring tools (Prometheus, Nagios) gọi /api/health để kiểm tra uptime
 * - Developers test infrastructure setup sau khi deploy
 * - Load balancer health check trước khi routing traffic
 * 
 * Endpoints:
 * - GET /api/health: Health check cơ bản (status UP/DOWN)
 * - GET /api/health/test-code-generator: Test generate sample codes
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    /**
     * CodeGenerator: Để test code generation (DN/UV/JOB/CV/APP codes)
     */
    @Autowired
    private CodeGenerator codeGenerator;
    
    /**
     * Health check endpoint
     * 
     * Mục đích:
     * - Kiểm tra hệ thống có đang chạy không (UP/DOWN)
     * - Trả về timestamp để verify response mới nhất
     * - Monitoring tools dùng để alert nếu hệ thống DOWN
     * 
     * @return API response với system status (UP), service name, timestamp
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
     * Mục đích:
     * - Kiểm tra CodeGenerator có hoạt động đúng không
     * - Verify format codes: DN + 8 số, UV + 8 số, JOB + 8 số, CV + 8 số, APP + 8 số
     * - Test infrastructure setup sau khi deploy
     * 
     * Lưu ý:
     * - Lambda (code -> false) để skip uniqueness check (test only)
     * - Production code phải check exists trong database
     * 
     * @return API response với sample generated codes (companyCode, candidateCode, jobCode, cvCode, applicationCode, adminCode)
     */
    @GetMapping("/test-code-generator")
    public ResponseEntity<ApiResponse<Map<String, String>>> testCodeGenerator() {
        Map<String, String> codes = new HashMap<>();
        
        // Generate sample codes (lambda trả về false = không cần check uniqueness, chỉ dùng cho testing)
        codes.put("companyCode", codeGenerator.generateCompanyCode(code -> false));
        codes.put("candidateCode", codeGenerator.generateCandidateCode(code -> false));
        codes.put("jobCode", codeGenerator.generateJobCode(code -> false));
        codes.put("cvCode", codeGenerator.generateCVCode(code -> false));
        codes.put("applicationCode", codeGenerator.generateApplicationCode(code -> false));
        codes.put("adminCode", codeGenerator.generateAdminCode());
        
        return ResponseEntity.ok(ApiResponse.success("Code generation test successful", codes));
    }
}
