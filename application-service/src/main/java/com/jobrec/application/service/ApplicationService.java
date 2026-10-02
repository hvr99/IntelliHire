package com.jobrec.application.service;

import com.jobrec.application.domain.*;
import com.jobrec.application.dto.ApplicationResponse;
import com.jobrec.application.exception.*;
import com.jobrec.application.job.*;
import com.jobrec.application.notification.NotificationClient;
import com.jobrec.application.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ApplicationService {
    private static final Map<ApplicationStatus, Set<ApplicationStatus>> ALLOWED_TRANSITIONS = Map.of(
        ApplicationStatus.APPLIED, Set.of(ApplicationStatus.SHORTLISTED, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
        ApplicationStatus.SHORTLISTED, Set.of(ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
        ApplicationStatus.INTERVIEW, Set.of(ApplicationStatus.OFFERED, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
        ApplicationStatus.OFFERED, Set.of(ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
        ApplicationStatus.REJECTED, Set.of(), ApplicationStatus.WITHDRAWN, Set.of());
    private final ApplicationRepository applications;
    private final StatusHistoryRepository history;
    private final JobClient jobClient;
    private final NotificationClient notificationClient;
    public ApplicationService(ApplicationRepository applications, StatusHistoryRepository history, JobClient jobClient) {
        this(applications, history, jobClient, (candidateId, applicationId, status) -> {});
    }
    public ApplicationService(ApplicationRepository applications, StatusHistoryRepository history, JobClient jobClient, NotificationClient notificationClient) {
        this.applications = applications; this.history = history; this.jobClient = jobClient; this.notificationClient = notificationClient;
    }

    @Transactional
    public ApplicationResponse apply(UUID candidateId, UUID jobId) {
        JobDetails job = jobClient.getJob(jobId);
        if (job.status() != JobStatus.OPEN) throw new IllegalArgumentException("Cannot apply to a closed job");
        if (applications.existsByJobIdAndCandidateId(jobId, candidateId)) throw new DuplicateApplicationException("You have already applied to this job");
        LocalDateTime now = LocalDateTime.now();
        Application application = new Application(); application.setId(UUID.randomUUID()); application.setJobId(jobId); application.setCandidateId(candidateId);
        application.setStatus(ApplicationStatus.APPLIED); application.setCreatedAt(now); application.setUpdatedAt(now);
        try { application = applications.save(application); } catch (DataIntegrityViolationException ex) { throw new DuplicateApplicationException("You have already applied to this job"); }
        recordHistory(null, ApplicationStatus.APPLIED, application.getId(), now);
        return ApplicationResponse.from(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listMine(UUID candidateId) { return applications.findByCandidateIdOrderByCreatedAtDesc(candidateId).stream().map(ApplicationResponse::from).toList(); }

    @Transactional
    public ApplicationResponse withdraw(UUID candidateId, UUID applicationId) {
        Application application = applications.findByIdAndCandidateId(applicationId, candidateId).orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        return changeStatus(application, ApplicationStatus.WITHDRAWN);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listForJob(UUID recruiterId, UUID jobId) {
        verifyRecruiterOwnsJob(recruiterId, jobId);
        return applications.findByJobIdOrderByCreatedAtDesc(jobId).stream().map(ApplicationResponse::from).toList();
    }

    @Transactional
    public ApplicationResponse changeStatus(UUID recruiterId, UUID applicationId, ApplicationStatus newStatus) {
        Application application = applications.findById(applicationId).orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        verifyRecruiterOwnsJob(recruiterId, application.getJobId());
        return changeStatus(application, newStatus);
    }

    private ApplicationResponse changeStatus(Application application, ApplicationStatus newStatus) {
        ApplicationStatus old = application.getStatus();
        if (!ALLOWED_TRANSITIONS.getOrDefault(old, Set.of()).contains(newStatus)) throw new IllegalArgumentException("Invalid application status transition");
        LocalDateTime now = LocalDateTime.now(); application.setStatus(newStatus); application.setUpdatedAt(now); applications.save(application);
        recordHistory(old, newStatus, application.getId(), now);
        try { notificationClient.notifyApplicationStatusChanged(application.getCandidateId(), application.getId(), newStatus); }
        catch (RuntimeException ignored) { /* Notification delivery must not roll back status changes. */ }
        return ApplicationResponse.from(application);
    }
    private void recordHistory(ApplicationStatus oldStatus, ApplicationStatus newStatus, UUID applicationId, LocalDateTime changedAt) {
        StatusHistory h = new StatusHistory(); h.setId(UUID.randomUUID()); h.setApplicationId(applicationId); h.setOldStatus(oldStatus); h.setNewStatus(newStatus); h.setChangedAt(changedAt); history.save(h);
    }
    private void verifyRecruiterOwnsJob(UUID recruiterId, UUID jobId) {
        JobDetails job = jobClient.getJob(jobId);
        if (!recruiterId.equals(job.recruiterId())) throw new OwnershipException("You can only manage applications for your own jobs");
    }
}
