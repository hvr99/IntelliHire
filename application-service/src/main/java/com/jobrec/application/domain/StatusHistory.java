package com.jobrec.application.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "status_history", schema = "applications")
public class StatusHistory {
    @Id private UUID id;
    @Column(name="application_id", nullable=false) private UUID applicationId;
    @Enumerated(EnumType.STRING) @Column(name="old_status") private ApplicationStatus oldStatus;
    @Enumerated(EnumType.STRING) @Column(name="new_status", nullable=false) private ApplicationStatus newStatus;
    @Column(name="changed_at", nullable=false) private LocalDateTime changedAt;
    public UUID getId(){return id;} public void setId(UUID v){id=v;}
    public UUID getApplicationId(){return applicationId;} public void setApplicationId(UUID v){applicationId=v;}
    public ApplicationStatus getOldStatus(){return oldStatus;} public void setOldStatus(ApplicationStatus v){oldStatus=v;}
    public ApplicationStatus getNewStatus(){return newStatus;} public void setNewStatus(ApplicationStatus v){newStatus=v;}
    public LocalDateTime getChangedAt(){return changedAt;} public void setChangedAt(LocalDateTime v){changedAt=v;}
}
