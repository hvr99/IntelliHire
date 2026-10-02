package com.jobrec.job.controller;

import com.jobrec.job.dto.CreateJobRequest;
import com.jobrec.job.dto.JobResponse;
import com.jobrec.job.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @PostMapping
    public ResponseEntity<JobResponse> createJob(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody CreateJobRequest request) {
        UUID recruiterId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(recruiterId, request));
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @PutMapping("/{jobId}")
    public ResponseEntity<JobResponse> updateJob(@AuthenticationPrincipal Jwt jwt,
                                                  @PathVariable UUID jobId,
                                                  @Valid @RequestBody CreateJobRequest request) {
        UUID recruiterId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(jobService.updateJob(recruiterId, jobId, request));
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @PatchMapping("/{jobId}/close")
    public ResponseEntity<Void> closeJob(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID jobId) {
        UUID recruiterId = UUID.fromString(jwt.getSubject());
        jobService.closeJob(recruiterId, jobId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/mine")
    public ResponseEntity<Page<JobResponse>> getMyJobs(@AuthenticationPrincipal Jwt jwt,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        UUID recruiterId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(jobService.getRecruiterJobs(recruiterId, PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String workMode,
            @RequestParam(required = false) BigDecimal salaryMin,
            @RequestParam(required = false) String skill,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.searchJobs(keyword, location, workMode, salaryMin, skill,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable UUID jobId) {
        return ResponseEntity.ok(jobService.getJobById(jobId));
    }
}
