package com.jobrec.job.controller;

import com.jobrec.job.dto.JobResponse;
import com.jobrec.job.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/jobs")
public class InternalJobController {

    private final JobService jobService;

    public InternalJobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable UUID jobId) {
        return ResponseEntity.ok(jobService.getJobById(jobId));
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getJobs(@RequestParam List<UUID> ids) {
        return ResponseEntity.ok(jobService.getJobsByIds(ids));
    }

    @GetMapping("/open")
    public ResponseEntity<List<JobResponse>> getOpenJobs() {
        return ResponseEntity.ok(jobService.getOpenJobs());
    }
}
