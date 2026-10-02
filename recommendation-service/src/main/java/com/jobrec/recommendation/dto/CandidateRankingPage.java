package com.jobrec.recommendation.dto;

import java.util.List;

public record CandidateRankingPage(List<CandidateRankingResponse> content, int number, int size,
                                   long totalElements, int totalPages, boolean first, boolean last,
                                   List<SkippedCandidate> skippedCandidates) {}
