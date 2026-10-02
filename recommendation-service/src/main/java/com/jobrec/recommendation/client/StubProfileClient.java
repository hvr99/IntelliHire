package com.jobrec.recommendation.client;

import com.jobrec.recommendation.domain.CandidateProfile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "recommendation.clients.mode", havingValue = "stub", matchIfMissing = true)
public class StubProfileClient implements ProfileClient {
    private CandidateProfile profile = new CandidateProfile(); private RuntimeException failure;
    public StubProfileClient setProfile(CandidateProfile profile) { this.profile = profile; return this; }
    public StubProfileClient failWith(RuntimeException failure) { this.failure = failure; return this; }
    @Override public CandidateProfile getProfile(UUID candidateId) { if (failure != null) throw failure; return profile; }
}
