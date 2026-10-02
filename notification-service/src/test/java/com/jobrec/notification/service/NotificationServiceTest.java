package com.jobrec.notification.service;

import com.jobrec.notification.domain.Notification;
import com.jobrec.notification.domain.NotificationType;
import com.jobrec.notification.dto.CreateNotificationRequest;
import com.jobrec.notification.dto.NotificationResponse;
import com.jobrec.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {
    @Mock NotificationRepository repository;
    NotificationService service;
    UUID userId;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this); service = new NotificationService(repository); userId = UUID.randomUUID();
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test void createsUnreadNotification() {
        NotificationResponse result = service.create(new CreateNotificationRequest(userId, NotificationType.APPLICATION_STATUS_CHANGED, "Status", "Updated", UUID.randomUUID()));
        assertEquals(userId, result.userId()); assertFalse(result.read()); assertEquals(NotificationType.APPLICATION_STATUS_CHANGED, result.type());
        verify(repository).save(any(Notification.class));
    }

    @Test void readRequiresOwnership() {
        UUID id = UUID.randomUUID(); when(repository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());
        assertThrows(NotificationNotFoundException.class, () -> service.markRead(userId, id));
        verify(repository, never()).save(any());
    }

    @Test void duplicateRecommendationNotificationIsNotCreated() {
        Notification existing = notification(NotificationType.RECOMMENDED_JOB); when(repository.findFirstByUserIdAndTypeAndRefId(userId, NotificationType.RECOMMENDED_JOB, existing.getRefId())).thenReturn(Optional.of(existing));
        NotificationResponse result = service.create(new CreateNotificationRequest(userId, NotificationType.RECOMMENDED_JOB, "Job", "Recommended", existing.getRefId()));
        assertEquals(existing.getId(), result.id()); verify(repository, never()).save(any());
    }

    @Test void readAllUpdatesOnlyOwnedNotifications() {
        Notification first = notification(NotificationType.APPLICATION_STATUS_CHANGED); Notification second = notification(NotificationType.RECOMMENDED_JOB);
        when(repository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(first, second));
        assertEquals(2, service.markAllRead(userId)); assertTrue(first.isRead()); assertTrue(second.isRead()); verify(repository).saveAll(any());
    }

    private Notification notification(NotificationType type) {
        Notification notification = new Notification(); notification.setId(UUID.randomUUID()); notification.setUserId(userId); notification.setType(type); notification.setRefId(UUID.randomUUID()); notification.setTitle("Title"); notification.setMessage("Message"); return notification;
    }
}
