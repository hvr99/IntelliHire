package com.jobrec.recommendation.dto;

import java.util.List;

public record RecommendationPage(List<RecommendationResponse> content, int number, int size,
                                 long totalElements, int totalPages, boolean first, boolean last) {}
