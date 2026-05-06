package com.ctf.service;

import com.ctf.dao.FlagDAOImpl;
import com.ctf.dao.SubmissionDAOImpl;
import com.ctf.model.Flag;
import com.ctf.model.Submission;
import com.ctf.util.HashingUtil;
import java.time.LocalDateTime;
import java.util.UUID;

public class SubmissionService {

    public enum SubmitResult {
        CORRECT,
        INCORRECT,
        ALREADY_SOLVED,
        CHALLENGE_NOT_FOUND
    }

    private final SubmissionDAOImpl submissionDAO = new SubmissionDAOImpl();
    private final FlagDAOImpl flagDAO = new FlagDAOImpl();

    public SubmitResult submit(UUID userId, UUID challengeId, String rawFlag) {

        // 1. Anti-doublon
        if (submissionDAO.existsCorrectSubmission(userId, challengeId)) {
            return SubmitResult.ALREADY_SOLVED;
        }

        // 2. Récupérer le flag stocké
        Flag storedFlag = flagDAO.findByChallengeId(challengeId);
        if (storedFlag == null) {
            return SubmitResult.CHALLENGE_NOT_FOUND;
        }

        // 3. Comparer les hashs
        String submittedHash = HashingUtil.hash(rawFlag);
        boolean isCorrect = submittedHash.equals(storedFlag.getHash());

        // 4. Persister la soumission
        Submission submission = new Submission();
        submission.setId(UUID.randomUUID());
        submission.setSubmittedFlag(submittedHash);
        submission.setCorrect(isCorrect);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setUserId(userId);
        submission.setChallengeId(challengeId);
        submissionDAO.save(submission);

        // 5. Si correct → scorer
        if (isCorrect) {
            // scoringService.registerSolve(userId, challengeId); // à activer quand P4 livre ScoringService
            System.out.println("TODO: scoringService.registerSolve(" + userId + ", " + challengeId + ")");
        }

        return isCorrect ? SubmitResult.CORRECT : SubmitResult.INCORRECT;
    }
}