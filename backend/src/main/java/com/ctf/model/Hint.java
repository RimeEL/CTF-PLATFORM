package com.ctf.model;
import java.util.UUID;

public class Hint {
    private UUID id;
    private String content;
    private UUID challengeId;
    
    public Hint(UUID id, String content, UUID challengeId) {
		super();
		this.id = id;
		this.content = content;
		this.challengeId = challengeId;
	
	}
    
	public Hint() {
		
	}

	// getters / setters
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public UUID getChallengeId() {
		return challengeId;
	}
	public void setChallengeId(UUID challengeId) {
		this.challengeId = challengeId;
	}
    
}