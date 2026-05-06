package com.ctf.model;
import java.time.LocalDateTime;
import java.util.UUID;

public class Challenge {
    private UUID id;
    private String title;
    private String description;
    private String category;
    private ChallengeDifficulty difficulty;
    private int points;
    private int initialPoints;
    private int minimumPoints;
    private boolean isActive;
    private LocalDateTime createdAt;
    private UUID competitionId;
    
    
    public Challenge() {
		super();
	}



	public Challenge(UUID id, String title, String description, String category, ChallengeDifficulty difficulty,
			int points, int initialPoints, int minimumPoints, boolean isActive, LocalDateTime createdAt,
			UUID competitionId) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.category = category;
		this.difficulty = difficulty;
		this.points = points;
		this.initialPoints = initialPoints;
		this.minimumPoints = minimumPoints;
		this.isActive = isActive;
		this.createdAt = createdAt;
		this.competitionId = competitionId;
	}
    
    

	@Override
	public String toString() {
		return "Challenge [id=" + id + ", title=" + title + ", difficulty=" + difficulty + ", points=" + points + "]";
	}


 
	// getters / setters
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public ChallengeDifficulty getDifficulty() {
		return difficulty;
	}
	public void setDifficulty(ChallengeDifficulty difficulty) {
		this.difficulty = difficulty;
	}
	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}
	public int getInitialPoints() {
		return initialPoints;
	}
	public void setInitialPoints(int initialPoints) {
		this.initialPoints = initialPoints;
	}
	public int getMinimumPoints() {
		return minimumPoints;
	}
	public void setMinimumPoints(int minimumPoints) {
		this.minimumPoints = minimumPoints;
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
	public UUID getCompetitionId() {
		return competitionId;
	}
	public void setCompetitionId(UUID competitionId) {
		this.competitionId = competitionId;
	}
}