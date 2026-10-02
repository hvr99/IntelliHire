package com.jobrec.recommendation.dto;

import com.jobrec.recommendation.domain.SkillGap;
import java.util.List;
import java.util.UUID;

public record RecommendationResponse(UUID jobId, String title, String company, String location,
                                     double matchPercent, List<String> matchedSkills,
                                     List<String> missingSkills, SkillGap skillGap,
                                     Double semanticScore, boolean semanticUsed) {
    public RecommendationResponse(UUID jobId, String title, String company, String location,
                                   double matchPercent, List<String> matchedSkills,
                                   List<String> missingSkills, SkillGap skillGap) {
        this(jobId, title, company, location, matchPercent, matchedSkills, missingSkills, skillGap, null, false);
    }
}
