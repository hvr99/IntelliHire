package com.jobrec.recommendation.matching;

import com.jobrec.recommendation.domain.JobRequirements;
import com.jobrec.recommendation.domain.MatchResult;
import com.jobrec.recommendation.domain.SkillGap;

import java.util.*;
import java.util.stream.Collectors;

/** Pure skill-gap analysis; it performs no scoring, I/O, network calls, or external lookups. */
public final class SkillGapAnalyzer {
    private static final String SUGGESTION = "Add %s to your profile or upload a certificate showing it.";

    public SkillGap analyze(MatchResult matchResult, JobRequirements requirements) {
        Objects.requireNonNull(matchResult, "matchResult");
        Objects.requireNonNull(requirements, "requirements");

        Set<String> matched = normalize(matchResult.getMatchedSkills());
        Set<String> required = normalize(requirements.getRequiredSkills());
        Set<String> preferred = normalize(requirements.getPreferredSkills());
        preferred.removeAll(required);

        List<String> missingRequired = required.stream().filter(skill -> !matched.contains(skill)).sorted().toList();
        List<String> missingPreferred = preferred.stream().filter(skill -> !matched.contains(skill)).sorted().toList();
        List<String> ordered = new ArrayList<>(missingRequired);
        ordered.addAll(missingPreferred);

        int totalSkills = required.size() + preferred.size();
        int matchedSkills = (int) required.stream().filter(matched::contains).count()
                + (int) preferred.stream().filter(matched::contains).count();
        double coverage = totalSkills == 0 ? 100.0 : round(matchedSkills * 100.0 / totalSkills);

        Map<String, String> suggestions = new LinkedHashMap<>();
        ordered.forEach(skill -> suggestions.put(skill, SUGGESTION.formatted(skill)));
        return new SkillGap(missingRequired, missingPreferred, coverage, ordered, suggestions);
    }

    private static Set<String> normalize(Collection<String> values) {
        if (values == null) return new HashSet<>();
        return values.stream().filter(SkillGapAnalyzer::hasText)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static boolean hasText(String value) { return value != null && !value.trim().isEmpty(); }
    private static double round(double value) { return Math.round(value * 100.0) / 100.0; }
}
