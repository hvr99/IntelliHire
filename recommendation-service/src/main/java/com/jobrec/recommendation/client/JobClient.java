package com.jobrec.recommendation.client;

import java.util.List;
import java.util.UUID;

public interface JobClient {
    List<JobData> getOpenJobs();
    JobData getJob(UUID jobId);
}
