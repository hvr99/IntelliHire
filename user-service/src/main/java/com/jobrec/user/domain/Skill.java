package com.jobrec.user.domain;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "skills")
public class Skill {
    @Id private UUID id;
    @Column(name = "user_id") private UUID userId;
    private String name;
    private String level;
    private Integer years;
    private String source;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Integer getYears() { return years; }
    public void setYears(Integer years) { this.years = years; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
