package com.ctf.model;
import java.util.UUID;

public class Flag {
    private UUID id;
    private String hash;
    private UUID challengeId;
    // getters / setters
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public UUID getChallengeId() {
		return challengeId;
	}
	public void setChallengeId(UUID challengeId) {
		this.challengeId = challengeId;
	}
}