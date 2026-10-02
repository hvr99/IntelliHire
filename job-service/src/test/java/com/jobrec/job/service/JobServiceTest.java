package com.jobrec.job.service;

import com.jobrec.job.domain.entity.Job;
import com.jobrec.job.domain.enums.JobStatus;
import com.jobrec.job.dto.CreateJobRequest;
import com.jobrec.job.dto.JobResponse;
import com.jobrec.job.exception.OwnershipException;
import com.jobrec.job.exception.ResourceNotFoundException;
import com.jobrec.job.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock private JobRepository jobRepository;
    @InjectMocks private JobService jobService;
    private UUID recruiterId;
    private UUID otherRecruiterId;

    @BeforeEach
    void setUp() {
        recruiterId = UUID.randomUUID();
        otherRecruiterId = UUID.randomUUID();
    }

    @Test
    void createJob_success() {
        CreateJobRequest req = new CreateJobRequest();
        req.setTitle("Java Dev");
        req.setCompany("Acme");
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        JobResponse resp = jobService.createJob(recruiterId, req);
        assertEquals("Java Dev", resp.getTitle());
        assertEquals(JobStatus.OPEN, resp.getStatus());
    }

    @Test
    void createJob_invalidSalaryRange_throws() {
        CreateJobRequest req = new CreateJobRequest();
        req.setTitle("Dev");
        req.setCompany("Acme");
        req.setSalaryMin(BigDecimal.valueOf(100000));
        req.setSalaryMax(BigDecimal.valueOf(50000));
        assertThrows(IllegalArgumentException.class, () -> jobService.createJob(recruiterId, req));
    }

    @Test
    void closeJob_notOwner_throwsOwnershipException() {
        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setRecruiterId(recruiterId);
        job.setStatus(JobStatus.OPEN);
        when(jobRepository.findById(any())).thenReturn(Optional.of(job));
        assertThrows(OwnershipException.class, () -> jobService.closeJob(otherRecruiterId, job.getId()));
    }

    @Test
    void closeJob_owner_success() {
        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setRecruiterId(recruiterId);
        job.setTitle("Dev");
        job.setCompany("Acme");
        job.setStatus(JobStatus.OPEN);
        job.setCreatedAt(LocalDateTime.now());
        when(jobRepository.findById(any())).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        jobService.closeJob(recruiterId, job.getId());
        assertEquals(JobStatus.CLOSED, job.getStatus());
    }

    @Test
    void getJobById_notFound_throws() {
        when(jobRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> jobService.getJobById(UUID.randomUUID()));
    }
}
