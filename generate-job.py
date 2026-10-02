import os

def cf(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content.strip() + '\n')

B = 'job-service/src/main/java/com/jobrec/job'
T = 'job-service/src/test/java/com/jobrec/job'

cf(f'{B}/JobServiceApplication.java', """
package com.jobrec.job;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class JobServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobServiceApplication.class, args);
    }
}
""")

# === ENUMS ===
cf(f'{B}/domain/enums/JobStatus.java', """
package com.jobrec.job.domain.enums;
public enum JobStatus { OPEN, CLOSED }
""")

cf(f'{B}/domain/enums/SkillImportance.java', """
package com.jobrec.job.domain.enums;
public enum SkillImportance { REQUIRED, PREFERRED }
""")

# === ENTITIES ===
cf(f'{B}/domain/entity/Job.java', """
package com.jobrec.job.domain.entity;

import com.jobrec.job.domain.enums.JobStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "jobs")
public class Job {
    @Id private UUID id;
    @Column(name = "recruiter_id", nullable = false) private UUID recruiterId;
    @Column(nullable = false) private String title;
    @Column(nullable = false) private String company;
    private String location;
    @Column(name = "salary_min") private BigDecimal salaryMin;
    @Column(name = "salary_max") private BigDecimal salaryMax;
    private String description;
    @Column(name = "min_experience_years") private Integer minExperienceYears;
    @Column(name = "education_level") private String educationLevel;
    @Column(name = "work_mode") private String workMode;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private JobStatus status;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JobSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JobCertification> certifications = new ArrayList<>();

    public Job() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRecruiterId() { return recruiterId; }
    public void setRecruiterId(UUID recruiterId) { this.recruiterId = recruiterId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public BigDecimal getSalaryMin() { return salaryMin; }
    public void setSalaryMin(BigDecimal salaryMin) { this.salaryMin = salaryMin; }
    public BigDecimal getSalaryMax() { return salaryMax; }
    public void setSalaryMax(BigDecimal salaryMax) { this.salaryMax = salaryMax; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getMinExperienceYears() { return minExperienceYears; }
    public void setMinExperienceYears(Integer minExperienceYears) { this.minExperienceYears = minExperienceYears; }
    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }
    public String getWorkMode() { return workMode; }
    public void setWorkMode(String workMode) { this.workMode = workMode; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<JobSkill> getSkills() { return skills; }
    public void setSkills(List<JobSkill> skills) { this.skills = skills; }
    public List<JobCertification> getCertifications() { return certifications; }
    public void setCertifications(List<JobCertification> certifications) { this.certifications = certifications; }
}
""")

cf(f'{B}/domain/entity/JobSkill.java', """
package com.jobrec.job.domain.entity;

import com.jobrec.job.domain.enums.SkillImportance;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "job_skills")
public class JobSkill {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "job_id", nullable = false) private Job job;
    @Column(nullable = false) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private SkillImportance importance;

    public JobSkill() {}
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SkillImportance getImportance() { return importance; }
    public void setImportance(SkillImportance importance) { this.importance = importance; }
}
""")

cf(f'{B}/domain/entity/JobCertification.java', """
package com.jobrec.job.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "job_certifications")
public class JobCertification {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "job_id", nullable = false) private Job job;
    @Column(nullable = false) private String name;

    public JobCertification() {}
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
""")

# === DTOs ===
cf(f'{B}/dto/CreateJobRequest.java', """
package com.jobrec.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class CreateJobRequest {
    @NotBlank(message = "Title is required") private String title;
    @NotBlank(message = "Company is required") private String company;
    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String description;
    private Integer minExperienceYears;
    private String educationLevel;
    private String workMode;
    private List<SkillDto> skills;
    private List<String> certifications;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public BigDecimal getSalaryMin() { return salaryMin; }
    public void setSalaryMin(BigDecimal salaryMin) { this.salaryMin = salaryMin; }
    public BigDecimal getSalaryMax() { return salaryMax; }
    public void setSalaryMax(BigDecimal salaryMax) { this.salaryMax = salaryMax; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getMinExperienceYears() { return minExperienceYears; }
    public void setMinExperienceYears(Integer minExperienceYears) { this.minExperienceYears = minExperienceYears; }
    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }
    public String getWorkMode() { return workMode; }
    public void setWorkMode(String workMode) { this.workMode = workMode; }
    public List<SkillDto> getSkills() { return skills; }
    public void setSkills(List<SkillDto> skills) { this.skills = skills; }
    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }
}
""")

