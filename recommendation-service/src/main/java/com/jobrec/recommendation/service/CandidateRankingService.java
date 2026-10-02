package com.jobrec.recommendation.service;

import com.jobrec.recommendation.client.*;
import com.jobrec.recommendation.domain.CandidateProfile;
import com.jobrec.recommendation.domain.MatchResult;
import com.jobrec.recommendation.dto.CandidateRankingPage;
import com.jobrec.recommendation.dto.CandidateRankingResponse;
import com.jobrec.recommendation.dto.SkippedCandidate;
import com.jobrec.recommendation.exception.RecruiterOwnershipException;
import com.jobrec.recommendation.matching.MatchingEngine;
import com.jobrec.recommendation.semantic.SemanticMatcher;
import com.jobrec.recommendation.semantic.SemanticScoreBlender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CandidateRankingService {
    private final JobClient jobClient;
    private final ApplicationClient applicationClient;
    private final ProfileClient profileClient;
    private final SemanticMatcher semanticMatcher;
    private final MatchingEngine matchingEngine = new MatchingEngine();
    private final SemanticScoreBlender semanticScoreBlender;
    private final int maxSize;

    public CandidateRankingService(JobClient jobClient, ApplicationClient applicationClient, ProfileClient profileClient,
                                   SemanticMatcher semanticMatcher, com.jobrec.recommendation.semantic.SemanticScoringConfig scoringConfig,
                                   @Value("${recommendation.page.max-size:100}") int maxSize) {
        this.jobClient = jobClient; this.applicationClient = applicationClient; this.profileClient = profileClient;
        this.semanticMatcher = semanticMatcher; this.semanticScoreBlender = new SemanticScoreBlender(scoringConfig); this.maxSize = maxSize;
    }

    public CandidateRankingPage rank(UUID recruiterId, UUID jobId, int page, int size) {
        validatePage(page, size);
        JobData job = jobClient.getJob(jobId);
        if (job.recruiterId() == null || !recruiterId.equals(job.recruiterId())) {
            throw new RecruiterOwnershipException("You can only rank candidates for your own jobs");
        }
        List<ApplicationData> applications = applicationClient.getApplications(jobId);
        if (applications.isEmpty()) return page(List.of(), List.of(), page, size);

        Map<UUID, ApplicationData> byCandidate = applications.stream().filter(application -> application.candidateId() != null)
                .collect(Collectors.toMap(ApplicationData::candidateId, application -> application, (first, ignored) -> first, LinkedHashMap::new));
        Map<UUID, CandidateProfile> profiles = profileClient.getProfiles(byCandidate.keySet());
        List<SkippedCandidate> skipped = byCandidate.keySet().stream().filter(candidateId -> !profiles.containsKey(candidateId))
                .map(candidateId -> new SkippedCandidate(candidateId, "Candidate profile unavailable")).toList();
        Map<UUID, Double> semanticScores = semanticScores(job, profiles);

        List<CandidateRankingResponse> ranked = profiles.entrySet().stream().map(entry -> {
            UUID candidateId = entry.getKey(); CandidateProfile profile = entry.getValue();
            MatchResult weighted = matchingEngine.match(profile, job.requirements());
            SemanticScoreBlender.BlendedScore blended = semanticScoreBlender.blend(weighted, semanticScores.get(candidateId));
            ApplicationData application = byCandidate.get(candidateId);
            return new CandidateRankingResponse(candidateId, blended.score(), blended.semanticScore(), blended.semanticUsed(),
                    weighted.matchedSkills(), weighted.missingSkills(), application.status());
        }).sorted(Comparator.comparingDouble(CandidateRankingResponse::matchPercent).reversed()
                .thenComparing(response -> response.candidateId().toString())).toList();
        return page(ranked, skipped, page, size);
    }

    private Map<UUID, Double> semanticScores(JobData job, Map<UUID, CandidateProfile> profiles) {
        if (profiles.isEmpty()) return Map.of();
        try { Map<UUID, Double> result = semanticMatcher.matchCandidates(job, profiles); return result == null ? Map.of() : result; }
        catch (RuntimeException ignored) { return Map.of(); }
    }

    private CandidateRankingPage page(List<CandidateRankingResponse> ranked, List<SkippedCandidate> skipped, int page, int size) {
        int from = Math.min(page * size, ranked.size()); int to = Math.min(from + size, ranked.size());
        int totalPages = ranked.isEmpty() ? 0 : (int) Math.ceil(ranked.size() / (double) size);
        return new CandidateRankingPage(ranked.subList(from, to), page, size, ranked.size(), totalPages,
                page == 0, ranked.isEmpty() || page >= totalPages - 1, skipped);
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > maxSize) throw new IllegalArgumentException("Invalid pagination parameters");
    }
}
