package com.jobrec.user.service;

import com.jobrec.user.domain.*;
import com.jobrec.user.ml.MlClient;
import com.jobrec.user.ml.MlUnavailableException;
import com.jobrec.user.repository.SkillRepository;
import com.jobrec.user.repository.SuggestedSkillRepository;
import com.jobrec.user.skill.SkillNormalizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SkillSuggestionServiceTest {
    private SuggestedSkillRepository suggestions;
    private SkillRepository skills;
    private MlClient ml;
    private SkillSuggestionService service;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach void setUp() { suggestions = mock(SuggestedSkillRepository.class); skills = mock(SkillRepository.class); ml = mock(MlClient.class); service = new SkillSuggestionService(suggestions, skills, new SkillNormalizer(), ml); when(suggestions.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0)); }

    @Test void confirmsSelectedAndRejectsUnselected() {
        SuggestedSkill python = suggestion("python", SuggestionSource.RESUME); SuggestedSkill aws = suggestion("aws", SuggestionSource.CERTIFICATE);
        when(suggestions.findByUserIdAndStatus(userId, SuggestionStatus.PENDING)).thenReturn(List.of(python, aws));
        when(suggestions.findById(python.getId())).thenReturn(Optional.of(python));
        when(suggestions.findByUserIdOrderByNameAsc(userId)).thenReturn(List.of(python, aws)); when(skills.existsByUserIdAndName(userId, "python")).thenReturn(false);
        service.confirm(userId, List.of(python.getId()));
        assertEquals(SuggestionStatus.ACCEPTED, python.getStatus()); assertEquals(SuggestionStatus.REJECTED, aws.getStatus());
        ArgumentCaptor<Skill> captor = ArgumentCaptor.forClass(Skill.class); verify(skills).save(captor.capture()); assertEquals("python", captor.getValue().getName()); assertEquals("RESUME", captor.getValue().getSource());
    }

    @Test void doesNotDuplicateConfirmedSkill() {
        SuggestedSkill suggestion = suggestion("python", SuggestionSource.RESUME); when(suggestions.findByUserIdAndStatus(userId, SuggestionStatus.PENDING)).thenReturn(List.of(suggestion)); when(suggestions.findById(suggestion.getId())).thenReturn(Optional.of(suggestion)); when(skills.existsByUserIdAndName(userId, "python")).thenReturn(true);
        service.confirm(userId, List.of(suggestion.getId()));
        verify(skills, never()).save(any()); assertEquals(SuggestionStatus.ACCEPTED, suggestion.getStatus());
    }

    @Test void rejectsSuggestionOwnedByAnotherUser() {
        UUID other = UUID.randomUUID(); SuggestedSkill suggestion = suggestion("python", SuggestionSource.RESUME); suggestion.setUserId(other); when(suggestions.findByUserIdAndStatus(userId, SuggestionStatus.PENDING)).thenReturn(List.of()); when(suggestions.findById(suggestion.getId())).thenReturn(Optional.of(suggestion));
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> service.confirm(userId, List.of(suggestion.getId())));
        verify(skills, never()).save(any());
    }

    @Test void propagatesMlUnavailable() {
        when(ml.extractResume(any(), any(), any())).thenThrow(new MlUnavailableException("ML service is unavailable"));
        assertThrows(MlUnavailableException.class, () -> service.extract(userId, new org.springframework.mock.web.MockMultipartFile("file", "resume.pdf", "application/pdf", new byte[]{1}), SuggestionSource.RESUME));
        verify(suggestions, never()).saveAll(anyList());
    }

    @Test void extractionSavesPendingOnly() {
        when(ml.extractResume(any(), any(), any())).thenReturn(new MlClient.MlExtraction(List.of(new MlClient.MlSkill("JS", .9))));
        List<?> result = service.extract(userId, new org.springframework.mock.web.MockMultipartFile("file", "resume.pdf", "application/pdf", new byte[]{1}), SuggestionSource.RESUME);
        ArgumentCaptor<List<SuggestedSkill>> captor = ArgumentCaptor.forClass(List.class); verify(suggestions).saveAll(captor.capture()); SuggestedSkill saved = captor.getValue().get(0);
        assertEquals("javascript", saved.getName()); assertEquals(SuggestionStatus.PENDING, saved.getStatus()); assertEquals(1, result.size()); verify(skills, never()).save(any());
    }

    private SuggestedSkill suggestion(String name, SuggestionSource source) { SuggestedSkill value = new SuggestedSkill(); value.setId(UUID.randomUUID()); value.setUserId(userId); value.setName(name); value.setSource(source); value.setConfidence(BigDecimal.valueOf(.9)); value.setStatus(SuggestionStatus.PENDING); return value; }
}
