package com.jobrec.user.domain;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "profiles")
public class Profile {
    @Id private UUID userId;
    private String headline;
    private String summary;
    private String location;
    private String phone;
    
    public Profile() {}
    public Profile(UUID userId, String headline, String summary, String location, String phone) {
        this.userId = userId; this.headline = headline; this.summary = summary; this.location = location; this.phone = phone;
    }
    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
