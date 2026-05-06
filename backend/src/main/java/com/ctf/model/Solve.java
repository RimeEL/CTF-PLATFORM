package com.ctf.model;
import java.time.LocalDateTime;
import java.util.UUID;

public class Solve {
    private UUID id;
    private LocalDateTime solvedAt;
    private int awardedPoints;
    private UUID userId;
    private UUID challengeId;
    // getters / setters
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public LocalDateTime getSolvedAt() {
		return solvedAt;
	}
	public void setSolvedAt(LocalDateTime solvedAt) {
		this.solvedAt = solvedAt;
	}
	public int getAwardedPoints() {
		return awardedPoints;
	}
	public void setAwardedPoints(int awardedPoints) {
		this.awardedPoints = awardedPoints;
	}
	public UUID getUserId() {
		return userId;
	}
	public void setUserId(UUID userId) {
		this.userId = userId;
	}
	public UUID getChallengeId() {
		return challengeId;
	}
	public void setChallengeId(UUID challengeId) {
		this.challengeId = challengeId;
	}
	public Solve(UUID id, LocalDateTime solvedAt, int awardedPoints, UUID userId, UUID challengeId) {
		super();
		this.id = id;
		this.solvedAt = solvedAt;
		this.awardedPoints = awardedPoints;
		this.userId = userId;
		this.challengeId = challengeId;
	}
	@Override
	public String toString() {
		return "Solve [id=" + id + ", awardedPoints=" + awardedPoints +  ", challengeId=" + challengeId + "]";
	}
	
	
    
}