package com.ctf.model;
import java.util.UUID;

public class DynamicScoring {
    private UUID challengeId;
    private double decayRate;
    // getters / setters
	public UUID getChallengeId() {
		return challengeId;
	}
	public void setChallengeId(UUID challengeId) {
		this.challengeId = challengeId;
	}
	public double getDecayRate() {
		return decayRate;
	}
	public void setDecayRate(double decayRate) {
		this.decayRate = decayRate;
	}
	public DynamicScoring(UUID challengeId, double decayRate) {
		
		this.challengeId = challengeId;
		this.decayRate = decayRate;
	}
	public DynamicScoring() {
		
	}
	
	
    
}