package com.dlr.mastery.application;

import com.dlr.assessment.domain.QuizGrader;
import com.dlr.catalog.application.LabCatalog;
import com.dlr.catalog.domain.LabContent;
import com.dlr.shared.web.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private static final int[] NEXT_INTERVAL_DAYS = {3, 7, 14, 30};
    /** Délai de la révision de chaque étape (J+1, J+3, J+7, J+14, J+30). */
    private static final int[] STAGE_DAYS = {1, 3, 7, 14, 30};

    private final JdbcTemplate jdbcTemplate;
    private final LabCatalog labCatalog;
    private final Clock clock;

    @Autowired
    public ReviewService(JdbcTemplate jdbcTemplate, LabCatalog labCatalog) {
        this(jdbcTemplate, labCatalog, Clock.systemUTC());
    }

    ReviewService(JdbcTemplate jdbcTemplate, LabCatalog labCatalog, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.labCatalog = labCatalog;
        this.clock = clock;
    }

    public List<ReviewItem> pending() {
        return jdbcTemplate.query(
                """
                select r.id, r.attempt_id, r.lab_id, r.due_at, r.reason, r.status, r.repetition_stage, r.created_at,
                       (select max(a.score) from attempt a
                         where a.lab_id = r.lab_id and a.status in ('COMPLETED', 'COMPLETED_BELOW_THRESHOLD')) as best_score
                from review_item r where r.status = 'PENDING' order by r.due_at
                """,
                (result, row) -> {
                    String labCode = result.getString("lab_id");
                    BigDecimal bestScore = result.getBigDecimal("best_score");
                    int stage = result.getInt("repetition_stage");
                    return new ReviewItem(
                            result.getObject("id", UUID.class), result.getObject("attempt_id", UUID.class),
                            labCode, result.getTimestamp("due_at").toInstant(),
                            displayedReason(result.getString("reason"), stage, labCode, bestScore),
                            result.getString("status"), stage,
                            result.getTimestamp("created_at").toInstant(), null,
                            isBelowThreshold(labCode, bestScore), bestScore);
                });
    }

    public static String initialReason(BigDecimal score, int threshold) {
        return score.compareTo(BigDecimal.valueOf(threshold)) < 0
                ? "Score " + score + " % sous le seuil recommandé de " + threshold + " %."
                : "Consolider les concepts du laboratoire avec la répétition espacée.";
    }

    // Le score est relu en direct : le texte enregistré à la validation devient faux après un recalcul.
    private String displayedReason(String stored, int stage, String labCode, BigDecimal bestScore) {
        boolean generated = stage == 0 && (stored.startsWith("Score ") || stored.startsWith("Consolider les concepts"));
        if (!generated || bestScore == null) {
            return stored;
        }
        return labCatalog.findByCode(labCode)
                .map(lab -> initialReason(bestScore, lab.threshold()))
                .orElse(stored);
    }

    public List<ReviewItem> today() {
        Instant now = Instant.now(clock);
        return pending().stream().filter(item -> !item.dueAt().isAfter(now)).toList();
    }

    @Transactional
    public ReviewCompletion complete(UUID id, boolean successful) {
        ReviewItem current = pending().stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("REVIEW_NOT_FOUND", "Révision introuvable : " + id));
        if (successful && current.belowThreshold()) {
            throw new IllegalStateException(
                    "Impossible de valider cette révision : le score du laboratoire (" + current.bestScore()
                            + " %) est sous le seuil recommandé. Revois le laboratoire et recalcule ton score, "
                            + "ou choisis « Encore difficile ».");
        }
        int nextStage = successful ? current.stage() + 1 : current.stage();
        boolean hasNext = !successful || nextStage <= NEXT_INTERVAL_DAYS.length;
        int days = !successful ? 1 : hasNext ? NEXT_INTERVAL_DAYS[nextStage - 1] : 0;
        ReviewItem next = close(current, hasNext ? nextStage : -1, days,
                successful ? "Consolider la maîtrise par répétition espacée." : "Revoir rapidement après une difficulté.");
        return new ReviewCompletion(id, successful, next);
    }

    /** Question de rappel de cette révision : tirée du quiz du laboratoire, sans montrer le cours ni la réponse. */
    public ReviewQuestion question(UUID id) {
        ReviewItem current = find(id);
        LabContent lab = lab(current.labCode());
        LabContent.QuizQuestion question = lab.quiz().get(questionIndex(current, lab));
        return new ReviewQuestion(id, current.labCode(), question.code(), question.type(), question.prompt(),
                question.choices(), current.stage());
    }

    /**
     * Note la réponse de rappel puis planifie la suite d'après le résultat et la confiance déclarée :
     * juste et sûr : étape suivante ; juste mais hésitant : même intervalle ; partielle : plus tôt ; ratée : demain.
     */
    @Transactional
    public ReviewAnswerResult answer(
            UUID id, String questionId, Integer selectedChoice, String answerText, Integer confidence) {
        if (confidence != null && (confidence < 1 || confidence > 3)) {
            throw new IllegalArgumentException("La confiance doit être comprise entre 1 et 3.");
        }
        int declared = confidence == null ? 2 : confidence;
        ReviewItem current = find(id);
        LabContent lab = lab(current.labCode());
        LabContent.QuizQuestion question = lab.quiz().get(questionIndex(current, lab));
        if (!question.code().equals(questionId)) {
            throw new IllegalArgumentException("Cette révision porte sur la question " + question.code() + ".");
        }

        QuizGrader.Grade grade = QuizGrader.grade(question, selectedChoice, answerText);
        QuizGrader.Outcome outcome = QuizGrader.outcome(grade.score());
        Instant now = Instant.now(clock);
        jdbcTemplate.update(
                """
                insert into review_answer
                    (id, review_id, question_id, selected_choice, answer_text, score, outcome, confidence, answered_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), id, question.code(), selectedChoice, answerText, grade.score(), outcome.name(),
                declared, Timestamp.from(now));

        int stage = current.stage();
        int stageDays = STAGE_DAYS[Math.min(stage, STAGE_DAYS.length - 1)];
        int nextStage = stage;
        int days;
        String reason;
        switch (outcome) {
            case CORRECT -> {
                if (declared >= 2) {
                    nextStage = stage + 1;
                    days = nextStage <= NEXT_INTERVAL_DAYS.length ? NEXT_INTERVAL_DAYS[nextStage - 1] : 0;
                    reason = "Consolider la maîtrise par répétition espacée.";
                } else {
                    days = stageDays;
                    reason = "Réponse juste mais hésitante : à confirmer au même intervalle.";
                }
            }
            case PARTIAL -> {
                days = Math.max(1, stageDays / 2);
                reason = "Réponse partielle : à revoir plus tôt.";
            }
            default -> {
                days = 1;
                reason = "Revoir rapidement après une difficulté.";
            }
        }
        ReviewItem next = close(current, days > 0 ? nextStage : -1, days, reason);
        return new ReviewAnswerResult(
                outcome.name(), grade.score(), grade.feedback(), question.explanation(), question.modelAnswer(),
                question.correctChoice(), grade.missingIdeas(), calibration(outcome, declared), next);
    }

    private static String calibration(QuizGrader.Outcome outcome, int confidence) {
        if (outcome == QuizGrader.Outcome.CORRECT) {
            return confidence == 1
                    ? "Tu avais le doute mais tu as eu raison : tu en sais plus que tu ne le crois."
                    : "Confiance et résultat concordent.";
        }
        return confidence == 3
                ? "Tu étais sûr(e) de toi mais la réponse n'est pas acquise : c'est la notion à retravailler en priorité."
                : "Ton doute était justifié : cette notion sera revue bientôt.";
    }

    private ReviewItem find(UUID id) {
        return pending().stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("REVIEW_NOT_FOUND", "Révision introuvable : " + id));
    }

    private LabContent lab(String labCode) {
        return labCatalog.findByCode(labCode)
                .orElseThrow(() -> new ResourceNotFoundException("LAB_NOT_FOUND", "Laboratoire introuvable : " + labCode));
    }

    /** La question change à chaque révision terminée du laboratoire, pour ne pas apprendre la réponse par cœur. */
    private int questionIndex(ReviewItem current, LabContent lab) {
        if (lab.quiz().isEmpty()) {
            throw new IllegalStateException("Aucune question de rappel pour " + lab.code() + ".");
        }
        Integer done = jdbcTemplate.queryForObject(
                "select count(*) from review_item where lab_id = ? and status = 'COMPLETED'", Integer.class, current.labCode());
        return (done == null ? 0 : done) % lab.quiz().size();
    }

    /** Clôt la révision et planifie la suivante dans `days` jours ; une étape négative signifie « terminé ». */
    private ReviewItem close(ReviewItem current, int nextStage, int days, String reason) {
        Instant now = Instant.now(clock);
        jdbcTemplate.update(
                "update review_item set status = 'COMPLETED', completed_at = ? where id = ?",
                Timestamp.from(now), current.id());
        if (nextStage < 0) {
            return null;
        }
        ReviewItem next = new ReviewItem(
                UUID.randomUUID(), current.attemptId(), current.labCode(), now.plus(days, ChronoUnit.DAYS),
                reason, "PENDING", nextStage, now, null, current.belowThreshold(), current.bestScore());
        jdbcTemplate.update(
                """
                insert into review_item
                    (id, attempt_id, lab_id, due_at, reason, status, repetition_stage, created_at)
                values (?, ?, ?, ?, ?, 'PENDING', ?, ?)
                """,
                next.id(), next.attemptId(), next.labCode(), Timestamp.from(next.dueAt()), next.reason(),
                next.stage(), Timestamp.from(now));
        return next;
    }

    private boolean isBelowThreshold(String labCode, BigDecimal bestScore) {
        return bestScore != null && labCatalog.findByCode(labCode)
                .map(LabContent::threshold)
                .map(threshold -> bestScore.compareTo(BigDecimal.valueOf(threshold)) < 0)
                .orElse(false);
    }

    public record ReviewItem(
            UUID id,
            UUID attemptId,
            String labCode,
            Instant dueAt,
            String reason,
            String status,
            int stage,
            Instant createdAt,
            Instant completedAt,
            boolean belowThreshold,
            BigDecimal bestScore
    ) {
    }

    public record ReviewCompletion(UUID completedReviewId, boolean successful, ReviewItem nextReview) {
    }

    /** Question de rappel : sans la bonne réponse, qui n'est révélée qu'après la réponse. */
    public record ReviewQuestion(
            UUID reviewId,
            String labCode,
            String questionId,
            String type,
            String prompt,
            List<String> choices,
            int stage
    ) {
    }

    public record ReviewAnswerResult(
            String outcome,
            BigDecimal score,
            String feedback,
            String explanation,
            String modelAnswer,
            Integer correctChoice,
            List<String> missingIdeas,
            String calibration,
            ReviewItem nextReview
    ) {
    }
}
