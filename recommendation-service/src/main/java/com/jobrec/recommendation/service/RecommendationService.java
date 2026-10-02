package com.jobrec.recommendation.service;

import com.jobrec.recommendation.client.*;
import com.jobrec.recommendation.domain.*;
import com.jobrec.recommendation.dto.RecommendationResponse;
import com.jobrec.recommendation.dto.RecommendationPage;
import com.jobrec.recommendation.matching.MatchingEngine;
import com.jobrec.recommendation.matching.SkillGapAnalyzer;
import com.jobrec.recommendation.semantic.NoOpSemanticMatcher;
import com.jobrec.recommendation.semantic.SemanticMatcher;
import com.jobrec.recommendation.semantic.SemanticScoringConfig;
import com.jobrec.recommendation.semantic.SemanticScoreBlender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RecommendationService {
    private final ProfileClient profileClient; private final JobClient jobClient;
    private final MatchingEngine matchingEngine = new MatchingEngine(); private final SkillGapAnalyzer skillGapAnalyzer = new SkillGapAnalyzer();
    private final SemanticMatcher semanticMatcher;
    private final SemanticScoringConfig semanticScoringConfig;
    private final SemanticScoreBlender semanticScoreBlender;
    private final int maxSize;
    public RecommendationService(ProfileClient profileClient, JobClient jobClient, @Value("${recommendation.page.max-size:100}") int maxSize) {
        this(profileClient, jobClient, new NoOpSemanticMatcher(), new SemanticScoringConfig(0.20), maxSize);
    }
    @Autowired
    public RecommendationService(ProfileClient profileClient, JobClient jobClient, SemanticMatcher semanticMatcher,
                                 SemanticScoringConfig semanticScoringConfig,
                                 @Value("${recommendation.page.max-size:100}") int maxSize) { this.profileClient=profileClient; this.jobClient=jobClient; this.semanticMatcher=semanticMatcher; this.semanticScoringConfig=semanticScoringConfig; this.semanticScoreBlender=new SemanticScoreBlender(semanticScoringConfig); this.maxSize=maxSize; }

    public RecommendationPage recommend(UUID candidateId, int page, int size, Double minScore) {
        validatePage(page, size); validateMinScore(minScore);
        CandidateProfile profile = profileClient.getProfile(candidateId);
        List<JobData> openJobs = jobClient.getOpenJobs();
        Map<UUID, Double> semanticScores = semanticScores(profile, openJobs);
        List<RecommendationResponse> all = openJobs.stream().map(job -> calculate(profile, job, semanticScores)).filter(r -> minScore == null || r.matchPercent() >= minScore)
                .sorted(Comparator.comparingDouble(RecommendationResponse::matchPercent).reversed().thenComparing(r -> r.jobId().toString())).toList();
        int from = Math.min(page * size, all.size()); int to = Math.min(from + size, all.size());
        int totalPages = all.isEmpty() ? 0 : (int) Math.ceil(all.size() / (double) size);
        return new RecommendationPage(all.subList(from, to), page, size, all.size(), totalPages, page == 0, page >= totalPages - 1);
    }
    public RecommendationResponse recommendOne(UUID candidateId, UUID jobId) {
        CandidateProfile profile = profileClient.getProfile(candidateId); JobData job = jobClient.getJob(jobId);
        return calculate(profile, job, semanticScores(profile, List.of(job)));
    }
    private RecommendationResponse calculate(CandidateProfile profile, JobData job, Map<UUID, Double> semanticScores) {
        MatchResult result = matchingEngine.match(profile, job.requirements()); SkillGap gap = skillGapAnalyzer.analyze(result, job.requirements());
        SemanticScoreBlender.BlendedScore blended = semanticScoreBlender.blend(result, semanticScores.get(job.id()));
        return new RecommendationResponse(job.id(), job.title(), job.company(), job.location(), blended.score(), result.matchedSkills(), result.missingSkills(), gap,
                blended.semanticScore(), blended.semanticUsed());
    }
    private Map<UUID, Double> semanticScores(CandidateProfile profile, List<JobData> jobs) {
        try { Map<UUID, Double> scores = semanticMatcher.match(profile, jobs); return scores == null ? Map.of() : scores; }
        catch (RuntimeException ignored) { return Map.of(); }
    }
    private void validatePage(int page, int size) { if (page < 0 || size < 1 || size > maxSize) throw new IllegalArgumentException("Invalid pagination parameters"); }
    private void validateMinScore(Double score) { if (score != null && (score < 0 || score > 100)) throw new IllegalArgumentException("minScore must be between 0 and 100"); }
}
