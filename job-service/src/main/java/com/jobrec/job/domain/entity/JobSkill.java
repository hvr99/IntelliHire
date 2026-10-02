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
