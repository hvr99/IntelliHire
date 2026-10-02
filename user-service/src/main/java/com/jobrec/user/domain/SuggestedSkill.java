package com.jobrec.user.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "suggested_skill")
public class SuggestedSkill {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(nullable = false) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private SuggestionSource source;
    @Column(nullable = false, precision = 5, scale = 4) private BigDecimal confidence;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private SuggestionStatus status;
    @Column(name = "source_document") private String sourceDocument;

    public UUID getId() { return id; }
    public void setId(UUID value) { id = value; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID value) { userId = value; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public SuggestionSource getSource() { return source; }
    public void setSource(SuggestionSource value) { source = value; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal value) { confidence = value; }
    public SuggestionStatus getStatus() { return status; }
    public void setStatus(SuggestionStatus value) { status = value; }
    public String getSourceDocument() { return sourceDocument; }
    public void setSourceDocument(String value) { sourceDocument = value; }
}
