package com.jobrec.recommendation.controller;

import com.jobrec.recommendation.dto.CandidateRankingPage;
import com.jobrec.recommendation.service.CandidateRankingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/recruiter/jobs")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterRankingController {
    private final CandidateRankingService service;
    private final int defaultSize;

    public RecruiterRankingController(CandidateRankingService service,
                                      @Value("${recommendation.page.default-size:20}") int defaultSize) {
        this.service = service; this.defaultSize = defaultSize;
    }

    @GetMapping("/{jobId}/candidates")
    public CandidateRankingPage candidates(Authentication auth, @PathVariable UUID jobId,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(required = false) Integer size) {
        return service.rank(UUID.fromString(auth.getName()), jobId, page, size == null ? defaultSize : size);
    }
}
