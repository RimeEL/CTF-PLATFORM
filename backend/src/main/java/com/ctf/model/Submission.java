package com.ctf.model;
import java.time.LocalDateTime;
import java.util.UUID;

public class Submission {
 private UUID id;
 private String submittedFlag;
 private boolean isCorrect;
 private LocalDateTime submittedAt;
 private UUID userId;
 private UUID challengeId;
 // getters / setters
 public UUID getId() {
	return id;
 }
 public void setId(UUID id) {
	this.id = id;
 }
 public String getSubmittedFlag() {
	return submittedFlag;
 }
 public void setSubmittedFlag(String submittedFlag) {
	this.submittedFlag = submittedFlag;
 }
 public boolean isCorrect() {
	return isCorrect;
 }
 public void setCorrect(boolean isCorrect) {
	this.isCorrect = isCorrect;
 }
 public LocalDateTime getSubmittedAt() {
	return submittedAt;
 }
 public void setSubmittedAt(LocalDateTime submittedAt) {
	this.submittedAt = submittedAt;
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
 
}