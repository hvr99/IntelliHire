package com.jobrec.recommendation.semantic;

import com.jobrec.recommendation.client.JobData;
import com.jobrec.recommendation.domain.CandidateProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "recommendation.semantic.mode", havingValue = "rest")
public class MlSemanticMatcher implements SemanticMatcher {
    private final RestClient client;

    public MlSemanticMatcher(@Value("${ml-service.base-url:http://localhost:8000}") String baseUrl,
                             @Value("${recommendation.semantic.timeout-ms:750}") int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public MlSemanticMatcher(RestClient client) {
        this.client = client;
    }

    @Override
    public Map<UUID, Double> match(CandidateProfile candidate, List<JobData> jobs) {
        if (jobs == null || jobs.isEmpty()) return Map.of();
        try {
            Map<String, List<Map<String, Object>>> response = client.post().uri("/similarity")
                    .body(new SimilarityRequest(RecommendationTextBuilder.candidateText(candidate), jobs.stream()
                            .map(job -> new SimilarityJob(job.id(), RecommendationTextBuilder.jobText(job))).toList()))
                    .retrieve().body(new ParameterizedTypeReference<>() {});
            if (response == null || response.get("matches") == null) return Map.of();
            return response.get("matches").stream().map(this::toMatch).filter(Objects::nonNull)
                    .collect(Collectors.toMap(SimilarityMatch::jobId, item -> item.similarity, (left, right) -> right));
        } catch (RuntimeException ignored) {
            return Map.of();
        }
    }

    private SimilarityMatch toMatch(Map<String, Object> item) {
        try {
            UUID jobId = UUID.fromString(String.valueOf(item.get("jobId")));
            double similarity = ((Number) item.get("similarity")).doubleValue();
            return new SimilarityMatch(jobId, Math.max(0.0, Math.min(1.0, similarity)));
        } catch (RuntimeException ignored) { return null; }
    }

    private record SimilarityRequest(String candidateText, List<SimilarityJob> jobs) {}
    private record SimilarityJob(UUID id, String text) {}
    private record SimilarityMatch(UUID jobId, double similarity) {}
}
