package com.jobrecruitment.backend.controllers;

import com.jobrecruitment.backend.dtos.request.CandidateRegisterRequest;
import com.jobrecruitment.backend.dtos.request.CompanyRegisterRequest;
import com.jobrecruitment.backend.dtos.request.LoginRequest;
import com.jobrecruitment.backend.dtos.response.AuthResponse;
import com.jobrecruitment.backend.services.AuthService;
import com.jobrecruitment.backend.dtos.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and Registration APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "User Login",
            description = "Authenticate user with email and password. Returns JWT token upon successful authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Login successful")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/register-company")
    @Operation(
            summary = "Company Registration",
            description = "Register a new company account (Role: DN - Doanh nghiệp). " +
                    "Creates User and Company records with synchronized UserCode = CompanyCode. " +
                    "Initial status: PENDING (Chờ xét duyệt)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Company registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error or email already exists",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Role DN not found in database",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> registerCompany(
            @Valid @RequestBody CompanyRegisterRequest request) {
        AuthResponse response = authService.registerCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<AuthResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Company registration successful")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/register-candidate")
    @Operation(
            summary = "Candidate Registration",
            description = "Register a new candidate account (Role: UV - Ứng viên). " +
                    "Creates User and Candidate records with synchronized UserCode = CandidateCode. " +
                    "Age validation: Must be 18 years or older (RBNS rule)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Candidate registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error or email already exists",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Role UV not found in database",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> registerCandidate(
            @Valid @RequestBody CandidateRegisterRequest request) {
        AuthResponse response = authService.registerCandidate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<AuthResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Candidate registration successful")
                        .data(response)
                        .build()
        );
    }
}
