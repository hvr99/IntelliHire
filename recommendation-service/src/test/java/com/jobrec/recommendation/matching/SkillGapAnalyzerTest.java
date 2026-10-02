package com.jobrec.recommendation.matching;

import com.jobrec.recommendation.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SkillGapAnalyzerTest {
    private final SkillGapAnalyzer analyzer = new SkillGapAnalyzer();

    @Test void noMissingSkillsProducesFullCoverage() {
        JobRequirements job = job(Set.of("Java"), Set.of("SQL"));
        MatchResult match = result(List.of("java", "sql"), List.of());
        SkillGap gap = analyzer.analyze(match, job);
        assertTrue(gap.getPriorityOrderedMissingSkills().isEmpty());
        assertEquals(100.0, gap.getCoveragePercentage());
        assertTrue(gap.getSuggestions().isEmpty());
    }

    @Test void allSkillsMissingAreReportedWithSuggestions() {
        SkillGap gap = analyzer.analyze(result(List.of(), List.of()), job(Set.of("Java", "SQL"), Set.of("Docker")));
        assertEquals(List.of("java", "sql"), gap.getMissingRequiredSkills());
        assertEquals(List.of("docker"), gap.getMissingPreferredSkills());
        assertEquals(0.0, gap.getCoveragePercentage());
        assertEquals("Add java to your profile or upload a certificate showing it.", gap.getSuggestions().get("java"));
    }

    @Test void onlyPreferredSkillsMissingAreReported() {
        SkillGap gap = analyzer.analyze(result(List.of("java", "sql"), List.of("git")), job(Set.of("Java"), Set.of("SQL", "Git")));
        assertTrue(gap.getMissingRequiredSkills().isEmpty());
        assertEquals(List.of("git"), gap.getMissingPreferredSkills());
        assertEquals(66.67, gap.getCoveragePercentage());
    }

    @Test void requiredSkillsAlwaysComeBeforePreferredSkills() {
        SkillGap gap = analyzer.analyze(result(List.of(), List.of()), job(Set.of("zeta", "alpha"), Set.of("beta", "aardvark")));
        assertEquals(List.of("alpha", "zeta", "aardvark", "beta"), gap.getPriorityOrderedMissingSkills());
    }

    @Test void matchedSkillIsNeverClaimedMissingEvenWhenMatchResultMissingListIsStale() {
        SkillGap gap = analyzer.analyze(result(List.of("JAVA"), List.of("java", "sql")), job(Set.of("Java"), Set.of("SQL")));
        assertEquals(List.of("sql"), gap.getMissingPreferredSkills());
        assertFalse(gap.getPriorityOrderedMissingSkills().contains("java"));
    }

    private static MatchResult result(List<String> matched, List<String> missing) {
        return new MatchResult(50.0, Map.of("skills", 50.0), matched, missing);
    }

    private static JobRequirements job(Set<String> required, Set<String> preferred) {
        JobRequirements job = new JobRequirements();
        job.setRequiredSkills(required); job.setPreferredSkills(preferred);
        return job;
    }
}
