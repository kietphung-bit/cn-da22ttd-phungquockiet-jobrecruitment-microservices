package com.jobrecruitment.backend.controllers;

import com.jobrecruitment.backend.dtos.request.JobRequest;
import com.jobrecruitment.backend.dtos.response.ApiResponse;
import com.jobrecruitment.backend.dtos.response.JobResponse;
import com.jobrecruitment.backend.enums.JobStatus;
import com.jobrecruitment.backend.services.JobService;
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
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Management", description = "Job posting CRUD operations for Employers")
public class JobController {

    private final JobService jobService;

    @PostMapping
    @PreAuthorize("hasRole('DN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create Job Posting",
            description = "Create a new job posting (Employer only). " +
                    "Validates StartDate <= EndDate (RBNT). " +
                    "Generates unique JobCode (VL + 8 digits). " +
                    "Status: WAIT if startDate > today, otherwise PENDING."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Job created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error (StartDate > EndDate, invalid data)",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only Employers (DN) can create jobs",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Company profile or Job category not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @Valid @RequestBody JobRequest request,
            Authentication authentication) {
        JobResponse response = jobService.createJob(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<JobResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Job created successfully")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{jobId}")
    @PreAuthorize("hasRole('DN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update Job Posting",
            description = "Update an existing job posting (Employer only - own jobs). " +
                    "Validates ownership and StartDate <= EndDate."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Can only modify own jobs",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable Long jobId,
            @Valid @RequestBody JobRequest request,
            Authentication authentication) {
        JobResponse response = jobService.updateJob(jobId, request, authentication.getName());
        return ResponseEntity.ok(
                ApiResponse.<JobResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Job updated successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{jobId}")
    @Operation(
            summary = "Get Job by ID",
            description = "Retrieve job details by ID (Public access)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable Long jobId) {
        JobResponse response = jobService.getJobById(jobId);
        return ResponseEntity.ok(
                ApiResponse.<JobResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Job retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @Operation(
            summary = "Get All Jobs",
            description = "Retrieve all jobs with optional status filter (Public access)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Jobs retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<List<JobResponse>>> getAllJobs(
            @Parameter(description = "Filter by job status (PENDING, WAIT, ACTIVE, CLOSED, HIDDEN)")
            @RequestParam(required = false) JobStatus status) {
        List<JobResponse> response = jobService.getAllJobs(status);
        return ResponseEntity.ok(
                ApiResponse.<List<JobResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Jobs retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/my-jobs")
    @PreAuthorize("hasRole('DN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get My Jobs",
            description = "Retrieve all jobs posted by the authenticated employer (Employer only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Jobs retrieved successfully",
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
                    description = "Forbidden - Only Employers can access",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<List<JobResponse>>> getMyJobs(Authentication authentication) {
        List<JobResponse> response = jobService.getMyJobs(authentication.getName());
        return ResponseEntity.ok(
                ApiResponse.<List<JobResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Your jobs retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/company/{companyId}")
    @Operation(
            summary = "Get Jobs by Company",
            description = "Retrieve all jobs posted by a specific company (Public access)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Jobs retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Company not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<List<JobResponse>>> getJobsByCompany(@PathVariable Long companyId) {
        List<JobResponse> response = jobService.getJobsByCompany(companyId);
        return ResponseEntity.ok(
                ApiResponse.<List<JobResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Company jobs retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{jobId}/status")
    @PreAuthorize("hasRole('DN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update Job Status",
            description = "Change job status (Employer only - own jobs). " +
                    "Used to hide/unhide jobs or change status."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job status updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Can only modify own jobs",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<JobResponse>> updateJobStatus(
            @PathVariable Long jobId,
            @Parameter(description = "New job status") @RequestParam JobStatus status,
            Authentication authentication) {
        JobResponse response = jobService.updateJobStatus(jobId, status, authentication.getName());
        return ResponseEntity.ok(
                ApiResponse.<JobResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Job status updated successfully")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('DN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete Job",
            description = "Soft delete job by changing status to HIDDEN (Employer only - own jobs)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Can only delete own jobs",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @PathVariable Long jobId,
            Authentication authentication) {
        jobService.deleteJob(jobId, authentication.getName());
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Job deleted successfully")
                        .data(null)
                        .build()
        );
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search Jobs",
            description = "Search jobs by keyword in title or location (Public access)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<List<JobResponse>>> searchJobs(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        List<JobResponse> response = jobService.searchJobs(keyword);
        return ResponseEntity.ok(
                ApiResponse.<List<JobResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Search completed successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/filter/salary")
    @Operation(
            summary = "Filter Jobs by Salary",
            description = "Filter jobs by salary range (Public access)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Filter completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid salary range",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<List<JobResponse>>> filterBySalary(
            @Parameter(description = "Minimum salary") @RequestParam Double minSalary,
            @Parameter(description = "Maximum salary") @RequestParam Double maxSalary) {
        List<JobResponse> response = jobService.filterBySalary(minSalary, maxSalary);
        return ResponseEntity.ok(
                ApiResponse.<List<JobResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Filter completed successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/category/{jcid}")
    @Operation(
            summary = "Get Jobs by Category",
            description = "Retrieve all jobs in a specific category (Public access)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Jobs retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<List<JobResponse>>> getJobsByCategory(@PathVariable Integer jcid) {
        List<JobResponse> response = jobService.getJobsByCategory(jcid);
        return ResponseEntity.ok(
                ApiResponse.<List<JobResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Category jobs retrieved successfully")
                        .data(response)
                        .build()
        );
    }
}
