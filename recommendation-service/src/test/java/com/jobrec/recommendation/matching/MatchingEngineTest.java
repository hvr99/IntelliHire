package com.jobrec.recommendation.matching;

import com.jobrec.recommendation.domain.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineTest {
    private final MatchingEngine engine = new MatchingEngine();

    @Test void exactSkillMatchScoresFully() {
        JobRequirements job = job(Set.of("Java", "SQL"), Set.of()); CandidateProfile candidate = candidate(Set.of("java", " sql "));
        MatchResult result = engine.match(candidate, job);
        assertEquals(100, result.getTotalScore()); assertEquals(100, result.getComponentScores().get("skills"));
        assertEquals(Set.of("java", "sql"), Set.copyOf(result.getMatchedSkills())); assertTrue(result.getMissingSkills().isEmpty());
    }
    @Test void partialSkillMatchScoresRequiredSkillsMoreHeavily() {
        JobRequirements job = job(Set.of("Java", "SQL"), Set.of("Docker", "Git")); CandidateProfile candidate = candidate(Set.of("java", "docker"));
        MatchResult result = engine.match(candidate, job);
        assertEquals(50.0, result.getComponentScores().get("skills")); assertEquals(50.0, result.getTotalScore());
        assertEquals(Set.of("java", "docker"), Set.copyOf(result.getMatchedSkills())); assertEquals(Set.of("sql", "git"), Set.copyOf(result.getMissingSkills()));
    }
    @Test void allSkillsMissingScoresZero() { MatchResult result = engine.match(candidate(Set.of()), job(Set.of("Java"), Set.of("SQL"))); assertEquals(0, result.getTotalScore()); assertEquals(0, result.getComponentScores().get("skills")); assertEquals(Set.of("java", "sql"), Set.copyOf(result.getMissingSkills())); }
    @Test void experienceBelowRequirementIsPartial() { JobRequirements job = new JobRequirements(); job.setRequiredExperienceYears(4); CandidateProfile candidate = new CandidateProfile(); candidate.setExperienceYears(2); assertEquals(50, engine.match(candidate,job).getTotalScore()); }
    @Test void experienceAboveRequirementIsFull() { JobRequirements job = new JobRequirements(); job.setRequiredExperienceYears(4); CandidateProfile candidate = new CandidateProfile(); candidate.setExperienceYears(7); assertEquals(100, engine.match(candidate,job).getTotalScore()); }
    @Test void certificationMatchScoresFully() { JobRequirements job = new JobRequirements(); job.setRequiredCertifications(Set.of("AWS")); CandidateProfile candidate = new CandidateProfile(); candidate.setCertifications(Set.of("aws")); assertEquals(100, engine.match(candidate,job).getTotalScore()); }
    @Test void certificationMissScoresZero() { JobRequirements job = new JobRequirements(); job.setRequiredCertifications(Set.of("AWS")); CandidateProfile candidate = new CandidateProfile(); candidate.setCertifications(Set.of("Azure")); assertEquals(0, engine.match(candidate,job).getTotalScore()); }
    @Test void preferenceMismatchScoresZero() { JobRequirements job = new JobRequirements(); job.setLocation("Berlin"); job.setWorkMode("REMOTE"); CandidateProfile candidate = new CandidateProfile(); candidate.setPreferredLocation("Paris"); candidate.setPreferredWorkMode("ONSITE"); assertEquals(0, engine.match(candidate,job).getTotalScore()); }
    @Test void emptyProfileDoesNotProduceNaNOrOutOfRangeScore() { MatchResult result = engine.match(new CandidateProfile(), job(Set.of("Java"), Set.of())); assertTrue(result.getTotalScore() >= 0 && result.getTotalScore() <= 100); assertEquals(0, result.getTotalScore()); }
    @Test void weightsMustSumToOne() { assertThrows(IllegalArgumentException.class, () -> new MatchingWeights(.4,.2,.15,.1,.1,.1)); assertThrows(IllegalArgumentException.class, () -> new MatchingWeights(-.1,.2,.15,.1,.1,.55)); assertDoesNotThrow(() -> MatchingWeights.defaults().validate()); }
    @Test void noRequirementRedistributesWeightAcrossApplicableComponents() { JobRequirements job = job(Set.of("Java"), Set.of()); job.setRequiredExperienceYears(4); CandidateProfile candidate = candidate(Set.of("java")); candidate.setExperienceYears(0); MatchResult result = engine.match(candidate,job); assertEquals(66.67, result.getTotalScore()); }

    private static CandidateProfile candidate(Set<String> skills) { CandidateProfile c = new CandidateProfile(); c.setSkills(skills); return c; }
    private static JobRequirements job(Set<String> required, Set<String> preferred) { JobRequirements j = new JobRequirements(); j.setRequiredSkills(required); j.setPreferredSkills(preferred); return j; }
}
