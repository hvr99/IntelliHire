package com.jobrec.user.controller;

import com.jobrec.user.domain.SuggestionSource;
import com.jobrec.user.dto.ConfirmSkillSuggestionsRequest;
import com.jobrec.user.dto.SkillSuggestionResponse;
import com.jobrec.user.service.SkillSuggestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@PreAuthorize("hasRole('JOB_SEEKER')")
public class SkillSuggestionController {
    private final SkillSuggestionService service;
    public SkillSuggestionController(SkillSuggestionService service) { this.service = service; }
    @PostMapping(value = "/resume", consumes = "multipart/form-data")
    public ResponseEntity<List<SkillSuggestionResponse>> resume(@RequestParam("file") MultipartFile file, Authentication auth) { return ResponseEntity.ok(service.extract(userId(auth), file, SuggestionSource.RESUME)); }
    @PostMapping(value = "/certificates", consumes = "multipart/form-data")
    public ResponseEntity<List<SkillSuggestionResponse>> certificate(@RequestParam("file") MultipartFile file, Authentication auth) { return ResponseEntity.ok(service.extract(userId(auth), file, SuggestionSource.CERTIFICATE)); }
    @GetMapping("/skill-suggestions")
    public List<SkillSuggestionResponse> list(Authentication auth) { return service.list(userId(auth)); }
    @PostMapping("/skill-suggestions/confirm")
    public List<SkillSuggestionResponse> confirm(@Valid @RequestBody ConfirmSkillSuggestionsRequest request, Authentication auth) { return service.confirm(userId(auth), request.suggestionIds()); }
    private UUID userId(Authentication auth) { return UUID.fromString(auth.getName()); }
}
