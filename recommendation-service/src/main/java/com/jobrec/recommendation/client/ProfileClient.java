package com.jobrec.recommendation.client;

import com.jobrec.recommendation.domain.CandidateProfile;
import java.util.UUID;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public interface ProfileClient {
    CandidateProfile getProfile(UUID candidateId);

    /** Fetches profiles independently so one unavailable candidate does not abort a batch. */
    default Map<UUID, CandidateProfile> getProfiles(Collection<UUID> candidateIds) {
        Map<UUID, CandidateProfile> profiles = new LinkedHashMap<>();
        for (UUID candidateId : candidateIds) {
            try {
                CandidateProfile profile = getProfile(candidateId);
                if (profile != null) profiles.put(candidateId, profile);
            } catch (RuntimeException ignored) {
                // The caller reports this candidate as skipped.
            }
        }
        return profiles;
    }
}
