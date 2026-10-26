package com.dlr.mastery.application;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.catalog.domain.LabContent;
import com.dlr.assessment.domain.QuizGrader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maîtrise des concepts, pilotée par la rétention : un laboratoire réussi n'est « maîtrisé » que si le dernier rappel
 * (question sans le cours) est juste, fait avec confiance, à l'étape finale de la répétition espacée et au moins
 * 7 jours après la fin du laboratoire. Un dernier rappel raté renvoie le concept à revoir (oubli).
 * Sans aucun rappel enregistré (anciennes révisions validées d'un bouton), l'ancienne règle par étape s'applique.
 */
@Service
public class ConceptMasteryService {

    /** Étape de la dernière révision de la répétition espacée (J+30). */
    static final int FINAL_STAGE = 4;
    /** Délai depuis la fin du laboratoire à partir duquel un rappel juste prouve une rétention durable. */
    static final int DURABLE_FROM_DAYS = 7;

    private final LabCatalog catalog;
    private final JdbcTemplate jdbcTemplate;

    public ConceptMasteryService(LabCatalog catalog, JdbcTemplate jdbcTemplate) {
        this.catalog = catalog;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ConceptMastery> list() {
        Map<String, LabProgress> progressByLab = loadProgress().stream()
                .collect(Collectors.toMap(LabProgress::labCode, progress -> progress));
        Map<String, Recall> latestRecalls = latestRecalls();

        return catalog.findAll().stream()
                .flatMap(lab -> lab.keyConcepts().stream().map(concept -> toMastery(lab, concept, progressByLab.get(lab.code()), latestRecalls.get(lab.code()))))
                .toList();
    }

    private ConceptMastery toMastery(LabContent lab, LabContent.KeyConcept concept, LabProgress progress, Recall recall) {
        BigDecimal score = progress == null ? null : progress.score();
        int completedReviewStage = progress == null ? -1 : progress.completedReviewStage();
        MasteryStatus status;
        if (score == null) {
            status = MasteryStatus.NOT_STARTED;
        } else if (score.compareTo(BigDecimal.valueOf(lab.threshold())) < 0) {
            status = MasteryStatus.TO_REVIEW;
        } else if (recall == null) {
            status = completedReviewStage >= FINAL_STAGE ? MasteryStatus.MASTERED : MasteryStatus.CONSOLIDATING;
        } else if (recall.outcome() == QuizGrader.Outcome.WRONG) {
            status = MasteryStatus.TO_REVIEW;
        } else if (recall.provesMastery()) {
            status = MasteryStatus.MASTERED;
        } else {
            status = MasteryStatus.CONSOLIDATING;
        }
        RecallState retention = recall == null ? RecallState.NONE : switch (recall.outcome()) {
            case CORRECT -> RecallState.RETAINED;
            case PARTIAL -> RecallState.SHAKY;
            case WRONG -> RecallState.FORGOTTEN;
        };
        return new ConceptMastery(
                concept.code(), concept.name(), lab.code(), lab.number(), lab.title(), concept.definition(),
                concept.whyExists(), concept.whyImportant(), concept.minimalExample(), concept.professionalExample(),
                concept.codeExample(), concept.commonMistake(),
                concept.masteryQuestion(), concept.masteryProof(), status, score, completedReviewStage,
                retention, recall == null ? null : recall.delayDays());
    }

    /** Dernier rappel (réponse de révision) de chaque laboratoire, avec le délai écoulé depuis la fin du laboratoire. */
    private Map<String, Recall> latestRecalls() {
        Map<String, Recall> latest = new HashMap<>();
        jdbcTemplate.query(
                """
                select i.lab_id, i.repetition_stage, r.score, r.confidence, r.answered_at, a.completed_at
                from review_answer r
                join review_item i on i.id = r.review_id
                join attempt a on a.id = i.attempt_id
                where a.completed_at is not null
                order by r.answered_at, r.id
                """,
                result -> {
                    int delay = (int) Math.max(0, Duration.between(
                            result.getTimestamp("completed_at").toInstant(), result.getTimestamp("answered_at").toInstant()).toDays());
                    int declared = result.getInt("confidence");
                    // Sans confiance déclarée, la révision suppose « plutôt sûr·e », comme à l'enregistrement de la réponse.
                    int confidence = result.wasNull() ? 2 : declared;
                    latest.put(result.getString("lab_id"), new Recall(
                            QuizGrader.outcome(result.getBigDecimal("score")), result.getInt("repetition_stage"), confidence, delay));
                });
        return latest;
    }

    private List<LabProgress> loadProgress() {
        return jdbcTemplate.query(
                """
                select a.lab_id,
                       max(a.score) as score,
                       coalesce(max(case when r.status = 'COMPLETED' then r.repetition_stage else -1 end), -1) as completed_stage
                from attempt a
                left join review_item r on r.attempt_id = a.id
                where a.status in ('COMPLETED', 'COMPLETED_BELOW_THRESHOLD')
                group by a.lab_id
                """,
                (result, row) -> new LabProgress(
                        result.getString("lab_id"), result.getBigDecimal("score"), result.getInt("completed_stage")));
    }

    /** État de la mémoire d'après le dernier rappel : aucun, juste (RETAINED), partiel (SHAKY) ou raté (FORGOTTEN). */
    public enum RecallState {
        NONE,
        RETAINED,
        SHAKY,
        FORGOTTEN
    }

    public enum MasteryStatus {
        NOT_STARTED,
        TO_REVIEW,
        CONSOLIDATING,
        MASTERED
    }

    public record ConceptMastery(
            String code,
            String name,
            String labCode,
            int labNumber,
            String labTitle,
            String definition,
            String whyExists,
            String whyImportant,
            String minimalExample,
            String professionalExample,
            String codeExample,
            String commonMistake,
            String masteryQuestion,
            String masteryProof,
            MasteryStatus status,
            BigDecimal score,
            int completedReviewStage,
            RecallState retention,
            Integer lastRecallDays
    ) {
    }

    private record Recall(QuizGrader.Outcome outcome, int stage, int confidence, int delayDays) {
        boolean provesMastery() {
            return outcome == QuizGrader.Outcome.CORRECT && confidence >= 2
                    && stage >= FINAL_STAGE && delayDays >= DURABLE_FROM_DAYS;
        }
    }

    private record LabProgress(String labCode, BigDecimal score, int completedReviewStage) {
    }
}
