package com.jobrec.recommendation.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jobrec.recommendation.domain.JobRequirements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "recommendation.clients.mode", havingValue = "rest")
public class RestJobClient implements JobClient {
    private final RestClient client; private final String internalKey;
    public RestJobClient(SimpleClientHttpRequestFactory requestFactory, @Value("${job-service.base-url}") String baseUrl,
                         @Value("${internal.api-key}") String internalKey) {
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build(); this.internalKey = internalKey;
    }
    @Override public List<JobData> getOpenJobs() {
        try { RemoteJob[] jobs = client.get().uri("/internal/jobs/open").header("X-Internal-Key", internalKey).retrieve().body(RemoteJob[].class); return jobs == null ? List.of() : Arrays.stream(jobs).map(this::map).toList(); }
        catch (RuntimeException ex) { throw new DownstreamServiceException("Job service is unavailable", ex); }
    }
    @Override public JobData getJob(UUID jobId) {
        try { return map(client.get().uri("/internal/jobs/{id}", jobId).header("X-Internal-Key", internalKey).retrieve().body(RemoteJob.class)); }
        catch (RuntimeException ex) { throw new DownstreamServiceException("Job service is unavailable", ex); }
    }
    private JobData map(RemoteJob job) {
        if (job == null) throw new DownstreamServiceException("Job service returned an empty job", null);
        Set<String> required = job.skills == null ? Set.of() : Arrays.stream(job.skills).filter(s -> "REQUIRED".equalsIgnoreCase(s.importance)).map(s -> s.name).collect(Collectors.toSet());
        Set<String> preferred = job.skills == null ? Set.of() : Arrays.stream(job.skills).filter(s -> !"REQUIRED".equalsIgnoreCase(s.importance)).map(s -> s.name).collect(Collectors.toSet());
        JobRequirements requirements = new JobRequirements(required, preferred, job.minExperienceYears, job.certifications, job.location, job.workMode, job.salaryMin, job.salaryMax, job.educationLevel);
        requirements.setDescription(job.description);
        requirements.setRecruiterId(job.recruiterId);
        return new JobData(job.id, job.title, job.company, job.location, requirements);
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RemoteJob { public UUID id; public UUID recruiterId; public String title; public String company; public String location; public String description; public BigDecimal salaryMin; public BigDecimal salaryMax; public Integer minExperienceYears; public String educationLevel; public String workMode; public List<String> certifications; public RemoteSkill[] skills; }
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RemoteSkill { public String name; public String importance; }
}
