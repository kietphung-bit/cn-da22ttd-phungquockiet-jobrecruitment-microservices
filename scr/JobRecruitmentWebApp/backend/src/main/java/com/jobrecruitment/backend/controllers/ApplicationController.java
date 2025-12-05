package com.jobrecruitment.backend.controllers;

import com.jobrecruitment.backend.dtos.request.ApplicationRequest;
import com.jobrecruitment.backend.dtos.request.ApplicationStatusRequest;
import com.jobrecruitment.backend.dtos.response.ApiResponse;
import com.jobrecruitment.backend.dtos.response.ApplicationResponse;
import com.jobrecruitment.backend.enums.ApplicationStatus;
import com.jobrecruitment.backend.services.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Application Management", description = "Job application operations for Candidates and Employers")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @PreAuthorize("hasRole('UV')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Apply to Job",
            description = "Submit a job application (Candidate only). " +
                    "Validates: Job is ACTIVE, date within range (RBNT), CV is ACTIVE, not already applied. " +
                    "Generates unique ApplicationCode (DX + 8 digits). " +
                    "Status defaults to PENDING."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Application submitted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error (Job not active, date out of range, CV not active, already applied)",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only Candidates (UV) can apply",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job or CV not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyToJob(
            @Valid @RequestBody ApplicationRequest request,
            Authentication authentication) {
        ApplicationResponse response = applicationService.applyToJob(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ApplicationResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Application submitted successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{applicationId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get Application by ID",
            description = "Retrieve application details by ID (Authenticated users)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Application found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Application not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(@PathVariable Long applicationId) {
        ApplicationResponse response = applicationService.getApplicationById(applicationId);
        return ResponseEntity.ok(
                ApiResponse.<ApplicationResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Application retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('UV')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get My Applications",
            description = "Retrieve all applications submitted by the authenticated candidate (Candidate only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Applications retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only Candidates can access",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getMyApplications(Authentication authentication) {
        List<ApplicationResponse> response = applicationService.getMyApplications(authentication.getName());
        return ResponseEntity.ok(
                ApiResponse.<List<ApplicationResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Your applications retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('DN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get Applications by Job",
            description = "Retrieve all applications for a specific job (Employer only - own jobs)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Applications retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Can only view applications for own jobs",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getApplicationsByJob(
            @PathVariable Long jobId,
            @Parameter(description = "Filter by application status (PENDING, APPROVED, REJECTED)")
            @RequestParam(required = false) ApplicationStatus status,
            Authentication authentication) {
        
        List<ApplicationResponse> response;
        if (status != null) {
            response = applicationService.getApplicationsByJobAndStatus(jobId, status, authentication.getName());
        } else {
            response = applicationService.getApplicationsByJob(jobId, authentication.getName());
        }
        
        return ResponseEntity.ok(
                ApiResponse.<List<ApplicationResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Job applications retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{applicationId}/status")
    @PreAuthorize("hasRole('DN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update Application Status",
            description = "Change application status to APPROVED or REJECTED (Employer only - own jobs). " +
                    "Used by employers to manage candidate applications."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Application status updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Can only manage own job applications",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Application not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationStatusRequest request,
            Authentication authentication) {
        ApplicationResponse response = applicationService.updateApplicationStatus(
                applicationId, request, authentication.getName());
        return ResponseEntity.ok(
                ApiResponse.<ApplicationResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Application status updated successfully")
                        .data(response)
                        .build()
        );
    }
}
