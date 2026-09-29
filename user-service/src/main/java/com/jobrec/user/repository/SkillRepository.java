package com.jobrec.user.repository;
import com.jobrec.user.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface SkillRepository extends JpaRepository<Skill, UUID> {
    List<Skill> findByUserId(UUID userId);
    boolean existsByUserIdAndName(UUID userId, String name);
}
