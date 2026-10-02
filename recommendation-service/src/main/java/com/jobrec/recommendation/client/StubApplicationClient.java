package com.jobrec.recommendation.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
@ConditionalOnProperty(name = "recommendation.clients.mode", havingValue = "stub", matchIfMissing = true)
public class StubApplicationClient implements ApplicationClient {
    private final Map<UUID, List<ApplicationData>> applications = new LinkedHashMap<>();
    private RuntimeException failure;

    public StubApplicationClient addApplication(ApplicationData application) {
        applications.computeIfAbsent(application.jobId(), ignored -> new ArrayList<>()).add(application);
        return this;
    }
    public StubApplicationClient failWith(RuntimeException failure) { this.failure = failure; return this; }
    @Override public List<ApplicationData> getApplications(UUID jobId) {
        if (failure != null) throw failure;
        return List.copyOf(applications.getOrDefault(jobId, List.of()));
    }
}
