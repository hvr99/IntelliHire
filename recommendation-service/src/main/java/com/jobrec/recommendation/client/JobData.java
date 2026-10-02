package com.jobrec.recommendation.client;

import com.jobrec.recommendation.domain.JobRequirements;
import java.util.UUID;

public record JobData(UUID id, String title, String company, String location, JobRequirements requirements) {
    public String description() { return requirements == null ? null : requirements.getDescription(); }
    public UUID recruiterId() { return requirements == null ? null : requirements.getRecruiterId(); }
}
