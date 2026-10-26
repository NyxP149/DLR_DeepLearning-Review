package com.dlr.adaptation.application;

import com.dlr.assessment.domain.QuizGrader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Calibration : compare la confiance déclarée avant la correction au résultat réel.
 * Seule la première réponse d'une question compte pour le quiz (comme pour le score V2) ;
 * les réponses de révision sont toutes prises en compte.
 */
@Service
public class CalibrationService {

    static final int MIN_SAMPLES = 10;
    static final int MIN_LEVEL_SAMPLES = 5;
    static final int SOLID_ACCURACY = 70;
    static final int MAX_BLIND_SPOTS = 8;
    static final int MAX_PERIODS = 8;
    static final int MIN_PERIOD_SAMPLES = 5;
    static final int TREND_THRESHOLD = 5;
    /** Confiance déclarée convertie en probabilité de réussite annoncée : pas sûr·e, plutôt sûr·e, sûr·e. */
    private static final int[] CONFIDENCE_PERCENT = {0, 33, 67, 100};
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM");

    private final JdbcTemplate jdbcTemplate;
    private final QuestionPrompts prompts;

    public CalibrationService(JdbcTemplate jdbcTemplate, QuestionPrompts prompts) {
        this.jdbcTemplate = jdbcTemplate;
        this.prompts = prompts;
    }

    public Calibration calibration() {
        List<Sample> samples = new ArrayList<>(quizSamples());
        samples.addAll(reviewSamples());

        List<Level> levels = new ArrayList<>();
        for (int confidence = 1; confidence <= 3; confidence++) {
            levels.add(level(confidence, samples));
        }
        Source quiz = source("QUIZ", samples);
        Source review = source("REVIEW", samples);

        Level low = levels.get(0);
        Level high = levels.get(2);
        Verdict verdict = verdict(samples.size(), low, high);
        return new Calibration(samples.size(), levels, List.of(quiz, review), verdict.code(), verdict.message(),
                overconfidenceRate(high), underconfidenceRate(low), blindSpots(samples), trend(samples));
    }

    /**
     * Évolution semaine par semaine : écart entre la confiance moyenne déclarée et la réussite réelle.
     * Seules les semaines avec assez de réponses comptent pour la tendance.
     */
    private Trend trend(List<Sample> samples) {
        Map<LocalDate, List<Sample>> byWeek = new TreeMap<>();
        for (Sample sample : samples) {
            LocalDate week = sample.at().atZone(ZoneOffset.UTC).toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            byWeek.computeIfAbsent(week, key -> new ArrayList<>()).add(sample);
        }
        List<Period> all = byWeek.entrySet().stream().map(entry -> period(entry.getKey(), entry.getValue())).toList();
        List<Period> periods = all.subList(Math.max(0, all.size() - MAX_PERIODS), all.size());
        List<Period> reliable = periods.stream().filter(Period::reliable).toList();

        if (reliable.size() < 2) {
            return new Trend("INSUFFICIENT_DATA", "Il faut au moins deux semaines avec " + MIN_PERIOD_SAMPLES
                    + " réponses ou plus (avec confiance) pour suivre l'évolution de ta calibration.", periods);
        }
        Period first = reliable.getFirst();
        Period last = reliable.getLast();
        int from = Math.abs(first.gapPoints());
        int to = Math.abs(last.gapPoints());
        String span = " (semaine du " + DAY.format(first.weekStart()) + " à celle du " + DAY.format(last.weekStart()) + ")";
        if (to - from <= -TREND_THRESHOLD) {
            return new Trend("IMPROVING", "Ta confiance colle de mieux en mieux à tes résultats : l'écart est passé de "
                    + from + " à " + to + " points" + span + ".", periods);
        }
        if (to - from >= TREND_THRESHOLD) {
            return new Trend("WORSENING", "L'écart entre ta confiance et tes résultats se creuse : de " + from + " à "
                    + to + " points" + span + ". Reprends les angles morts avant d'avancer.", periods);
        }
        return new Trend("STABLE", "L'écart entre ta confiance et tes résultats reste stable (" + from + " puis " + to
                + " points)" + span + ".", periods);
    }

    private Period period(LocalDate weekStart, List<Sample> samples) {
        long correct = samples.stream().filter(sample -> sample.outcome() == QuizGrader.Outcome.CORRECT).count();
        int confidence = (int) Math.round(samples.stream().mapToInt(sample -> CONFIDENCE_PERCENT[sample.confidence()]).average().orElse(0));
        int accuracy = percent(correct, samples.size());
        long sure = samples.stream().filter(sample -> sample.confidence() == 3).count();
        long sureNotCorrect = samples.stream()
                .filter(sample -> sample.confidence() == 3 && sample.outcome() != QuizGrader.Outcome.CORRECT).count();
        return new Period(weekStart, samples.size(), confidence, accuracy, confidence - accuracy, (int) sure,
                percent(sureNotCorrect, sure), samples.size() >= MIN_PERIOD_SAMPLES);
    }

