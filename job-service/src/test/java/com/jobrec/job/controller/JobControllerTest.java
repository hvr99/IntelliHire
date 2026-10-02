package com.jobrec.job.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobrec.job.dto.CreateJobRequest;
import com.jobrec.job.dto.JobResponse;
import com.jobrec.job.domain.enums.JobStatus;
import com.jobrec.job.exception.OwnershipException;
import com.jobrec.job.exception.ResourceNotFoundException;
import com.jobrec.job.security.InternalApiKeyFilter;
import com.jobrec.job.security.SecurityConfig;
import com.jobrec.job.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobController.class)
@Import({SecurityConfig.class, InternalApiKeyFilter.class})
class JobControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean JobService jobService;

    private JobResponse sampleResponse() {
        JobResponse r = new JobResponse();
        r.setId(UUID.randomUUID());
        r.setRecruiterId(UUID.randomUUID());
        r.setTitle("Java Dev");
        r.setCompany("Acme");
        r.setStatus(JobStatus.OPEN);
        r.setCreatedAt(LocalDateTime.now());
        r.setSkills(Collections.emptyList());
        r.setCertifications(Collections.emptyList());
        return r;
    }

    @Test
    void createJob_asRecruiter_returns201() throws Exception {
        UUID recruiterId = UUID.randomUUID();
        when(jobService.createJob(any(), any())).thenReturn(sampleResponse());

        CreateJobRequest req = new CreateJobRequest();
        req.setTitle("Java Dev");
        req.setCompany("Acme");

        mockMvc.perform(post("/api/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .with(jwt().jwt(j -> j.subject(recruiterId.toString()).claim("role", "RECRUITER"))
                    .authorities(new SimpleGrantedAuthority("ROLE_RECRUITER"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Java Dev"));
    }

    @Test
    void createJob_asJobSeeker_returns403() throws Exception {
        CreateJobRequest req = new CreateJobRequest();
        req.setTitle("Test");
        req.setCompany("Acme");

        mockMvc.perform(post("/api/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "JOB_SEEKER"))
                    .authorities(new SimpleGrantedAuthority("ROLE_JOB_SEEKER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createJob_missingTitle_returns400() throws Exception {
        CreateJobRequest req = new CreateJobRequest();
        req.setCompany("Acme");
        // title is missing

        mockMvc.perform(post("/api/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "RECRUITER"))
                    .authorities(new SimpleGrantedAuthority("ROLE_RECRUITER"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchJobs_authenticated_returnsPage() throws Exception {
        Page<JobResponse> page = new PageImpl<>(List.of(sampleResponse()));
        when(jobService.searchJobs(any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/jobs")
                .param("keyword", "java")
                .param("page", "0")
                .param("size", "10")
                .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "JOB_SEEKER"))
                    .authorities(new SimpleGrantedAuthority("ROLE_JOB_SEEKER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Java Dev"));
    }

    @Test
    void getJob_authenticated_returnsJob() throws Exception {
        JobResponse resp = sampleResponse();
        when(jobService.getJobById(any())).thenReturn(resp);

        mockMvc.perform(get("/api/jobs/" + resp.getId())
                .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "JOB_SEEKER"))
                    .authorities(new SimpleGrantedAuthority("ROLE_JOB_SEEKER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getJob_notAuthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/jobs/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void closeJob_notOwner_returns403() throws Exception {
        UUID jobId = UUID.randomUUID();
        doThrow(new OwnershipException("You can only modify your own jobs"))
                .when(jobService).closeJob(any(), eq(jobId));

        mockMvc.perform(patch("/api/jobs/" + jobId + "/close")
                .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "RECRUITER"))
                    .authorities(new SimpleGrantedAuthority("ROLE_RECRUITER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyJobs_asRecruiter_returnsPage() throws Exception {
        Page<JobResponse> page = new PageImpl<>(List.of(sampleResponse()));
        when(jobService.getRecruiterJobs(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/jobs/mine")
                .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "RECRUITER"))
                    .authorities(new SimpleGrantedAuthority("ROLE_RECRUITER"))))
                .andExpect(status().isOk());
    }
}
