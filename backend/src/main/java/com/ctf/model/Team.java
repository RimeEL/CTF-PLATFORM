package com.ctf.model;
import java.time.LocalDateTime;
import java.util.UUID;

public class Team {
    private UUID id;
    private String name;
    private LocalDateTime createdAt;
    private UUID competitionId;
    // getters / setters
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public UUID getCompetitionId() {
		return competitionId;
	}
	public void setCompetitionId(UUID competitionId) {
		this.competitionId = competitionId;
	}
}