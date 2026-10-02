package com.jobrec.job.dto;

import com.jobrec.job.domain.enums.SkillImportance;

public class SkillDto {
    private String name;
    private SkillImportance importance;

    public SkillDto() {}
    public SkillDto(String name, SkillImportance importance) { this.name = name; this.importance = importance; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SkillImportance getImportance() { return importance; }
    public void setImportance(SkillImportance importance) { this.importance = importance; }
}
