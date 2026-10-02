package com.jobrec.recommendation.client;

import com.jobrec.recommendation.domain.JobRequirements;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
@ConditionalOnProperty(name = "recommendation.clients.mode", havingValue = "stub", matchIfMissing = true)
public class StubJobClient implements JobClient {
    private final Map<UUID, JobData> jobs = new LinkedHashMap<>(); private RuntimeException failure;
    public StubJobClient addJob(JobData job) { jobs.put(job.id(), job); return this; }
    public StubJobClient addJob(UUID id, String title, String company, String location, JobRequirements requirements) { return addJob(new JobData(id,title,company,location,requirements)); }
    public StubJobClient failWith(RuntimeException failure) { this.failure = failure; return this; }
    @Override public List<JobData> getOpenJobs() { if (failure != null) throw failure; return List.copyOf(jobs.values()); }
    @Override public JobData getJob(UUID id) { if (failure != null) throw failure; JobData job=jobs.get(id); if(job==null) throw new DownstreamServiceException("Job not found", null); return job; }
}
