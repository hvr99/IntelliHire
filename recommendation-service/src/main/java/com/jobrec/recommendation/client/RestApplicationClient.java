package com.jobrec.recommendation.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "recommendation.clients.mode", havingValue = "rest")
public class RestApplicationClient implements ApplicationClient {
    private final RestClient client;
    private final String internalKey;

    public RestApplicationClient(SimpleClientHttpRequestFactory requestFactory,
                                 @Value("${application-service.base-url}") String baseUrl,
                                 @Value("${internal.api-key}") String internalKey) {
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.internalKey = internalKey;
    }

    @Override
    public List<ApplicationData> getApplications(UUID jobId) {
        try {
            RemoteApplication[] applications = client.get().uri("/internal/jobs/{jobId}/applications", jobId)
                    .header("X-Internal-Key", internalKey).retrieve().body(RemoteApplication[].class);
            return applications == null ? List.of() : Arrays.stream(applications)
                    .map(application -> new ApplicationData(application.id, application.jobId, application.candidateId,
                            application.status == null ? null : application.status.name()))
                    .toList();
        } catch (RuntimeException exception) {
            throw new DownstreamServiceException("Application service is unavailable", exception);
        }
    }

    private static class RemoteApplication {
        public UUID id;
        public UUID jobId;
        public UUID candidateId;
        public Status status;
    }
    private enum Status { APPLIED, SHORTLISTED, INTERVIEW, OFFERED, REJECTED, WITHDRAWN }
}
