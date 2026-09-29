import os

def create_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content.strip() + '\n')

base_dir = 'user-service'

# Security Classes
sec_cfg_java = """
package com.jobrec.user.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Collection;
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
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
            .build();
    }
}
"""
create_file(f'{base_dir}/src/main/java/com/jobrec/user/security/SecurityConfig.java', sec_cfg_java)

int_api_flt_java = """
package com.jobrec.user.security;

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
        
        if (request.getServletPath().startsWith("/internal/")) {
            String apiKey = request.getHeader("X-Internal-Key");
            if (internalApiKey.equals(apiKey)) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        "internal-service", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_INTERNAL")));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or missing X-Internal-Key");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
"""
create_file(f'{base_dir}/src/main/java/com/jobrec/user/security/InternalApiKeyFilter.java', int_api_flt_java)

exc_hnd_java = """
package com.jobrec.user.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Duplicate data or integrity violation", null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred", null);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message, Object details) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        if (details != null) response.put("details", details);
        return new ResponseEntity<>(response, status);
    }
}
"""
create_file(f'{base_dir}/src/main/java/com/jobrec/user/exception/GlobalExceptionHandler.java', exc_hnd_java)

# Entities
profile_java = """
package com.jobrec.user.domain;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "profiles")
public class Profile {
    @Id private UUID userId;
    private String headline;
    private String summary;
    private String location;
    private String phone;
    
    public Profile() {}
    public Profile(UUID userId, String headline, String summary, String location, String phone) {
        this.userId = userId; this.headline = headline; this.summary = summary; this.location = location; this.phone = phone;
    }
    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
"""
create_file(f'{base_dir}/src/main/java/com/jobrec/user/domain/Profile.java', profile_java)

skill_java = """
package com.jobrec.user.domain;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "skills")
public class Skill {
    @Id private UUID id;
    @Column(name = "user_id") private UUID userId;
    private String name;
    private String level;
    private Integer years;
    private String source;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Integer getYears() { return years; }
    public void setYears(Integer years) { this.years = years; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
"""
create_file(f'{base_dir}/src/main/java/com/jobrec/user/domain/Skill.java', skill_java)

repo_prof = """
package com.jobrec.user.repository;
import com.jobrec.user.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface ProfileRepository extends JpaRepository<Profile, UUID> {}
"""
create_file(f'{base_dir}/src/main/java/com/jobrec/user/repository/ProfileRepository.java', repo_prof)

repo_skill = """
package com.jobrec.user.repository;
import com.jobrec.user.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface SkillRepository extends JpaRepository<Skill, UUID> {
    List<Skill> findByUserId(UUID userId);
    boolean existsByUserIdAndName(UUID userId, String name);
}
"""
create_file(f'{base_dir}/src/main/java/com/jobrec/user/repository/SkillRepository.java', repo_skill)

ctrl_java = """
package com.jobrec.user.controller;

import com.jobrec.user.domain.Profile;
import com.jobrec.user.domain.Skill;
import com.jobrec.user.repository.ProfileRepository;
import com.jobrec.user.repository.SkillRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class UserController {

    private final ProfileRepository profileRepository;
    private final SkillRepository skillRepository;

    public UserController(ProfileRepository profileRepository, SkillRepository skillRepository) {
        this.profileRepository = profileRepository;
        this.skillRepository = skillRepository;
    }

    // JOB_SEEKER endpoints
    @PreAuthorize("hasRole('JOB_SEEKER')")
    @GetMapping("/api/users/me/profile")
    public ResponseEntity<Profile> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return profileRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(new Profile(userId, null, null, null, null))); // Empty profile works
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @PutMapping("/api/users/me/profile")
    public ResponseEntity<Profile> updateMyProfile(@AuthenticationPrincipal Jwt jwt, @RequestBody Profile profile) {
        UUID userId = UUID.fromString(jwt.getSubject());
        profile.setUserId(userId);
        return ResponseEntity.ok(profileRepository.save(profile));
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @PostMapping("/api/users/me/skills")
    public ResponseEntity<Skill> addSkill(@AuthenticationPrincipal Jwt jwt, @RequestBody Skill skill) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String name = skill.getName() != null ? skill.getName().trim().toLowerCase() : "";
        if (skillRepository.existsByUserIdAndName(userId, name)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Duplicate rejected
        }
        skill.setId(UUID.randomUUID());
        skill.setUserId(userId);
        skill.setName(name);
        return ResponseEntity.ok(skillRepository.save(skill));
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @GetMapping("/api/users/me/skills")
    public ResponseEntity<List<Skill>> getMySkills(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(skillRepository.findByUserId(userId));
    }

    // RECRUITER endpoints
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/api/candidates/{userId}/profile")
    public ResponseEntity<Profile> getCandidateProfile(@PathVariable UUID userId) {
        return profileRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // INTERNAL endpoints
    @GetMapping("/internal/profiles/{userId}")
    public ResponseEntity<Profile> getInternalProfile(@PathVariable UUID userId) {
        return profileRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
"""
create_file(f'{base_dir}/src/main/java/com/jobrec/user/controller/UserController.java', ctrl_java)

test_ctrl = """
package com.jobrec.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobrec.user.domain.Profile;
import com.jobrec.user.domain.Skill;
import com.jobrec.user.repository.ProfileRepository;
import com.jobrec.user.repository.SkillRepository;
import com.jobrec.user.security.InternalApiKeyFilter;
import com.jobrec.user.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, InternalApiKeyFilter.class})
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
                .with(jwt().jwt(j -> j.subject(userId.toString()).claim("role", "JOB_SEEKER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Dev"));
    }

    @Test
    void updateMyProfile_withRecruiter_forbidden() throws Exception {
        mockMvc.perform(put("/api/users/me/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Profile()))
                .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "RECRUITER"))))
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
                .with(jwt().jwt(j -> j.subject(userId.toString()).claim("role", "JOB_SEEKER"))))
                .andExpect(status().isConflict());
    }

    @Test
    void getCandidateProfile_withRecruiter_success() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findById(userId)).thenReturn(Optional.of(new Profile(userId, "Dev", "Bio", "NY", "123")));

        mockMvc.perform(get("/api/candidates/" + userId + "/profile")
                .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "RECRUITER"))))
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
"""
create_file(f'{base_dir}/src/test/java/com/jobrec/user/controller/UserControllerTest.java', test_ctrl)

test_app = """
package com.jobrec.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceApplicationTests {
    @Test
    void contextLoads() {
    }
}
"""
create_file(f'{base_dir}/src/test/java/com/jobrec/user/UserServiceApplicationTests.java', test_app)

print("User Service phase 2 script done.")
