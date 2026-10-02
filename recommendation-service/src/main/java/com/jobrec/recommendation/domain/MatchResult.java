package com.jobrec.recommendation.domain;

import java.util.List;
import java.util.Map;

public record MatchResult(double totalScore, Map<String, Double> componentScores,
                          List<String> matchedSkills, List<String> missingSkills) {
    public MatchResult {
        if (totalScore < 0 || totalScore > 100) throw new IllegalArgumentException("Total score must be between 0 and 100");
        componentScores = Map.copyOf(componentScores);
        matchedSkills = List.copyOf(matchedSkills);
        missingSkills = List.copyOf(missingSkills);
    }
    public double getTotalScore() { return totalScore; }
    public Map<String, Double> getComponentScores() { return componentScores; }
    public List<String> getMatchedSkills() { return matchedSkills; }
    public List<String> getMissingSkills() { return missingSkills; }
}
