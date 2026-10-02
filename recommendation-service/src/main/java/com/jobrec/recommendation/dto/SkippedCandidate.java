package com.jobrec.recommendation.dto;

import java.util.UUID;

public record SkippedCandidate(UUID candidateId, String error) {}
