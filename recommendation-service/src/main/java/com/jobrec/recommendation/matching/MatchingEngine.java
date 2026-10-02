package com.jobrec.recommendation.matching;

import com.jobrec.recommendation.domain.CandidateProfile;
import com.jobrec.recommendation.domain.JobRequirements;
import com.jobrec.recommendation.domain.MatchResult;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/** Pure, deterministic matching logic. It has no Spring, persistence, network, or ML dependencies. */
public final class MatchingEngine {
    private static final String SKILLS = "skills";
    private static final String EXPERIENCE = "experience";
    private static final String CERTIFICATION = "certification";
    private static final String PREFERENCES = "preferences";
    private static final String EDUCATION = "education";
    private static final String OTHER = "other";
    private final MatchingWeights weights;
    private final SkillNormalizer skillNormalizer;

    public MatchingEngine() { this(MatchingWeights.defaults(), new SkillNormalizer()); }
    public MatchingEngine(MatchingWeights weights) { this(weights, new SkillNormalizer()); }
    public MatchingEngine(MatchingWeights weights, SkillNormalizer skillNormalizer) { this.weights = Objects.requireNonNull(weights); this.skillNormalizer = Objects.requireNonNull(skillNormalizer); weights.validate(); }

    public MatchResult match(CandidateProfile candidate, JobRequirements job) {
        Objects.requireNonNull(candidate, "candidate"); Objects.requireNonNull(job, "job");
        Set<String> candidateSkills = normalized(candidate.getSkills());
        Set<String> required = normalized(job.getRequiredSkills());
        Set<String> preferred = normalized(job.getPreferredSkills());
        preferred.removeAll(required);
        Set<String> matched = new TreeSet<>(); Set<String> missing = new TreeSet<>();
        required.forEach(skill -> { if (candidateSkills.contains(skill)) matched.add(skill); else missing.add(skill); });
        preferred.forEach(skill -> { if (candidateSkills.contains(skill)) matched.add(skill); else missing.add(skill); });

        Map<String, Double> scores = new LinkedHashMap<>();
        Map<String, Double> configured = new LinkedHashMap<>();
        configured.put(SKILLS, weights.skills()); configured.put(EXPERIENCE, weights.experience()); configured.put(CERTIFICATION, weights.certification());
        configured.put(PREFERENCES, weights.preferences()); configured.put(EDUCATION, weights.education()); configured.put(OTHER, weights.other());
        scores.put(SKILLS, scoreSkills(candidateSkills, required, preferred));
        scores.put(EXPERIENCE, scoreExperience(candidate.getExperienceYears(), job.getRequiredExperienceYears()));
        scores.put(CERTIFICATION, scoreCertifications(candidate.getCertifications(), job.getRequiredCertifications()));
        scores.put(PREFERENCES, scorePreferences(candidate, job));
        scores.put(EDUCATION, scoreEducation(candidate.getEducationLevel(), job.getRequiredEducationLevel()));
        scores.put(OTHER, 0.0);

        Set<String> applicable = new LinkedHashSet<>();
        if (!required.isEmpty() || !preferred.isEmpty()) applicable.add(SKILLS);
        if (job.getRequiredExperienceYears() != null) applicable.add(EXPERIENCE);
        if (!normalized(job.getRequiredCertifications()).isEmpty()) applicable.add(CERTIFICATION);
        if (hasPreferenceRequirement(job)) applicable.add(PREFERENCES);
        if (hasText(job.getRequiredEducationLevel())) applicable.add(EDUCATION);
        // No unspecified "other" requirement is modeled, so its weight is redistributed.
        double configuredTotal = applicable.stream().mapToDouble(configured::get).sum();
        double total = configuredTotal == 0 ? 0 : applicable.stream().mapToDouble(name -> configured.get(name) / configuredTotal * scores.get(name)).sum();
        return new MatchResult(round(clamp(total)), scores, sorted(matched), sorted(missing));
    }

