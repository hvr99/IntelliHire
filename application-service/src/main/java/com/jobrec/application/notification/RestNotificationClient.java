package com.jobrec.application.notification;

import com.jobrec.application.domain.ApplicationStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.time.Duration;
import java.util.UUID;

@Component
public class RestNotificationClient implements NotificationClient {
    private final RestClient client;
    private final String internalKey;
    public RestNotificationClient(@Value("${notification-service.base-url}") String baseUrl,
                                  @Value("${notification-service.connect-timeout-ms:1000}") int connectTimeout,
                                  @Value("${notification-service.read-timeout-ms:2000}") int readTimeout,
                                  @Value("${internal.api-key}") String internalKey) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeout)); factory.setReadTimeout(Duration.ofMillis(readTimeout));
        client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build(); this.internalKey = internalKey;
    }
    @Override public void notifyApplicationStatusChanged(UUID candidateId, UUID applicationId, ApplicationStatus status) {
        try {
            client.post().uri("/internal/notifications").header("X-Internal-Key", internalKey)
                    .body(new NotificationRequest(candidateId, "APPLICATION_STATUS_CHANGED", "Application status updated",
                            "Your application status changed to " + status.name() + ".", applicationId))
                    .retrieve().toBodilessEntity();
        } catch (RuntimeException exception) { throw new NotificationUnavailableException("Notification service is unavailable", exception); }
    }
    private record NotificationRequest(UUID userId, String type, String title, String message, UUID refId) {}
}
