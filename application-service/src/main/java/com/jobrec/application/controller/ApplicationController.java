package com.jobrec.application.controller;

import com.jobrec.application.domain.ApplicationStatus;
import com.jobrec.application.dto.*;
import com.jobrec.application.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final ApplicationService service;
    public ApplicationController(ApplicationService service) { this.service = service; }
    @PostMapping @org.springframework.security.access.prepost.PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ApplicationResponse> apply(@Valid @RequestBody ApplyRequest request, Authentication auth) { return ResponseEntity.status(201).body(service.apply(userId(auth), request.jobId())); }
    @GetMapping("/me") @org.springframework.security.access.prepost.PreAuthorize("hasRole('JOB_SEEKER')")
    public List<ApplicationResponse> mine(Authentication auth) { return service.listMine(userId(auth)); }
    @PostMapping("/{applicationId}/withdraw") @org.springframework.security.access.prepost.PreAuthorize("hasRole('JOB_SEEKER')")
    public ApplicationResponse withdraw(@PathVariable UUID applicationId, Authentication auth) { return service.withdraw(userId(auth), applicationId); }
    @GetMapping("/job/{jobId}") @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECRUITER')")
    public List<ApplicationResponse> forJob(@PathVariable UUID jobId, Authentication auth) { return service.listForJob(userId(auth), jobId); }
    @PatchMapping("/{applicationId}/status") @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECRUITER')")
    public ApplicationResponse changeStatus(@PathVariable UUID applicationId, @Valid @RequestBody ChangeStatusRequest request, Authentication auth) { return service.changeStatus(userId(auth), applicationId, request.status()); }
    private UUID userId(Authentication auth) { return UUID.fromString(auth.getName()); }
}
