package com.jobrec.user.dto;

import com.jobrec.user.domain.SuggestedSkill;
import java.math.BigDecimal;
import java.util.UUID;

public record SkillSuggestionResponse(UUID id, String name, String source, BigDecimal confidence, String status, String sourceDocument) {
    public static SkillSuggestionResponse from(SuggestedSkill value) { return new SkillSuggestionResponse(value.getId(), value.getName(), value.getSource().name(), value.getConfidence(), value.getStatus().name(), value.getSourceDocument()); }
}
