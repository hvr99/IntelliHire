package com.jobrec.recommendation.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jobrec.recommendation.domain.CandidateProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.math.BigDecimal;
import java.util.*;

@Component
@ConditionalOnProperty(name = "recommendation.clients.mode", havingValue = "rest")
public class RestProfileClient implements ProfileClient {
    private final RestClient client; private final String internalKey;
    public RestProfileClient(SimpleClientHttpRequestFactory requestFactory, @Value("${user-service.base-url}") String baseUrl,
                             @Value("${internal.api-key}") String internalKey) {
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build(); this.internalKey = internalKey;
    }
    @Override public CandidateProfile getProfile(UUID candidateId) {
        try {
            RemoteProfile remote = client.get().uri("/internal/profiles/{id}", candidateId).header("X-Internal-Key", internalKey).retrieve().body(RemoteProfile.class);
            if (remote == null) throw new DownstreamServiceException("User service returned an empty profile", null);
            RemoteSkill[] skills = client.get().uri("/internal/profiles/{id}/skills", candidateId).header("X-Internal-Key", internalKey).retrieve().body(RemoteSkill[].class);
            CandidateProfile profile = new CandidateProfile(); profile.setSkills(skills == null ? remote.skills : Arrays.stream(skills).map(skill -> skill.name).toList()); profile.setExperienceYears(remote.experienceYears);
            profile.setCertifications(remote.certifications); profile.setPreferredLocation(remote.preferredLocation != null ? remote.preferredLocation : remote.location);
            profile.setPreferredWorkMode(remote.preferredWorkMode); profile.setPreferredSalaryMin(remote.preferredSalaryMin);
            profile.setPreferredSalaryMax(remote.preferredSalaryMax); profile.setEducationLevel(remote.educationLevel);
            profile.setSummary(remote.summary); profile.setExperienceTitles(remote.experienceTitles); return profile;
        } catch (HttpClientErrorException.NotFound ex) { return new CandidateProfile(); }
          catch (RuntimeException ex) { if (ex instanceof DownstreamServiceException d) throw d; throw new DownstreamServiceException("User service is unavailable", ex); }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RemoteProfile {
        public List<String> skills = List.of(); public Integer experienceYears; public List<String> certifications = List.of(); public String summary; public List<String> experienceTitles = List.of();
        public String location; public String preferredLocation; public String preferredWorkMode; public BigDecimal preferredSalaryMin; public BigDecimal preferredSalaryMax; public String educationLevel;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RemoteSkill { public String name; }
}
