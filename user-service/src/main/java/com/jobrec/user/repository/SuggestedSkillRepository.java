package com.jobrec.user.repository;

import com.jobrec.user.domain.SuggestedSkill;
import com.jobrec.user.domain.SuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SuggestedSkillRepository extends JpaRepository<SuggestedSkill, UUID> {
    List<SuggestedSkill> findByUserIdOrderByNameAsc(UUID userId);
    List<SuggestedSkill> findByUserIdAndStatus(UUID userId, SuggestionStatus status);
}