cf(f'{B}/dto/SkillDto.java', """
package com.jobrec.job.dto;

import com.jobrec.job.domain.enums.SkillImportance;

public class SkillDto {
    private String name;
    private SkillImportance importance;

    public SkillDto() {}
    public SkillDto(String name, SkillImportance importance) { this.name = name; this.importance = importance; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SkillImportance getImportance() { return importance; }
    public void setImportance(SkillImportance importance) { this.importance = importance; }
}
""")

cf(f'{B}/dto/JobResponse.java', """
package com.jobrec.job.dto;

import com.jobrec.job.domain.enums.JobStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class JobResponse {
    private UUID id;
    private UUID recruiterId;
    private String title;
    private String company;
    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String description;
    private Integer minExperienceYears;
    private String educationLevel;
    private String workMode;
    private JobStatus status;
    private LocalDateTime createdAt;
    private List<SkillDto> skills;
    private List<String> certifications;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRecruiterId() { return recruiterId; }
    public void setRecruiterId(UUID recruiterId) { this.recruiterId = recruiterId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public BigDecimal getSalaryMin() { return salaryMin; }
    public void setSalaryMin(BigDecimal salaryMin) { this.salaryMin = salaryMin; }
    public BigDecimal getSalaryMax() { return salaryMax; }
    public void setSalaryMax(BigDecimal salaryMax) { this.salaryMax = salaryMax; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getMinExperienceYears() { return minExperienceYears; }
    public void setMinExperienceYears(Integer minExperienceYears) { this.minExperienceYears = minExperienceYears; }
    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }
    public String getWorkMode() { return workMode; }
    public void setWorkMode(String workMode) { this.workMode = workMode; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<SkillDto> getSkills() { return skills; }
    public void setSkills(List<SkillDto> skills) { this.skills = skills; }
    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }
}
""")

# === REPOSITORY ===
cf(f'{B}/repository/JobRepository.java', """
package com.jobrec.job.repository;

import com.jobrec.job.domain.entity.Job;
import com.jobrec.job.domain.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {
    Page<Job> findByRecruiterId(UUID recruiterId, Pageable pageable);

    @Query("SELECT DISTINCT j FROM Job j LEFT JOIN j.skills s WHERE j.status = :status " +
           "AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "OR LOWER(j.description) LIKE LOWER(CONCAT('%',:keyword,'%'))) " +
           "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%',:location,'%'))) " +
           "AND (:workMode IS NULL OR j.workMode = :workMode) " +
           "AND (:salaryMin IS NULL OR j.salaryMax >= :salaryMin) " +
           "AND (:skill IS NULL OR LOWER(s.name) = LOWER(:skill))")
    Page<Job> searchJobs(@Param("status") JobStatus status,
                         @Param("keyword") String keyword,
                         @Param("location") String location,
                         @Param("workMode") String workMode,
                         @Param("salaryMin") BigDecimal salaryMin,
                         @Param("skill") String skill,
                         Pageable pageable);

    List<Job> findAllByIdIn(List<UUID> ids);
}
""")

# === EXCEPTION ===
cf(f'{B}/exception/ResourceNotFoundException.java', """
package com.jobrec.job.exception;
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
""")

cf(f'{B}/exception/OwnershipException.java', """
package com.jobrec.job.exception;
public class OwnershipException extends RuntimeException {
    public OwnershipException(String message) { super(message); }
}
""")

