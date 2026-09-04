package com.dlr.assessment.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentRepository {

    QuizAnswer saveAnswer(QuizAnswer answer);

    List<QuizAnswer> findAnswers(UUID attemptId);

    void deleteAnswer(UUID attemptId, String questionId);

    Checklist saveChecklist(Checklist checklist);

    Optional<Checklist> findChecklist(UUID attemptId);

    void createReview(UUID attemptId, String labCode, Instant dueAt, String reason, Instant createdAt);

    record QuizAnswer(
            UUID id,
            UUID attemptId,
            String questionId,
            Integer selectedChoice,
            String answerText,
            BigDecimal score,
            String feedback,
            Instant updatedAt
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
