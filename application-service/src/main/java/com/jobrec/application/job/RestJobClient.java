package com.jobrec.application.job;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.time.Duration;
import java.util.UUID;

@Component
public class RestJobClient implements JobClient {
    private final RestClient client;
    private final String internalKey;
    public RestJobClient(@Value("${job-service.base-url}") String baseUrl,
                         @Value("${job-service.connect-timeout-ms:1000}") int connectTimeout,
                         @Value("${job-service.read-timeout-ms:2000}") int readTimeout,
                         @Value("${internal.api-key}") String internalKey) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        factory.setReadTimeout(Duration.ofMillis(readTimeout));
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.internalKey = internalKey;
    }
    @Override
    public JobDetails getJob(UUID jobId) {
        try {
            JobResponse response = client.get().uri("/internal/jobs/{id}", jobId).header("X-Internal-Key", internalKey).retrieve().body(JobResponse.class);
            if (response == null) throw new RestClientException("Empty job response");
            return new JobDetails(response.id(), response.recruiterId(), response.status());
        } catch (RestClientException ex) {
            throw new JobServiceUnavailableException("Job service is unavailable", ex);
        }
    }
    private record JobResponse(UUID id, UUID recruiterId, JobStatus status) {}
}
