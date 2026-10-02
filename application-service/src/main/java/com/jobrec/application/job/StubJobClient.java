package com.jobrec.application.job;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** In-memory downstream replacement for isolated development and unit tests. */
public class StubJobClient implements JobClient {
    private final Map<UUID, JobDetails> jobs = new HashMap<>();
    private RuntimeException failure;
    public StubJobClient addJob(UUID id, UUID recruiterId, JobStatus status) { jobs.put(id, new JobDetails(id, recruiterId, status)); return this; }
    public StubJobClient failWith(RuntimeException exception) { failure = exception; return this; }
    @Override public JobDetails getJob(UUID jobId) {
        if (failure != null) throw failure;
        JobDetails job = jobs.get(jobId);
        if (job == null) throw new JobServiceUnavailableException("Job not found in stub", null);
        return job;
    }
}
