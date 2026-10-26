package com.dlr.assessment.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentRepository {

    QuizAnswer saveAnswer(QuizAnswer answer);

    /** Journalise chaque réponse : la première de chaque question (avant correction) alimente le score V2. */
    void logAnswer(QuizAnswer answer);

    /** Score de la première réponse enregistrée pour chaque question de la tentative. */
    java.util.Map<String, BigDecimal> firstScores(UUID attemptId);

    void recordHint(UUID attemptId, int level, Instant at);

    /** Plus haut niveau d'indice demandé pendant la tentative, 0 si aucun. */
    int highestHintLevel(UUID attemptId);

    List<QuizAnswer> findAnswers(UUID attemptId);

    void deleteAnswer(UUID attemptId, String questionId);

    Checklist saveChecklist(Checklist checklist);

    Optional<Checklist> findChecklist(UUID attemptId);

    void createReview(UUID attemptId, String labCode, Instant dueAt, String reason, Instant createdAt);

    boolean hasPendingReview(String labCode);

    record QuizAnswer(
            UUID id,
            UUID attemptId,
            String questionId,
            Integer selectedChoice,
            String answerText,
            BigDecimal score,
            String feedback,
            Instant updatedAt,
            Integer confidence
    ) {
    }

    record Checklist(UUID attemptId, int completedItems, int totalItems, List<Boolean> completed, Instant updatedAt) {
        public BigDecimal score() {
            return BigDecimal.valueOf(completedItems)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalItems), 2, java.math.RoundingMode.HALF_UP);
        }
    }
}
