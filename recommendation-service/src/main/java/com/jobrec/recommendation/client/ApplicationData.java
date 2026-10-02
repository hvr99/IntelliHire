package com.jobrec.recommendation.client;

import java.util.UUID;

public record ApplicationData(UUID id, UUID jobId, UUID candidateId, String status) {}
