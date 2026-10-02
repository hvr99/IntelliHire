package com.jobrec.recommendation.semantic;

import com.jobrec.recommendation.client.JobData;
import com.jobrec.recommendation.domain.CandidateProfile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "recommendation.semantic.mode", havingValue = "noop", matchIfMissing = true)
public class NoOpSemanticMatcher implements SemanticMatcher {
    @Override public Map<UUID, Double> match(CandidateProfile candidate, List<JobData> jobs) { return Map.of(); }
}
