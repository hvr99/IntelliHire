package com.jobrec.application.notification;

import com.jobrec.application.domain.ApplicationStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
@Primary
@Profile("test-stub")
public class StubNotificationClient implements NotificationClient {
    private final List<NotificationEvent> events = new ArrayList<>();
    private RuntimeException failure;
    @Override public void notifyApplicationStatusChanged(UUID candidateId, UUID applicationId, ApplicationStatus status) {
        if (failure != null) throw failure; events.add(new NotificationEvent(candidateId, applicationId, status));
    }
    public StubNotificationClient failWith(RuntimeException error) { failure = error; return this; }
    public List<NotificationEvent> events() { return List.copyOf(events); }
    public record NotificationEvent(UUID candidateId, UUID applicationId, ApplicationStatus status) {}
}
