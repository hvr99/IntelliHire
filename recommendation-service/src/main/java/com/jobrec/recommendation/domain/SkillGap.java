package com.jobrec.recommendation.domain;

import java.util.List;
import java.util.Map;

/** Deterministic explanation of the skills a candidate still needs for a job. */
public record SkillGap(List<String> missingRequiredSkills,
                       List<String> missingPreferredSkills,
                       double coveragePercentage,
                       List<String> priorityOrderedMissingSkills,
                       Map<String, String> suggestions) {
    public SkillGap {
        if (coveragePercentage < 0 || coveragePercentage > 100) {
            throw new IllegalArgumentException("Coverage percentage must be between 0 and 100");
        }
        missingRequiredSkills = List.copyOf(missingRequiredSkills);
        missingPreferredSkills = List.copyOf(missingPreferredSkills);
        priorityOrderedMissingSkills = List.copyOf(priorityOrderedMissingSkills);
        suggestions = Map.copyOf(suggestions);
    }

    public List<String> getMissingRequiredSkills() { return missingRequiredSkills; }
    public List<String> getMissingPreferredSkills() { return missingPreferredSkills; }
    public double getCoveragePercentage() { return coveragePercentage; }
    public List<String> getPriorityOrderedMissingSkills() { return priorityOrderedMissingSkills; }
    public Map<String, String> getSuggestions() { return suggestions; }
}