    private double scoreSkills(Set<String> candidate, Set<String> required, Set<String> preferred) {
        double possible = required.size() * 2.0 + preferred.size(); if (possible == 0) return 0;
        double matched = required.stream().filter(candidate::contains).count() * 2.0 + preferred.stream().filter(candidate::contains).count();
        return matched / possible * 100;
    }
    private double scoreExperience(Integer candidate, Integer required) {
        if (required == null) return 0; if (required <= 0) return 100;
        return clamp((Math.max(0, candidate == null ? 0 : candidate) / (double) required) * 100);
    }
    private double scoreCertifications(Collection<String> candidate, Collection<String> required) {
        Set<String> wanted = normalized(required); if (wanted.isEmpty()) return 0;
        Set<String> supplied = normalized(candidate); return (wanted.stream().filter(supplied::contains).count() / (double) wanted.size()) * 100;
    }
    private double scorePreferences(CandidateProfile candidate, JobRequirements job) {
        int count = 0; double score = 0;
        if (hasText(job.getLocation())) { count++; if (same(candidate.getPreferredLocation(), job.getLocation())) score++; }
        if (hasText(job.getWorkMode())) { count++; if (same(candidate.getPreferredWorkMode(), job.getWorkMode())) score++; }
        if (job.getSalaryMin() != null || job.getSalaryMax() != null) { count++; if (salaryOverlaps(candidate, job)) score++; }
        return count == 0 ? 0 : score / count * 100;
    }
    private double scoreEducation(String candidate, String required) {
        if (!hasText(required)) return 0; if (!hasText(candidate)) return 0;
        Integer candidateRank = educationRank(candidate), requiredRank = educationRank(required);
        return candidateRank != null && requiredRank != null ? (candidateRank >= requiredRank ? 100 : 0) : (same(candidate, required) ? 100 : 0);
    }
    private boolean salaryOverlaps(CandidateProfile candidate, JobRequirements job) {
        if (candidate.getPreferredSalaryMin() == null && candidate.getPreferredSalaryMax() == null) return false;
        BigDecimal candidateMin = candidate.getPreferredSalaryMin() == null ? BigDecimal.ZERO : candidate.getPreferredSalaryMin();
        BigDecimal candidateMax = candidate.getPreferredSalaryMax() == null ? new BigDecimal("999999999999") : candidate.getPreferredSalaryMax();
        BigDecimal jobMin = job.getSalaryMin() == null ? BigDecimal.ZERO : job.getSalaryMin();
        BigDecimal jobMax = job.getSalaryMax() == null ? new BigDecimal("999999999999") : job.getSalaryMax();
        return candidateMin.compareTo(jobMax) <= 0 && jobMin.compareTo(candidateMax) <= 0;
    }
    private boolean hasPreferenceRequirement(JobRequirements job) { return hasText(job.getLocation()) || hasText(job.getWorkMode()) || job.getSalaryMin() != null || job.getSalaryMax() != null; }
    private static boolean same(String a, String b) { return hasText(a) && hasText(b) && normalizeText(a).equals(normalizeText(b)); }
    private static boolean hasText(String value) { return value != null && !value.trim().isEmpty(); }
    private static String normalizeText(String value) { return value.trim().toLowerCase(Locale.ROOT); }
    private Set<String> normalized(Collection<String> values) { return skillNormalizer.normalizeToSet(values); }
    private static List<String> sorted(Collection<String> values) { return values.stream().sorted().toList(); }
    private static double clamp(double value) { return Math.max(0, Math.min(100, value)); }
    private static double round(double value) { return Math.round(value * 100.0) / 100.0; }
    private static Integer educationRank(String value) { return switch (normalizeText(value)) { case "high school", "highschool", "secondary" -> 1; case "associate", "associate degree" -> 2; case "bachelor", "bachelor's", "bachelors", "bachelor degree" -> 3; case "master", "master's", "masters", "master degree" -> 4; case "doctorate", "phd", "doctor" -> 5; default -> null; }; }
}
