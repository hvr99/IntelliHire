package com.jobrec.application.dto;
import com.jobrec.application.domain.Application;
import com.jobrec.application.domain.ApplicationStatus;
import java.time.LocalDateTime;
import java.util.UUID;
public record ApplicationResponse(UUID id, UUID jobId, UUID candidateId, ApplicationStatus status,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static ApplicationResponse from(Application a) { return new ApplicationResponse(a.getId(), a.getJobId(), a.getCandidateId(), a.getStatus(), a.getCreatedAt(), a.getUpdatedAt()); }
}
