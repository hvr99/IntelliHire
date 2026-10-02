package com.jobrec.notification.service;

import com.jobrec.notification.domain.Notification;
import com.jobrec.notification.domain.NotificationType;
import com.jobrec.notification.dto.CreateNotificationRequest;
import com.jobrec.notification.dto.NotificationResponse;
import com.jobrec.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository notifications;
    public NotificationService(NotificationRepository notifications) { this.notifications = notifications; }

    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        if (request.type() == NotificationType.RECOMMENDED_JOB && request.refId() != null) {
            var existing = notifications.findFirstByUserIdAndTypeAndRefId(request.userId(), request.type(), request.refId());
            if (existing.isPresent()) return NotificationResponse.from(existing.get());
        }
        Notification notification = new Notification(); notification.setId(UUID.randomUUID()); notification.setUserId(request.userId());
        notification.setType(request.type()); notification.setTitle(request.title()); notification.setMessage(request.message());
        notification.setRefId(request.refId()); notification.setRead(false); notification.setCreatedAt(LocalDateTime.now());
        return NotificationResponse.from(notifications.save(notification));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(UUID userId) { return notifications.findByUserIdOrderByCreatedAtDesc(userId).stream().map(NotificationResponse::from).toList(); }

    @Transactional
    public NotificationResponse markRead(UUID userId, UUID id) {
        Notification notification = notifications.findByIdAndUserId(id, userId).orElseThrow(() -> new NotificationNotFoundException("Notification not found"));
        notification.setRead(true); return NotificationResponse.from(notifications.save(notification));
    }

    @Transactional
    public int markAllRead(UUID userId) {
        List<Notification> owned = notifications.findByUserIdOrderByCreatedAtDesc(userId);
        owned.forEach(notification -> notification.setRead(true)); notifications.saveAll(owned); return owned.size();
    }
}
