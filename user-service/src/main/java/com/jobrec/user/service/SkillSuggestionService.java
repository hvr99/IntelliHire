package com.jobrec.user.service;

import com.jobrec.user.domain.*;
import com.jobrec.user.dto.SkillSuggestionResponse;
import com.jobrec.user.ml.MlClient;
import com.jobrec.user.repository.SkillRepository;
import com.jobrec.user.repository.SuggestedSkillRepository;
import com.jobrec.user.skill.SkillNormalizer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.*;

@Service
public class SkillSuggestionService {
    private final SuggestedSkillRepository suggestions;
    private final SkillRepository skills;
    private final SkillNormalizer normalizer;
    private final MlClient mlClient;

    public SkillSuggestionService(SuggestedSkillRepository suggestions, SkillRepository skills, SkillNormalizer normalizer, MlClient mlClient) { this.suggestions = suggestions; this.skills = skills; this.normalizer = normalizer; this.mlClient = mlClient; }

    @Transactional
    public List<SkillSuggestionResponse> extract(UUID userId, MultipartFile file, SuggestionSource source) {
        if (file.isEmpty()) throw new IllegalArgumentException("Uploaded file is empty");
        MlClient.MlExtraction extraction = source == SuggestionSource.RESUME
                ? mlClient.extractResume(bytes(file), file.getOriginalFilename(), file.getContentType())
                : mlClient.extractCertificate(bytes(file), file.getOriginalFilename(), file.getContentType());
        Map<String, MlClient.MlSkill> unique = new LinkedHashMap<>();
        for (MlClient.MlSkill extracted : extraction.skills()) {
            SkillNormalizer.NormalizedSkill normalized = normalizer.normalizeOne(extracted.name());
            if (!normalized.skill().isBlank()) unique.putIfAbsent(normalized.skill(), new MlClient.MlSkill(normalized.skill(), extracted.confidence()));
        }
        List<SuggestedSkill> saved = new ArrayList<>();
        for (MlClient.MlSkill extracted : unique.values()) {
            SuggestedSkill suggestion = new SuggestedSkill(); suggestion.setId(UUID.randomUUID()); suggestion.setUserId(userId); suggestion.setName(extracted.name()); suggestion.setSource(source); suggestion.setConfidence(BigDecimal.valueOf(Math.max(0, Math.min(1, extracted.confidence())))); suggestion.setStatus(SuggestionStatus.PENDING); suggestion.setSourceDocument(file.getOriginalFilename()); saved.add(suggestion);
        }
        return suggestions.saveAll(saved).stream().map(SkillSuggestionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SkillSuggestionResponse> list(UUID userId) { return suggestions.findByUserIdOrderByNameAsc(userId).stream().map(SkillSuggestionResponse::from).toList(); }

    @Transactional
    public List<SkillSuggestionResponse> confirm(UUID userId, Collection<UUID> selectedIds) {
        Set<UUID> selected = selectedIds == null ? Set.of() : new HashSet<>(selectedIds);
        List<SuggestedSkill> pending = suggestions.findByUserIdAndStatus(userId, SuggestionStatus.PENDING);
        for (UUID selectedId : selected) {
            SuggestedSkill suggestion = suggestions.findById(selectedId).orElseThrow(() -> new IllegalArgumentException("Skill suggestion not found: " + selectedId));
            if (!userId.equals(suggestion.getUserId())) throw new AccessDeniedException("Skill suggestion does not belong to the current user");
            if (suggestion.getStatus() != SuggestionStatus.PENDING) throw new IllegalArgumentException("Skill suggestion is no longer pending: " + selectedId);
        }
        for (SuggestedSkill suggestion : pending) {
            if (selected.contains(suggestion.getId())) {
                suggestion.setStatus(SuggestionStatus.ACCEPTED);
                if (!skills.existsByUserIdAndName(userId, suggestion.getName())) {
                    Skill skill = new Skill(); skill.setId(UUID.randomUUID()); skill.setUserId(userId); skill.setName(suggestion.getName()); skill.setSource(suggestion.getSource().name()); skills.save(skill);
                }
            } else suggestion.setStatus(SuggestionStatus.REJECTED);
        }
        suggestions.saveAll(pending);
        return list(userId);
    }

    private static byte[] bytes(MultipartFile file) { try { return file.getBytes(); } catch (Exception exception) { throw new IllegalArgumentException("Unable to read uploaded file", exception); } }
}
