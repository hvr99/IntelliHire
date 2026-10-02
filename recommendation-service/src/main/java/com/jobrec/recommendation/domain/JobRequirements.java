package com.jobrec.recommendation.domain;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Requirements and preferences supplied by a job. */
public class JobRequirements {
    private Set<String> requiredSkills = new LinkedHashSet<>();
    private Set<String> preferredSkills = new LinkedHashSet<>();
    private Integer requiredExperienceYears;
    private Set<String> requiredCertifications = new LinkedHashSet<>();
    private String location;
    private String workMode;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String requiredEducationLevel;
    private String description;
    private UUID recruiterId;

    public JobRequirements() {}
    public JobRequirements(Collection<String> requiredSkills, Collection<String> preferredSkills, Integer experienceYears,
                           Collection<String> certifications, String location, String workMode, BigDecimal salaryMin,
                           BigDecimal salaryMax, String educationLevel) {
        setRequiredSkills(requiredSkills); setPreferredSkills(preferredSkills); requiredExperienceYears = experienceYears;
        setRequiredCertifications(certifications); this.location = location; this.workMode = workMode;
        this.salaryMin = salaryMin; this.salaryMax = salaryMax; requiredEducationLevel = educationLevel;
    }
    public Set<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(Collection<String> value) { requiredSkills = value == null ? new LinkedHashSet<>() : new LinkedHashSet<>(value); }
    public Set<String> getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(Collection<String> value) { preferredSkills = value == null ? new LinkedHashSet<>() : new LinkedHashSet<>(value); }
    public Integer getRequiredExperienceYears() { return requiredExperienceYears; }
    public void setRequiredExperienceYears(Integer value) { requiredExperienceYears = value; }
    public Integer getExperienceYears() { return requiredExperienceYears; }
    public void setExperienceYears(Integer value) { requiredExperienceYears = value; }
    public Set<String> getRequiredCertifications() { return requiredCertifications; }
    public void setRequiredCertifications(Collection<String> value) { requiredCertifications = value == null ? new LinkedHashSet<>() : new LinkedHashSet<>(value); }
    public Set<String> getCertifications() { return requiredCertifications; }
    public void setCertifications(Collection<String> value) { setRequiredCertifications(value); }
    public String getLocation() { return location; }
    public void setLocation(String value) { location = value; }
    public String getWorkMode() { return workMode; }
    public void setWorkMode(String value) { workMode = value; }
    public BigDecimal getSalaryMin() { return salaryMin; }
    public void setSalaryMin(BigDecimal value) { salaryMin = value; }
    public BigDecimal getSalaryMax() { return salaryMax; }
    public void setSalaryMax(BigDecimal value) { salaryMax = value; }
    public String getRequiredEducationLevel() { return requiredEducationLevel; }
    public void setRequiredEducationLevel(String value) { requiredEducationLevel = value; }
    public String getEducationLevel() { return requiredEducationLevel; }
    public void setEducationLevel(String value) { requiredEducationLevel = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public UUID getRecruiterId() { return recruiterId; }
    public void setRecruiterId(UUID value) { recruiterId = value; }
}
