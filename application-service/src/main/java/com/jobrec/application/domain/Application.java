package com.jobrec.application.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "applications", schema = "applications", uniqueConstraints = @UniqueConstraint(name = "uq_applications_job_candidate", columnNames = {"job_id", "candidate_id"}))
public class Application {
    @Id private UUID id;
    @Column(name = "job_id", nullable = false) private UUID jobId;
    @Column(name = "candidate_id", nullable = false) private UUID candidateId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ApplicationStatus status;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    public UUID getId() { return id; } public void setId(UUID v) { id=v; }
    public UUID getJobId() { return jobId; } public void setJobId(UUID v) { jobId=v; }
    public UUID getCandidateId() { return candidateId; } public void setCandidateId(UUID v) { candidateId=v; }
    public ApplicationStatus getStatus() { return status; } public void setStatus(ApplicationStatus v) { status=v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt=v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { updatedAt=v; }
}
