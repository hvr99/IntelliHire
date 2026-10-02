package com.jobrec.application.notification;

import com.jobrec.application.domain.ApplicationStatus;
import java.util.UUID;

public interface NotificationClient {
    void notifyApplicationStatusChanged(UUID candidateId, UUID applicationId, ApplicationStatus status);
}
