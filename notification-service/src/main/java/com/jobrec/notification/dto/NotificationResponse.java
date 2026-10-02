package com.jobrec.notification.dto;

import com.jobrec.notification.domain.Notification;
import com.jobrec.notification.domain.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(UUID id, UUID userId, NotificationType type, String title,
                                   String message, UUID refId, boolean read, LocalDateTime createdAt) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getUserId(), notification.getType(), notification.getTitle(),
                notification.getMessage(), notification.getRefId(), notification.isRead(), notification.getCreatedAt());
    }
}
