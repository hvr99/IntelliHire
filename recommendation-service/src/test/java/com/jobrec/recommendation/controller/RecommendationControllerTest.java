package com.jobrec.recommendation.controller;

import com.jobrec.recommendation.dto.*;
import com.jobrec.recommendation.service.RecommendationService;
import com.jobrec.recommendation.service.CandidateRankingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({RecommendationController.class, RecruiterRankingController.class})
@Import(com.jobrec.recommendation.security.SecurityConfig.class)
class RecommendationControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean RecommendationService service;
    @MockBean CandidateRankingService rankingService;
    private final UUID candidateId = UUID.randomUUID();

    @Test void unauthenticatedRequestReturns401() throws Exception { mockMvc.perform(get("/api/recommendations")).andExpect(status().isUnauthorized()); }
    @Test void recruiterCannotAccessRecommendations() throws Exception { mockMvc.perform(get("/api/recommendations").with(jwt().jwt(j -> j.subject(candidateId.toString()).claim("role", "RECRUITER")).authorities(new SimpleGrantedAuthority("ROLE_RECRUITER")))).andExpect(status().isForbidden()); }
    @Test void seekerCanAccessRecommendations() throws Exception {
        when(service.recommend(eq(candidateId), eq(0), eq(20), isNull())).thenReturn(new RecommendationPage(List.of(), 0, 20, 0, 0, true, true));
        mockMvc.perform(get("/api/recommendations").with(jwt().jwt(j -> j.subject(candidateId.toString()).claim("role", "JOB_SEEKER")).authorities(new SimpleGrantedAuthority("ROLE_JOB_SEEKER")))).andExpect(status().isOk());
    }
    @Test void downstreamFailureReturns503() throws Exception {
        doThrow(new com.jobrec.recommendation.client.DownstreamServiceException("down", null)).when(service).recommend(eq(candidateId), eq(0), eq(20), isNull());
        mockMvc.perform(get("/api/recommendations").with(jwt().jwt(j -> j.subject(candidateId.toString()).claim("role", "JOB_SEEKER")).authorities(new SimpleGrantedAuthority("ROLE_JOB_SEEKER")))).andExpect(status().isServiceUnavailable());
    }
    @Test void seekerCannotAccessRecruiterRanking() throws Exception {
        mockMvc.perform(get("/api/recruiter/jobs/{jobId}/candidates", UUID.randomUUID())
                .with(jwt().jwt(j -> j.subject(candidateId.toString()).claim("role", "JOB_SEEKER"))
                        .authorities(new SimpleGrantedAuthority("ROLE_JOB_SEEKER"))))
                .andExpect(status().isForbidden());
    }
    @Test void recruiterOwnershipFailureReturns403() throws Exception {
        UUID jobId = UUID.randomUUID();
        doThrow(new com.jobrec.recommendation.exception.RecruiterOwnershipException("not your job"))
                .when(rankingService).rank(eq(candidateId), eq(jobId), eq(0), eq(20));
        mockMvc.perform(get("/api/recruiter/jobs/{jobId}/candidates", jobId)
                .with(jwt().jwt(j -> j.subject(candidateId.toString()).claim("role", "RECRUITER"))
                        .authorities(new SimpleGrantedAuthority("ROLE_RECRUITER"))))
                .andExpect(status().isForbidden());
    }
}
