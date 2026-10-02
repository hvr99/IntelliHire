package com.jobrec.recommendation.client;

import java.util.List;
import java.util.UUID;

public interface ApplicationClient {
    List<ApplicationData> getApplications(UUID jobId);
}
