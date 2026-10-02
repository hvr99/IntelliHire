package com.jobrec.recommendation.semantic;

import com.jobrec.recommendation.client.JobData;
import com.jobrec.recommendation.domain.CandidateProfile;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.LinkedHashMap;

/** Optional semantic enrichment; an empty result never replaces weighted matching. */
public interface SemanticMatcher {
    Map<UUID, Double> match(CandidateProfile candidate, List<JobData> jobs);

    default Map<UUID, Double> matchCandidates(JobData job, Map<UUID, CandidateProfile> candidates) {
        Map<UUID, Double> scores = new LinkedHashMap<>();
        candidates.forEach((candidateId, profile) -> {
            Map<UUID, Double> result = match(profile, List.of(job));
            if (result != null && result.containsKey(job.id())) scores.put(candidateId, result.get(job.id()));
        });
        return scores;
    }
}
