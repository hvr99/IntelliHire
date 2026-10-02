package com.jobrec.recommendation.service;

import com.jobrec.recommendation.client.*;
import com.jobrec.recommendation.domain.*;
import com.jobrec.recommendation.dto.RecommendationPage;
import com.jobrec.recommendation.dto.RecommendationResponse;
import com.jobrec.recommendation.semantic.SemanticMatcher;
import com.jobrec.recommendation.semantic.SemanticScoringConfig;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RecommendationServiceTest {
    private final StubProfileClient profiles = new StubProfileClient();
    private final StubJobClient jobs = new StubJobClient();
    private RecommendationService service;
    private final UUID candidateId = UUID.randomUUID();

    @BeforeEach void setUp() { service = new RecommendationService(profiles, jobs, 100); }

    @Test void stubProfileAndJobsProduceRecommendations() {
        profiles.setProfile(profile("java")); jobs.addJob(job("Java", Set.of("Java"))).addJob(job("SQL", Set.of("SQL")));
        RecommendationPage page = service.recommend(candidateId, 0, 20, null);
        assertEquals(2, page.totalElements()); assertEquals("Java", page.content().get(0).title()); assertEquals(100, page.content().get(0).matchPercent());
    }
    @Test void emptyProfileStillReturnsRecommendations() {
        jobs.addJob(job("Java", Set.of("Java"))); RecommendationPage page = service.recommend(candidateId, 0, 20, null);
        assertEquals(1, page.totalElements()); assertEquals(0, page.content().get(0).matchPercent()); assertEquals(List.of("java"), page.content().get(0).missingSkills());
    }
    @Test void paginationReturnsRequestedSlice() {
        for (int i=0; i<5; i++) jobs.addJob(job("Job" + i, Set.of("skill" + i)));
        RecommendationPage page = service.recommend(candidateId, 1, 2, null);
        assertEquals(2, page.content().size()); assertEquals(5, page.totalElements()); assertEquals(3, page.totalPages()); assertEquals(1, page.number());
    }
    @Test void minScoreFiltersResults() {
        profiles.setProfile(profile("java")); jobs.addJob(job("Match", Set.of("java"))).addJob(job("Miss", Set.of("sql")));
        RecommendationPage page = service.recommend(candidateId, 0, 20, 50.0);
        assertEquals(1, page.totalElements()); assertEquals("Match", page.content().get(0).title());
    }
    @Test void downstreamFailureIsPropagatedForControllerToMap() {
        jobs.failWith(new DownstreamServiceException("down", null));
        assertThrows(DownstreamServiceException.class, () -> service.recommend(candidateId, 0, 20, null));
    }
    @Test void semanticScoreIsBlendedWhenAvailable() {
        JobData job = job("Java", Set.of("Java")); jobs.addJob(job);
        SemanticMatcher matcher = (candidate, jobs) -> Map.of(job.id(), 0.5);
        service = new RecommendationService(profiles, jobs, matcher, new SemanticScoringConfig(0.20), 100);
        RecommendationResponse result = service.recommend(candidateId, 0, 20, null).content().get(0);
        assertEquals(10.0, result.matchPercent());
        assertEquals(0.5, result.semanticScore());
        assertTrue(result.semanticUsed());
    }
    @Test void unavailableSemanticScoreUsesWeightedScoreExactly() {
        JobData job = job("Java", Set.of("Java")); jobs.addJob(job);
        SemanticMatcher matcher = (candidate, jobs) -> { throw new IllegalStateException("ML unavailable"); };
        service = new RecommendationService(profiles, jobs, matcher, new SemanticScoringConfig(0.20), 100);
        RecommendationResponse result = service.recommend(candidateId, 0, 20, null).content().get(0);
        assertEquals(0.0, result.matchPercent()); assertNull(result.semanticScore()); assertFalse(result.semanticUsed());
    }
    @Test void semanticMatcherReceivesResumeAndConfirmedCertificateSkillsInOneBatch() {
        profiles.setProfile(profile("python", "aws"));
        JobData first = job("Python", Set.of("python")); JobData second = job("AWS", Set.of("aws")); jobs.addJob(first).addJob(second);
        List<String> captured = new ArrayList<>();
        SemanticMatcher matcher = (candidate, suppliedJobs) -> { captured.addAll(candidate.getSkills()); assertEquals(2, suppliedJobs.size()); return Map.of(); };
        service = new RecommendationService(profiles, jobs, matcher, new SemanticScoringConfig(0.20), 100);
        service.recommend(candidateId, 0, 20, null);
        assertEquals(Set.of("python", "aws"), new HashSet<>(captured));
    }
    @Test void invalidBlendFactorIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SemanticScoringConfig(1.01));
        assertThrows(IllegalArgumentException.class, () -> new SemanticScoringConfig(-0.01));
    }
    private static CandidateProfile profile(String... skills) { CandidateProfile p=new CandidateProfile(); p.setSkills(Set.of(skills)); return p; }
    private static JobData job(String title, Set<String> requiredSkills) { JobRequirements r=new JobRequirements(); r.setRequiredSkills(requiredSkills); return new JobData(UUID.randomUUID(), title, "Acme", "Remote", r); }
}
