package com.jobrec.recommendation.controller;

import com.jobrec.recommendation.dto.RecommendationResponse;
import com.jobrec.recommendation.dto.RecommendationPage;
import com.jobrec.recommendation.service.RecommendationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
@PreAuthorize("hasRole('JOB_SEEKER')")
public class RecommendationController {
    private final RecommendationService service; private final int defaultSize;
    public RecommendationController(RecommendationService service, @Value("${recommendation.page.default-size:20}") int defaultSize) { this.service=service; this.defaultSize=defaultSize; }
    @GetMapping public RecommendationPage list(Authentication auth, @RequestParam(defaultValue="0") int page, @RequestParam(required=false) Integer size, @RequestParam(required=false) Double minScore) { return service.recommend(userId(auth), page, size == null ? defaultSize : size, minScore); }
    @GetMapping("/jobs/{jobId}") public RecommendationResponse one(Authentication auth, @PathVariable UUID jobId) { return service.recommendOne(userId(auth), jobId); }
    private UUID userId(Authentication auth) { return UUID.fromString(auth.getName()); }
}
