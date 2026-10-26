package com.dlr.adaptation.application;

import com.dlr.assessment.domain.QuizGrader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rétention différée : ce qui reste d'un laboratoire quelques jours après l'avoir terminé.
 * Chaque réponse de révision (rappel sans le cours) est rangée selon le délai écoulé depuis la fin du laboratoire
 * et comparée à la première réponse du quiz initial sur la même question.
 */
@Service
public class RetentionService {

    static final int MIN_RECALLS = 5;
    /** À partir de ce délai, un rappel juste compte comme une rétention durable. */
    static final int DURABLE_FROM_DAYS = 7;
    /** Nombre minimal de rappels éloignés (3 jours et plus) pour juger la rétention. */
    static final int MIN_DELAYED = 3;
    static final int SOLID_PERCENT = 80;
    static final int FRAGILE_PERCENT = 50;
    static final int MAX_LISTED = 8;

    private static final int[] BUCKET_FROM = {0, 3, 8, 15};
    private static final String[] BUCKET_LABEL = {"Moins de 3 jours", "3 à 7 jours", "8 à 14 jours", "15 jours et plus"};

    private final JdbcTemplate jdbcTemplate;
    private final QuestionPrompts prompts;

    public RetentionService(JdbcTemplate jdbcTemplate, QuestionPrompts prompts) {
        this.jdbcTemplate = jdbcTemplate;
        this.prompts = prompts;
    }

    public Retention retention() {
        List<Sample> samples = samples();
        List<Bucket> buckets = new ArrayList<>();
        for (int index = 0; index < BUCKET_FROM.length; index++) {
            buckets.add(bucket(index, samples));
        }
        List<Sample> delayed = samples.stream().filter(sample -> sample.delayDays() >= BUCKET_FROM[1]).toList();
        int delayedPercent = percent(delayed.stream().filter(Sample::correct).count(), delayed.size());
        Verdict verdict = verdict(samples.size(), delayed.size(), delayedPercent);
        return new Retention(samples.size(), buckets, verdict.code(), verdict.message(),
                retainedLabs(samples), forgotten(samples));
    }

    private Verdict verdict(int total, int delayed, int delayedPercent) {
        if (total < MIN_RECALLS) {
            return new Verdict("INSUFFICIENT_DATA", "Pas encore assez de rappels (" + total + " sur " + MIN_RECALLS
                    + ") : la rétention se mesure avec les révisions, qui reposent sur des questions sans le cours.");
        }
        if (delayed < MIN_DELAYED) {
            return new Verdict("TOO_EARLY", "Tes rappels sont encore très proches de la fin des laboratoires : "
                    + "la mesure devient utile à partir de 3 jours après.");
        }
        if (delayedPercent >= SOLID_PERCENT) {
            return new Verdict("DURABLE", "Tu retiens bien : " + delayedPercent + " % de tes rappels à 3 jours et plus sont justes.");
        }
        if (delayedPercent < FRAGILE_PERCENT) {
            return new Verdict("FRAGILE", "Ça s'efface vite : seulement " + delayedPercent
                    + " % de tes rappels à 3 jours et plus sont justes. Reviens sur les oublis ci-dessous.");
        }
        return new Verdict("MIXED", "Rétention partielle : " + delayedPercent
                + " % de tes rappels à 3 jours et plus sont justes. Les oublis ci-dessous sont à retravailler.");
    }

    private List<Sample> samples() {
        List<Sample> samples = new ArrayList<>();
        jdbcTemplate.query(
                """
                select r.question_id, r.score, r.answered_at, i.lab_id, a.completed_at,
                       coalesce(
                           (select l.score from quiz_answer_log l
                             where l.attempt_id = i.attempt_id and l.question_id = r.question_id
                             order by l.answered_at, l.id limit 1),
                           (select q.score from quiz_answer q
                             where q.attempt_id = i.attempt_id and q.question_id = r.question_id)) as initial_score
                from review_answer r
                join review_item i on i.id = r.review_id
                join attempt a on a.id = i.attempt_id
                where a.completed_at is not null
                order by r.answered_at, r.id
                """,
                result -> {
                    Instant answeredAt = result.getTimestamp("answered_at").toInstant();
                    Instant completedAt = result.getTimestamp("completed_at").toInstant();
                    int delay = (int) Math.max(0, Duration.between(completedAt, answeredAt).toDays());
                    java.math.BigDecimal initialScore = result.getBigDecimal("initial_score");
                    samples.add(new Sample(result.getString("lab_id"), result.getString("question_id"), answeredAt, delay,
                            QuizGrader.outcome(result.getBigDecimal("score")),
                            initialScore == null ? null : QuizGrader.outcome(initialScore)));
                });
        return samples;
    }