    private Verdict verdict(int total, Level low, Level high) {
        if (total < MIN_SAMPLES) {
            return new Verdict("INSUFFICIENT_DATA", "Pas encore assez de réponses avec confiance (" + total + " sur "
                    + MIN_SAMPLES + ") pour juger ta calibration. Continue à déclarer ta confiance avant chaque correction.");
        }
        if (high.total() >= MIN_LEVEL_SAMPLES && high.correctPercent() < SOLID_ACCURACY) {
            return new Verdict("OVERCONFIDENT", "Tu te dis sûr·e mais tu réussis seulement " + high.correctPercent()
                    + " % de ces réponses : relis les angles morts ci-dessous avant de passer à la suite.");
        }
        if (low.total() >= MIN_LEVEL_SAMPLES && low.correctPercent() >= SOLID_ACCURACY) {
            return new Verdict("UNDERCONFIDENT", "Quand tu doutes, tu as pourtant raison dans " + low.correctPercent()
                    + " % des cas : tu en sais plus que tu ne le crois, fais-toi davantage confiance.");
        }
        return new Verdict("CALIBRATED", "Ta confiance suit globalement tes résultats : continue à la déclarer honnêtement.");
    }

    private List<Sample> quizSamples() {
        // Toutes les réponses, dans l'ordre : la première de chaque (tentative, question) fait foi.
        Set<String> seen = new HashSet<>();
        List<Sample> samples = new ArrayList<>();
        jdbcTemplate.query(
                """
                select l.attempt_id, l.question_id, l.score, l.confidence, l.answered_at, a.lab_id
                from quiz_answer_log l join attempt a on a.id = l.attempt_id
                order by l.answered_at, l.id
                """,
                result -> {
                    String key = result.getString("attempt_id") + "/" + result.getString("question_id");
                    if (!seen.add(key)) return;
                    int confidence = result.getInt("confidence");
                    if (result.wasNull()) return;
                    samples.add(new Sample("QUIZ", result.getString("lab_id"), result.getString("question_id"),
                            confidence, QuizGrader.outcome(result.getBigDecimal("score")),
                            result.getTimestamp("answered_at").toInstant()));
                });
        return samples;
    }

    private List<Sample> reviewSamples() {
        List<Sample> samples = new ArrayList<>();
        jdbcTemplate.query(
                """
                select r.question_id, r.score, r.confidence, r.answered_at, i.lab_id
                from review_answer r join review_item i on i.id = r.review_id
                where r.confidence is not null
                order by r.answered_at, r.id
                """,
                result -> {
                    samples.add(new Sample("REVIEW", result.getString("lab_id"), result.getString("question_id"),
                            result.getInt("confidence"), QuizGrader.outcome(result.getBigDecimal("score")),
                            result.getTimestamp("answered_at").toInstant()));
                });
        return samples;
    }

    private Level level(int confidence, List<Sample> samples) {
        int correct = 0;
        int partial = 0;
        int wrong = 0;
        for (Sample sample : samples) {
            if (sample.confidence() != confidence) continue;
            switch (sample.outcome()) {
                case CORRECT -> correct++;
                case PARTIAL -> partial++;
                case WRONG -> wrong++;
            }
        }
        int total = correct + partial + wrong;
        return new Level(confidence, total, correct, partial, wrong, percent(correct, total));
    }

    private Source source(String code, List<Sample> samples) {
        List<Sample> own = samples.stream().filter(sample -> sample.source().equals(code)).toList();
        long correct = own.stream().filter(sample -> sample.outcome() == QuizGrader.Outcome.CORRECT).count();
        return new Source(code, own.size(), percent(correct, own.size()));
    }

    /** Part des réponses « sûr·e » qui n'étaient pas justes. */
    private int overconfidenceRate(Level high) {
        return percent(high.partial() + high.wrong(), high.total());
    }

    /** Part des réponses « pas sûr·e » qui étaient pourtant justes. */
    private int underconfidenceRate(Level low) {
        return percent(low.correct(), low.total());
    }

    /** Les questions où la confiance était haute et la réponse fausse ou partielle, les plus récentes d'abord. */
    private List<BlindSpot> blindSpots(List<Sample> samples) {
        Map<String, Sample> latest = new LinkedHashMap<>();
        samples.stream()
                .filter(sample -> sample.confidence() == 3 && sample.outcome() != QuizGrader.Outcome.CORRECT)
                .sorted(Comparator.comparing(Sample::at).reversed())
                .forEach(sample -> latest.putIfAbsent(sample.labCode() + "/" + sample.questionId(), sample));
        return latest.values().stream().limit(MAX_BLIND_SPOTS).map(this::blindSpot).toList();
    }

    private BlindSpot blindSpot(Sample sample) {
        return new BlindSpot(sample.labCode(), sample.questionId(), prompts.prompt(sample.labCode(), sample.questionId()),
                sample.outcome().name(), sample.source(), sample.at());
    }

    private static int percent(long part, long whole) {
        return whole == 0 ? 0 : (int) Math.round(part * 100.0 / whole);
    }

    private record Sample(String source, String labCode, String questionId, int confidence,
                          QuizGrader.Outcome outcome, Instant at) {
    }

    private record Verdict(String code, String message) {
    }

    public record Calibration(
            int totalAnswers,
            List<Level> levels,
            List<Source> sources,
            String verdict,
            String message,
            int overconfidencePercent,
            int underconfidencePercent,
            List<BlindSpot> blindSpots,
            Trend trend
    ) {
    }

    /** gapPoints > 0 : confiance supérieure à la réussite (sur-confiance), < 0 : sous-confiance. */
    public record Period(LocalDate weekStart, int total, int confidencePercent, int accuracyPercent, int gapPoints,
                         int sureTotal, int overconfidencePercent, boolean reliable) {
    }

    public record Trend(String verdict, String message, List<Period> periods) {
    }

    public record Level(int confidence, int total, int correct, int partial, int wrong, int correctPercent) {
    }

    public record Source(String code, int total, int correctPercent) {
    }

    public record BlindSpot(String labCode, String questionId, String prompt, String outcome, String source, Instant answeredAt) {
    }
}
