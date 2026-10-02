package com.jobrec.recommendation.dto;

import java.util.List;
import java.util.UUID;

public record CandidateRankingResponse(UUID candidateId, double matchPercent, Double semanticScore,
                                       boolean semanticUsed, List<String> matchedSkills,
                                       List<String> missingSkills, String applicationStatus) {}
