package com.jobrec.auth.dto;

import com.jobrec.auth.domain.enums.Role;
import java.util.UUID;

public class UserResponse {
    private UUID id;
    private String email;
    private Role role;

    public UserResponse() {}
    public UserResponse(UUID id, String email, Role role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
