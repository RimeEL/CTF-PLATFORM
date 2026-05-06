package com.ctf.model;
import java.time.LocalDateTime;
import java.util.UUID;

public class User {
	private UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private UserRole role;
    private boolean isActive;
    private LocalDateTime createdAt;
    private UUID teamId;
    // getters / setters
    public UUID getId() {
 		return id;
 	}
 	public void setId(UUID id) {
 		this.id = id;
 	}
 	public String getUsername() {
 		return username;
 	}
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	public String getEmail() {
 		return email;
 	}
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	public String getPasswordHash() {
 		return passwordHash;
 	}
 	public void setPasswordHash(String passwordHash) {
 		this.passwordHash = passwordHash;
 	}
 	public UserRole getRole() {
 		return role;
 	}
 	public void setRole(UserRole role) {
 		this.role = role;
 	}
 	public boolean isActive() {
 		return isActive;
 	}
 	public void setActive(boolean isActive) {
 		this.isActive = isActive;
 	}
 	public LocalDateTime getCreatedAt() {
 		return createdAt;
 	}
 	public void setCreatedAt(LocalDateTime createdAt) {
 		this.createdAt = createdAt;
 	}
 	public UUID getTeamId() {
 		return teamId;
 	}
 	public void setTeamId(UUID teamId) {
 		this.teamId = teamId;
 	}
}