cf(f'{B}/exception/GlobalExceptionHandler.java', """
package com.jobrec.job.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDenied(AccessDeniedException ex) { throw ex; }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return buildError(HttpStatus.BAD_REQUEST, "Validation Failed", errors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(OwnershipException.class)
    public ResponseEntity<Map<String, Object>> handleOwnership(OwnershipException ex) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadArg(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegrity(DataIntegrityViolationException ex) {
        return buildError(HttpStatus.CONFLICT, "Data integrity violation", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred", null);
    }

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus s, String msg, Object details) {
        Map<String, Object> r = new HashMap<>();
        r.put("status", s.value());
        r.put("error", s.getReasonPhrase());
        r.put("message", msg);
        if (details != null) r.put("details", details);
        return new ResponseEntity<>(r, s);
    }
}
""")

# === SECURITY (reuse pattern from user-service) ===
cf(f'{B}/security/SecurityConfig.java', """
package com.jobrec.job.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");
            if (role != null) {
                return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
            }
            return Collections.emptyList();
        });
        return converter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, InternalApiKeyFilter internalApiKeyFilter) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/internal/**").permitAll()
                .requestMatchers("/api/jobs/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
            .build();
    }
}
""")

cf(f'{B}/security/InternalApiKeyFilter.java', """
package com.jobrec.job.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    @Value("${internal.api-key}")
    private String internalApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/internal/")) {
            String apiKey = request.getHeader("X-Internal-Key");
            if (internalApiKey.equals(apiKey)) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        "internal-service", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_INTERNAL")));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\\"status\\":401,\\"error\\":\\"Unauthorized\\",\\"message\\":\\"Invalid or missing X-Internal-Key\\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
""")

# === SERVICE ===
cf(f'{B}/service/JobService.java', """
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
""")

# === CONTROLLERS ===
cf(f'{B}/controller/JobController.java', """
package com.jobrec.job.controller;

import com.jobrec.job.dto.CreateJobRequest;
import com.jobrec.job.dto.JobResponse;
import com.jobrec.job.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @PostMapping
    public ResponseEntity<JobResponse> createJob(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody CreateJobRequest request) {
        UUID recruiterId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(recruiterId, request));
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @PutMapping("/{jobId}")
    public ResponseEntity<JobResponse> updateJob(@AuthenticationPrincipal Jwt jwt,
                                                  @PathVariable UUID jobId,
                                                  @Valid @RequestBody CreateJobRequest request) {
        UUID recruiterId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(jobService.updateJob(recruiterId, jobId, request));
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @PatchMapping("/{jobId}/close")
    public ResponseEntity<Void> closeJob(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID jobId) {
        UUID recruiterId = UUID.fromString(jwt.getSubject());
        jobService.closeJob(recruiterId, jobId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/mine")
    public ResponseEntity<Page<JobResponse>> getMyJobs(@AuthenticationPrincipal Jwt jwt,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        UUID recruiterId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(jobService.getRecruiterJobs(recruiterId, PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String workMode,
            @RequestParam(required = false) BigDecimal salaryMin,
            @RequestParam(required = false) String skill,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.searchJobs(keyword, location, workMode, salaryMin, skill,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable UUID jobId) {
        return ResponseEntity.ok(jobService.getJobById(jobId));
    }
}
""")

cf(f'{B}/controller/InternalJobController.java', """
package com.jobrec.job.controller;

import com.jobrec.job.dto.JobResponse;
import com.jobrec.job.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/jobs")
public class InternalJobController {

    private final JobService jobService;

    public InternalJobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable UUID jobId) {
        return ResponseEntity.ok(jobService.getJobById(jobId));
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getJobs(@RequestParam List<UUID> ids) {
        return ResponseEntity.ok(jobService.getJobsByIds(ids));
    }
}
""")

# === TESTS ===
cf(f'{T}/service/JobServiceTest.java', """
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
""")

cf(f'{T}/controller/JobControllerTest.java', """
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
""")

cf(f'{T}/controller/InternalJobControllerTest.java', """
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalJobController.class)
@Import({SecurityConfig.class, InternalApiKeyFilter.class})
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
""")

cf(f'{T}/JobServiceApplicationTests.java', """
package com.jobrec.job;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JobServiceApplicationTests {
    @Test
    void contextLoads() {
    }
}
""")

print("Job service generated successfully.")
