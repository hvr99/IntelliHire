package com.jobrec.notification.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification", schema = "notifications")
public class Notification {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private NotificationType type;
    @Column(nullable = false) private String title;
    @Column(nullable = false, length = 2000) private String message;
    @Column(name = "ref_id") private UUID refId;
    @Column(nullable = false) private boolean read;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;

    public UUID getId() { return id; } public void setId(UUID value) { id = value; }
    public UUID getUserId() { return userId; } public void setUserId(UUID value) { userId = value; }
    public NotificationType getType() { return type; } public void setType(NotificationType value) { type = value; }
    public String getTitle() { return title; } public void setTitle(String value) { title = value; }
    public String getMessage() { return message; } public void setMessage(String value) { message = value; }
    public UUID getRefId() { return refId; } public void setRefId(UUID value) { refId = value; }
    public boolean isRead() { return read; } public void setRead(boolean value) { read = value; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime value) { createdAt = value; }
}
