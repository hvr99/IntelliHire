package com.jobrec.recommendation.domain;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;

/** Candidate facts and preferences used by the deterministic matcher. */
public class CandidateProfile {
    private Set<String> skills = new LinkedHashSet<>();
    private Integer experienceYears;
    private Set<String> certifications = new LinkedHashSet<>();
    private String preferredLocation;
    private String preferredWorkMode;
    private BigDecimal preferredSalaryMin;
    private BigDecimal preferredSalaryMax;
    private String educationLevel;
    private String summary;
    private List<String> experienceTitles = List.of();

    public CandidateProfile() {}
    public CandidateProfile(Collection<String> skills, Integer experienceYears, Collection<String> certifications,
                            String preferredLocation, String preferredWorkMode, BigDecimal preferredSalaryMin,
                            BigDecimal preferredSalaryMax, String educationLevel) {
        setSkills(skills); this.experienceYears = experienceYears; setCertifications(certifications);
        this.preferredLocation = preferredLocation; this.preferredWorkMode = preferredWorkMode;
        this.preferredSalaryMin = preferredSalaryMin; this.preferredSalaryMax = preferredSalaryMax;
        this.educationLevel = educationLevel;
    }
    public Set<String> getSkills() { return skills; }
    public void setSkills(Collection<String> value) { skills = value == null ? new LinkedHashSet<>() : new LinkedHashSet<>(value); }
    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer value) { experienceYears = value; }
    public Set<String> getCertifications() { return certifications; }
    public void setCertifications(Collection<String> value) { certifications = value == null ? new LinkedHashSet<>() : new LinkedHashSet<>(value); }
    public String getPreferredLocation() { return preferredLocation; }
    public void setPreferredLocation(String value) { preferredLocation = value; }
    public String getLocation() { return preferredLocation; }
    public void setLocation(String value) { preferredLocation = value; }
    public String getPreferredWorkMode() { return preferredWorkMode; }
    public void setPreferredWorkMode(String value) { preferredWorkMode = value; }
    public String getWorkMode() { return preferredWorkMode; }
    public void setWorkMode(String value) { preferredWorkMode = value; }
    public BigDecimal getPreferredSalaryMin() { return preferredSalaryMin; }
    public void setPreferredSalaryMin(BigDecimal value) { preferredSalaryMin = value; }
    public BigDecimal getSalaryMin() { return preferredSalaryMin; }
    public void setSalaryMin(BigDecimal value) { preferredSalaryMin = value; }
    public BigDecimal getPreferredSalaryMax() { return preferredSalaryMax; }
    public void setPreferredSalaryMax(BigDecimal value) { preferredSalaryMax = value; }
    public BigDecimal getSalaryMax() { return preferredSalaryMax; }
    public void setSalaryMax(BigDecimal value) { preferredSalaryMax = value; }
    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String value) { educationLevel = value; }
    public String getEducation() { return educationLevel; }
    public void setEducation(String value) { educationLevel = value; }
    public String getSummary() { return summary; }
    public void setSummary(String value) { summary = value; }
    public List<String> getExperienceTitles() { return experienceTitles; }
    public void setExperienceTitles(Collection<String> value) { experienceTitles = value == null ? List.of() : List.copyOf(value); }
    public Integer getExperience() { return experienceYears; }
    public void setExperience(Integer value) { experienceYears = value; }
}