    private Bucket bucket(int index, List<Sample> samples) {
        int from = BUCKET_FROM[index];
        int to = index + 1 < BUCKET_FROM.length ? BUCKET_FROM[index + 1] : Integer.MAX_VALUE;
        int correct = 0;
        int partial = 0;
        int wrong = 0;
        int initialSamples = 0;
        int initialCorrect = 0;
        for (Sample sample : samples) {
            if (sample.delayDays() < from || sample.delayDays() >= to) continue;
            switch (sample.outcome()) {
                case CORRECT -> correct++;
                case PARTIAL -> partial++;
                case WRONG -> wrong++;
            }
            if (sample.initial() != null) {
                initialSamples++;
                if (sample.initial() == QuizGrader.Outcome.CORRECT) initialCorrect++;
            }
        }
        int total = correct + partial + wrong;
        return new Bucket(BUCKET_LABEL[index], from, index + 1 < BUCKET_FROM.length ? to - 1 : null, total, correct, partial,
                wrong, percent(correct, total), initialSamples, percent(initialCorrect, initialSamples));
    }

    /** Laboratoires dont le dernier rappel, fait au moins 7 jours après la fin, est juste. */
    private List<RetainedLab> retainedLabs(List<Sample> samples) {
        Map<String, Sample> latest = new LinkedHashMap<>();
        samples.stream().sorted(Comparator.comparing(Sample::answeredAt))
                .forEach(sample -> latest.put(sample.labCode(), sample));
        return latest.values().stream()
                .filter(sample -> sample.correct() && sample.delayDays() >= DURABLE_FROM_DAYS)
                .sorted(Comparator.comparing(Sample::answeredAt).reversed())
                .map(sample -> new RetainedLab(sample.labCode(), sample.delayDays()))
                .toList();
    }

    /** Questions réussies au quiz initial mais dont le dernier rappel n'est plus juste, les plus récentes d'abord. */
    private List<Forgotten> forgotten(List<Sample> samples) {
        Map<String, Sample> latest = new LinkedHashMap<>();
        samples.stream().sorted(Comparator.comparing(Sample::answeredAt))
                .forEach(sample -> latest.put(sample.labCode() + "/" + sample.questionId(), sample));
        return latest.values().stream()
                .filter(sample -> sample.initial() == QuizGrader.Outcome.CORRECT && !sample.correct())
                .sorted(Comparator.comparing(Sample::answeredAt).reversed())
                .limit(MAX_LISTED)
                .map(sample -> new Forgotten(sample.labCode(), sample.questionId(),
                        prompts.prompt(sample.labCode(), sample.questionId()), sample.outcome().name(),
                        sample.delayDays(), sample.answeredAt()))
                .toList();
    }

    private static int percent(long part, long whole) {
        return whole == 0 ? 0 : (int) Math.round(part * 100.0 / whole);
    }

    private record Sample(String labCode, String questionId, Instant answeredAt, int delayDays,
                          QuizGrader.Outcome outcome, QuizGrader.Outcome initial) {
        boolean correct() {
            return outcome == QuizGrader.Outcome.CORRECT;
        }
    }

    private record Verdict(String code, String message) {
    }

    public record Retention(
            int totalRecalls,
            List<Bucket> buckets,
            String verdict,
            String message,
            List<RetainedLab> retainedLabs,
            List<Forgotten> forgotten
    ) {
    }

    /** `initialPercent` : part de ces mêmes questions réussies à la première réponse du quiz initial. */
    public record Bucket(String label, int fromDays, Integer toDays, int total, int correct, int partial, int wrong,
                         int correctPercent, int initialSamples, int initialPercent) {
    }

    public record RetainedLab(String labCode, int delayDays) {
    }

    public record Forgotten(String labCode, String questionId, String prompt, String outcome, int delayDays, Instant answeredAt) {
    }
}
