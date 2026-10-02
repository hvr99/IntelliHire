package com.jobrec.application.controller;
import com.jobrec.application.dto.ApplicationResponse;
import com.jobrec.application.repository.ApplicationRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/internal/jobs")
public class InternalApplicationController {
    private final ApplicationRepository repository;
    public InternalApplicationController(ApplicationRepository repository) { this.repository = repository; }
    @GetMapping("/{jobId}/applications")
    public List<ApplicationResponse> list(@PathVariable UUID jobId) { return repository.findByJobIdOrderByCreatedAtDesc(jobId).stream().map(ApplicationResponse::from).toList(); }
}
