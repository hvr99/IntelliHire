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
