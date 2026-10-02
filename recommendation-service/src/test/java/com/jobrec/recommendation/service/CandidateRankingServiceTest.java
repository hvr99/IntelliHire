package com.jobrec.recommendation.service;

import com.jobrec.recommendation.client.*;
import com.jobrec.recommendation.domain.CandidateProfile;
import com.jobrec.recommendation.domain.JobRequirements;
import com.jobrec.recommendation.dto.CandidateRankingPage;
import com.jobrec.recommendation.semantic.SemanticMatcher;
import com.jobrec.recommendation.semantic.SemanticScoringConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CandidateRankingServiceTest {
    private final UUID recruiterId = UUID.randomUUID();
    private final UUID jobId = UUID.randomUUID();
    private final StubJobClient jobs = new StubJobClient();
    private final StubApplicationClient applications = new StubApplicationClient();
    private final Map<UUID, CandidateProfile> profileData = new LinkedHashMap<>();
    private CandidateRankingService service;

    @BeforeEach void setUp() {
        JobRequirements requirements = new JobRequirements(); requirements.setRequiredSkills(Set.of("java")); requirements.setRecruiterId(recruiterId);
        jobs.addJob(new JobData(jobId, "Backend", "Acme", "Remote", requirements));
        ProfileClient profiles = new ProfileClient() {
            @Override public CandidateProfile getProfile(UUID candidateId) { return profileData.get(candidateId); }
            @Override public Map<UUID, CandidateProfile> getProfiles(Collection<UUID> ids) {
                Map<UUID, CandidateProfile> result = new LinkedHashMap<>(); ids.forEach(id -> { if (profileData.containsKey(id)) result.put(id, profileData.get(id)); }); return result;
            }
        };
        SemanticMatcher noMl = (candidate, suppliedJobs) -> Map.of();
        service = new CandidateRankingService(jobs, applications, profiles, noMl, new SemanticScoringConfig(0.20), 100);
    }

    @Test void recruiterOwnershipIsRequired() {
        assertThrows(com.jobrec.recommendation.exception.RecruiterOwnershipException.class,
                () -> service.rank(UUID.randomUUID(), jobId, 0, 20));
    }

    @Test void candidatesAreRankedByMatchingEngineScore() {
        UUID strong = candidate("java"); UUID weak = candidate("python");
        applications.addApplication(new ApplicationData(UUID.randomUUID(), jobId, weak, "APPLIED"))
                .addApplication(new ApplicationData(UUID.randomUUID(), jobId, strong, "SHORTLISTED"));
        CandidateRankingPage page = service.rank(recruiterId, jobId, 0, 20);
        assertEquals(List.of(strong, weak), page.content().stream().map(item -> item.candidateId()).toList());
        assertEquals("SHORTLISTED", page.content().get(0).applicationStatus());
    }

    @Test void noApplicantsReturnsEmptyPage() {
        CandidateRankingPage page = service.rank(recruiterId, jobId, 0, 20);
        assertTrue(page.content().isEmpty()); assertTrue(page.skippedCandidates().isEmpty());
    }

    @Test void unavailableProfileIsSkipped() {
        UUID available = candidate("java"); UUID unavailable = UUID.randomUUID();
        applications.addApplication(new ApplicationData(UUID.randomUUID(), jobId, available, "APPLIED"))
                .addApplication(new ApplicationData(UUID.randomUUID(), jobId, unavailable, "APPLIED"));
        CandidateRankingPage page = service.rank(recruiterId, jobId, 0, 20);
        assertEquals(List.of(available), page.content().stream().map(item -> item.candidateId()).toList());
        assertEquals(List.of(unavailable), page.skippedCandidates().stream().map(item -> item.candidateId()).toList());
    }

    @Test void mlUnavailableFallsBackToWeightedScore() {
        UUID candidate = candidate("java"); applications.addApplication(new ApplicationData(UUID.randomUUID(), jobId, candidate, "APPLIED"));
        CandidateRankingPage page = service.rank(recruiterId, jobId, 0, 20);
        assertEquals(100.0, page.content().get(0).matchPercent());
        assertNull(page.content().get(0).semanticScore()); assertFalse(page.content().get(0).semanticUsed());
    }

    @Test void availableSemanticScoreUsesSharedBlend() {
        UUID candidate = candidate("java"); applications.addApplication(new ApplicationData(UUID.randomUUID(), jobId, candidate, "APPLIED"));
        SemanticMatcher semantic = new SemanticMatcher() {
            @Override public Map<UUID, Double> match(CandidateProfile profile, List<JobData> suppliedJobs) { return Map.of(); }
            @Override public Map<UUID, Double> matchCandidates(JobData job, Map<UUID, CandidateProfile> candidates) { return Map.of(candidate, 0.5); }
        };
        service = new CandidateRankingService(jobs, applications, new ProfileClient() {
            @Override public CandidateProfile getProfile(UUID id) { return profileData.get(id); }
            @Override public Map<UUID, CandidateProfile> getProfiles(Collection<UUID> ids) { return Map.of(candidate, profileData.get(candidate)); }
        }, semantic, new SemanticScoringConfig(0.20), 100);
        var result = service.rank(recruiterId, jobId, 0, 20).content().get(0);
        assertEquals(90.0, result.matchPercent()); assertEquals(0.5, result.semanticScore()); assertTrue(result.semanticUsed());
    }

    @Test void paginationIsAppliedAfterRanking() {
        for (int i = 0; i < 3; i++) applications.addApplication(new ApplicationData(UUID.randomUUID(), jobId, candidate("java"), "APPLIED"));
        CandidateRankingPage page = service.rank(recruiterId, jobId, 1, 2);
        assertEquals(1, page.content().size()); assertEquals(3, page.totalElements()); assertEquals(2, page.totalPages());
    }

    private UUID candidate(String skill) { UUID id = UUID.randomUUID(); CandidateProfile profile = new CandidateProfile(); profile.setSkills(Set.of(skill)); profileData.put(id, profile); return id; }
}
