package com.jobrec.job.controller;

import com.jobrec.job.dto.JobResponse;
import com.jobrec.job.domain.enums.JobStatus;
import com.jobrec.job.security.InternalApiKeyFilter;
import com.jobrec.job.security.SecurityConfig;
import com.jobrec.job.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalJobController.class)
@Import({SecurityConfig.class, InternalApiKeyFilter.class})
@TestPropertySource(properties = "internal.api-key=test_internal_key")
class InternalJobControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean JobService jobService;

    @Test
    void internalGetJob_withKey_returns200() throws Exception {
        JobResponse resp = new JobResponse();
        resp.setId(UUID.randomUUID());
        resp.setTitle("Dev");
        resp.setCompany("Acme");
        resp.setStatus(JobStatus.OPEN);
        resp.setCreatedAt(LocalDateTime.now());
        resp.setSkills(Collections.emptyList());
        resp.setCertifications(Collections.emptyList());

        when(jobService.getJobById(any())).thenReturn(resp);

        mockMvc.perform(get("/internal/jobs/" + resp.getId())
                .header("X-Internal-Key", "test_internal_key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dev"));
    }

    @Test
    void internalGetJob_wrongKey_returns401() throws Exception {
        mockMvc.perform(get("/internal/jobs/" + UUID.randomUUID())
                .header("X-Internal-Key", "wrong_key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void internalGetJob_noKey_returns401() throws Exception {
        mockMvc.perform(get("/internal/jobs/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
