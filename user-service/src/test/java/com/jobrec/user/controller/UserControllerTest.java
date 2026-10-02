package com.jobrec.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobrec.user.domain.Profile;
import com.jobrec.user.domain.Skill;
import com.jobrec.user.repository.ProfileRepository;
import com.jobrec.user.repository.SkillRepository;
import com.jobrec.user.security.InternalApiKeyFilter;
import com.jobrec.user.security.SecurityConfig;
import com.jobrec.user.skill.SkillNormalizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, InternalApiKeyFilter.class, SkillNormalizer.class})
@TestPropertySource(properties = "internal.api-key=test_internal_key")
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ProfileRepository profileRepository;
    @MockBean SkillRepository skillRepository;

    @Test
    void getMyProfile_withJobSeeker_success() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findById(userId)).thenReturn(Optional.of(new Profile(userId, "Dev", "Bio", "NY", "123")));

        mockMvc.perform(get("/api/users/me/profile")
                .with(jwt().jwt(j -> j.subject(userId.toString()).claim("role", "JOB_SEEKER")).authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_JOB_SEEKER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Dev"));
    }

    @Test
    void updateMyProfile_withRecruiter_forbidden() throws Exception {
        mockMvc.perform(put("/api/users/me/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Profile()))
                .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "RECRUITER")).authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_RECRUITER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void addSkill_duplicate_returns409() throws Exception {
        UUID userId = UUID.randomUUID();
        Skill skill = new Skill();
        skill.setName("Java");
        skill.setSource("MANUAL");

        when(skillRepository.existsByUserIdAndName(any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/users/me/skills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(skill))
                .with(jwt().jwt(j -> j.subject(userId.toString()).claim("role", "JOB_SEEKER")).authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_JOB_SEEKER"))))
                .andExpect(status().isConflict());
    }

    @Test
    void getCandidateProfile_withRecruiter_success() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findById(userId)).thenReturn(Optional.of(new Profile(userId, "Dev", "Bio", "NY", "123")));

        mockMvc.perform(get("/api/candidates/" + userId + "/profile")
                .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "RECRUITER")).authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_RECRUITER"))))
                .andExpect(status().isOk());
    }

    @Test
    void internalProfile_withKey_success() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findById(userId)).thenReturn(Optional.of(new Profile(userId, "Dev", "Bio", "NY", "123")));

        mockMvc.perform(get("/internal/profiles/" + userId)
                .header("X-Internal-Key", "test_internal_key"))
                .andExpect(status().isOk());
    }

    @Test
    void internalProfile_wrongKey_forbidden() throws Exception {
        mockMvc.perform(get("/internal/profiles/" + UUID.randomUUID())
                .header("X-Internal-Key", "wrong_key"))
                .andExpect(status().isUnauthorized());
    }
}
