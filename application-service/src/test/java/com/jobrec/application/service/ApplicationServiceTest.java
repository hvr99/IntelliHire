package com.jobrec.application.service;

import com.jobrec.application.domain.*;
import com.jobrec.application.exception.*;
import com.jobrec.application.job.*;
import com.jobrec.application.repository.*;
import com.jobrec.application.dto.ApplicationResponse;
import com.jobrec.application.notification.NotificationClient;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationServiceTest {
    @Mock ApplicationRepository applications; @Mock StatusHistoryRepository history; @Mock JobClient jobs; @Mock NotificationClient notifications;
    ApplicationService service; UUID jobId, candidateId, recruiterId;
    @BeforeEach void setUp() { MockitoAnnotations.openMocks(this); service=new ApplicationService(applications,history,jobs,notifications); jobId=UUID.randomUUID(); candidateId=UUID.randomUUID(); recruiterId=UUID.randomUUID(); when(jobs.getJob(jobId)).thenReturn(new JobDetails(jobId,recruiterId,JobStatus.OPEN)); when(applications.save(any())).thenAnswer(i -> i.getArgument(0)); }
    @Test void duplicateApplicationIsRejected() { when(applications.existsByJobIdAndCandidateId(jobId,candidateId)).thenReturn(true); assertThrows(DuplicateApplicationException.class, () -> service.apply(candidateId,jobId)); }
    @Test void invalidTransitionIsRejected() { Application a=application(ApplicationStatus.REJECTED); when(applications.findById(any())).thenReturn(Optional.of(a)); assertThrows(IllegalArgumentException.class, () -> service.changeStatus(recruiterId,a.getId(),ApplicationStatus.INTERVIEW)); }
    @Test void unauthorizedRecruiterIsRejected() { Application a=application(ApplicationStatus.APPLIED); UUID other=UUID.randomUUID(); when(applications.findById(any())).thenReturn(Optional.of(a)); when(jobs.getJob(jobId)).thenReturn(new JobDetails(jobId,other,JobStatus.OPEN)); assertThrows(OwnershipException.class, () -> service.changeStatus(recruiterId,a.getId(),ApplicationStatus.SHORTLISTED)); }
    @Test void unavailableJobServiceIsPropagated() { when(jobs.getJob(jobId)).thenThrow(new JobServiceUnavailableException("down", null)); assertThrows(JobServiceUnavailableException.class, () -> service.apply(candidateId,jobId)); verifyNoInteractions(applications); }
    @Test void statusUpdateSucceedsWhenNotificationFails() { Application a=application(ApplicationStatus.APPLIED); when(applications.findById(any())).thenReturn(Optional.of(a)); doThrow(new RuntimeException("notification down")).when(notifications).notifyApplicationStatusChanged(any(), any(), any()); ApplicationResponse result=service.changeStatus(recruiterId,a.getId(),ApplicationStatus.SHORTLISTED); assertEquals(ApplicationStatus.SHORTLISTED,result.status()); verify(applications).save(a); verify(notifications).notifyApplicationStatusChanged(candidateId,a.getId(),ApplicationStatus.SHORTLISTED); }
    private Application application(ApplicationStatus status) { Application a=new Application(); a.setId(UUID.randomUUID()); a.setJobId(jobId); a.setCandidateId(candidateId); a.setStatus(status); return a; }
}
