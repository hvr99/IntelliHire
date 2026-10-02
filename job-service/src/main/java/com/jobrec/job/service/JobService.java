package com.jobrec.job.service;

import com.jobrec.job.domain.entity.Job;
import com.jobrec.job.domain.entity.JobCertification;
import com.jobrec.job.domain.entity.JobSkill;
import com.jobrec.job.domain.enums.JobStatus;
import com.jobrec.job.dto.CreateJobRequest;
import com.jobrec.job.dto.JobResponse;
import com.jobrec.job.dto.SkillDto;
import com.jobrec.job.exception.OwnershipException;
import com.jobrec.job.exception.ResourceNotFoundException;
import com.jobrec.job.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public JobResponse createJob(UUID recruiterId, CreateJobRequest request) {
        validateSalaryRange(request.getSalaryMin(), request.getSalaryMax());

        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setRecruiterId(recruiterId);
        job.setTitle(request.getTitle());
        job.setCompany(request.getCompany());
        job.setLocation(request.getLocation());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setDescription(request.getDescription());
        job.setMinExperienceYears(request.getMinExperienceYears());
        job.setEducationLevel(request.getEducationLevel());
        job.setWorkMode(request.getWorkMode());
        job.setStatus(JobStatus.OPEN);
        job.setCreatedAt(LocalDateTime.now());

        if (request.getSkills() != null) {
            for (SkillDto dto : request.getSkills()) {
                JobSkill skill = new JobSkill();
                skill.setId(UUID.randomUUID());
                skill.setJob(job);
                skill.setName(dto.getName().trim().toLowerCase());
                skill.setImportance(dto.getImportance());
                job.getSkills().add(skill);
            }
        }
        if (request.getCertifications() != null) {
            for (String certName : request.getCertifications()) {
                JobCertification cert = new JobCertification();
                cert.setId(UUID.randomUUID());
                cert.setJob(job);
                cert.setName(certName);
                job.getCertifications().add(cert);
            }
        }

        return toResponse(jobRepository.save(job));
    }

    @Transactional
    public JobResponse updateJob(UUID recruiterId, UUID jobId, CreateJobRequest request) {
        Job job = findAndVerifyOwnership(jobId, recruiterId);
        validateSalaryRange(request.getSalaryMin(), request.getSalaryMax());

        job.setTitle(request.getTitle());
        job.setCompany(request.getCompany());
        job.setLocation(request.getLocation());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setDescription(request.getDescription());
        job.setMinExperienceYears(request.getMinExperienceYears());
        job.setEducationLevel(request.getEducationLevel());
        job.setWorkMode(request.getWorkMode());

        job.getSkills().clear();
        if (request.getSkills() != null) {
            for (SkillDto dto : request.getSkills()) {
                JobSkill skill = new JobSkill();
                skill.setId(UUID.randomUUID());
                skill.setJob(job);
                skill.setName(dto.getName().trim().toLowerCase());
                skill.setImportance(dto.getImportance());
                job.getSkills().add(skill);
            }
        }
        job.getCertifications().clear();
        if (request.getCertifications() != null) {
            for (String certName : request.getCertifications()) {
                JobCertification cert = new JobCertification();
                cert.setId(UUID.randomUUID());
                cert.setJob(job);
                cert.setName(certName);
                job.getCertifications().add(cert);
            }
        }

        return toResponse(jobRepository.save(job));
    }

    @Transactional
    public void closeJob(UUID recruiterId, UUID jobId) {
        Job job = findAndVerifyOwnership(jobId, recruiterId);
        job.setStatus(JobStatus.CLOSED);
        jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getRecruiterJobs(UUID recruiterId, Pageable pageable) {
        return jobRepository.findByRecruiterId(recruiterId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(String keyword, String location, String workMode,
                                         BigDecimal salaryMin, String skill, Pageable pageable) {
        return jobRepository.searchJobs(JobStatus.OPEN, keyword, location, workMode, salaryMin, skill, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public JobResponse getJobById(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public List<JobResponse> getJobsByIds(List<UUID> ids) {
        return jobRepository.findAllByIdIn(ids).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobResponse> getOpenJobs() {
        return jobRepository.findByStatus(JobStatus.OPEN).stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Job findAndVerifyOwnership(UUID jobId, UUID recruiterId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (!job.getRecruiterId().equals(recruiterId)) {
            throw new OwnershipException("You can only modify your own jobs");
        }
        return job;
    }

    private void validateSalaryRange(BigDecimal min, BigDecimal max) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Minimum salary cannot exceed maximum salary");
        }
    }

    private JobResponse toResponse(Job job) {
        JobResponse r = new JobResponse();
        r.setId(job.getId());
        r.setRecruiterId(job.getRecruiterId());
        r.setTitle(job.getTitle());
        r.setCompany(job.getCompany());
        r.setLocation(job.getLocation());
        r.setSalaryMin(job.getSalaryMin());
        r.setSalaryMax(job.getSalaryMax());
        r.setDescription(job.getDescription());
        r.setMinExperienceYears(job.getMinExperienceYears());
        r.setEducationLevel(job.getEducationLevel());
        r.setWorkMode(job.getWorkMode());
        r.setStatus(job.getStatus());
        r.setCreatedAt(job.getCreatedAt());
        r.setSkills(job.getSkills().stream()
            .map(s -> new SkillDto(s.getName(), s.getImportance()))
            .collect(Collectors.toList()));
        r.setCertifications(job.getCertifications().stream()
            .map(JobCertification::getName)
            .collect(Collectors.toList()));
        return r;
    }
}
