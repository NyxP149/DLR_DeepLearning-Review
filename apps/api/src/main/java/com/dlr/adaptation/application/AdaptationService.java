package com.dlr.adaptation.application;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.mastery.application.ConceptMasteryService;
import com.dlr.mastery.application.ConceptMasteryService.ConceptMastery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdaptationService {

    private final JdbcTemplate jdbcTemplate;
    private final ConceptMasteryService masteryService;
    private final LabCatalog catalog;
    private final Clock clock;

    @Autowired
    public AdaptationService(JdbcTemplate jdbcTemplate, ConceptMasteryService masteryService, LabCatalog catalog) {
        this(jdbcTemplate, masteryService, catalog, Clock.systemUTC());
    }

    AdaptationService(JdbcTemplate jdbcTemplate, ConceptMasteryService masteryService, LabCatalog catalog, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.masteryService = masteryService;
        this.catalog = catalog;
        this.clock = clock;
    }

    @Transactional
    public Recommendation current() {
        Instant now = Instant.now(clock);
        List<Recommendation> active = jdbcTemplate.query(
                """
                select id, reason, targeted_concepts, lab_code, proposed_activity, difficulty,
                       expected_benefit, factors, status, expires_at, created_at
                from adaptation_recommendation
                where status in ('PROPOSED', 'POSTPONED', 'ACCEPTED') and expires_at > ?
                order by created_at desc limit 1
                """, recommendationMapper(), Timestamp.from(now));
        if (!active.isEmpty()) return active.getFirst();

        Set<String> recentlyDeclined = jdbcTemplate.queryForList(
                        """
                        select targeted_concepts from adaptation_recommendation
                        where status in ('IGNORED', 'REPLACED') and decided_at > ?
                        """, String.class, Timestamp.from(now.minus(7, ChronoUnit.DAYS))).stream()
                .flatMap(value -> Arrays.stream(value.split(","))).collect(Collectors.toSet());
        ConceptMastery target = masteryService.list().stream()
                .filter(concept -> !recentlyDeclined.contains(concept.code()))
                .min(Comparator.comparingInt(this::priority).thenComparingInt(ConceptMastery::labNumber))
                .orElseGet(() -> masteryService.list().getFirst());
        Recommendation recommendation = propose(target, now);
        jdbcTemplate.update(
                """
                insert into adaptation_recommendation
                    (id, reason, targeted_concepts, lab_code, proposed_activity, difficulty,
                     expected_benefit, factors, status, expires_at, created_at, decided_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, null)
                """,
                recommendation.id(), recommendation.reason(), String.join(",", recommendation.targetedConcepts()),
                recommendation.labCode(), recommendation.proposedActivity(), recommendation.difficulty(),
                recommendation.expectedBenefit(), String.join(" | ", recommendation.factors()),
                recommendation.status(), Timestamp.from(recommendation.expiresAt()), Timestamp.from(recommendation.createdAt()));
        return recommendation;
    }

    @Transactional
    public Recommendation decide(UUID id, Decision decision) {
        String status = switch (decision) {
            case ACCEPT -> "ACCEPTED";
            case IGNORE -> "IGNORED";
            case POSTPONE -> "POSTPONED";
            case REPLACE -> "REPLACED";
        };
        Instant now = Instant.now(clock);
        int updated = decision == Decision.POSTPONE
                ? jdbcTemplate.update(
                        "update adaptation_recommendation set status = ?, decided_at = ?, expires_at = ? where id = ? and status in ('PROPOSED', 'POSTPONED')",
                        status, Timestamp.from(now), Timestamp.from(now.plus(3, ChronoUnit.DAYS)), id)
                : jdbcTemplate.update(
                        "update adaptation_recommendation set status = ?, decided_at = ? where id = ? and status in ('PROPOSED', 'POSTPONED')",
                        status, Timestamp.from(now), id);
        if (updated == 0) throw new IllegalStateException("Cette recommandation a déjà été traitée.");
        return (decision == Decision.IGNORE || decision == Decision.REPLACE) ? current() : find(id);
    }

    public Insights insights() {
        int attempts = count("select count(*) from attempt");
        int hints = count("select count(*) from ai_interaction where purpose like 'HINT_%'");
        int completedLabs = count("select count(distinct lab_id) from attempt where status in ('COMPLETED', 'COMPLETED_BELOW_THRESHOLD')");
        int completedReviews = count("select count(*) from review_item where status = 'COMPLETED'");
        int completedSessions = count("select count(*) from study_session where status = 'COMPLETED'");
        BigDecimal average = jdbcTemplate.queryForObject(
                "select coalesce(avg(score), 0) from attempt where score is not null", BigDecimal.class);
        int averageScore = average == null ? 0 : average.intValue();
        int hintDependency = Math.min(100, attempts == 0 ? 0 : Math.round((hints * 100f) / attempts));
        int autonomy = clamp((attempts == 0 ? 40 : averageScore) - Math.min(30, hints * 5) + Math.min(15, completedReviews * 2));
        int remaining = Math.max(0, catalog.findAll().size() - completedLabs);
        int pace = Math.max(1, completedSessions / 5);
        int estimatedWeeks = remaining == 0 ? 0 : (int) Math.ceil(remaining / (double) pace);
        int mastered = (int) masteryService.list().stream()
                .filter(concept -> concept.status() == ConceptMasteryService.MasteryStatus.MASTERED).count();
        int transfer = masteryService.list().isEmpty() ? 0 : mastered * 100 / masteryService.list().size();
        return new Insights(autonomy, hintDependency, transfer, estimatedWeeks,
                List.of("score moyen : " + averageScore + " %", "indices demandés : " + hints,
                        "révisions validées : " + completedReviews, "séances terminées : " + completedSessions),
                "Estimation locale fondée sur l'activité enregistrée ; elle ne constitue pas une certitude.");
    }

    private Recommendation propose(ConceptMastery target, Instant now) {
        String difficulty = switch (target.status()) {
            case TO_REVIEW -> "RENFORCEMENT";
            case CONSOLIDATING -> "TRANSFERT";
            case MASTERED -> "CHALLENGE";
            case NOT_STARTED -> "GUIDE";
        };
        String scoreFactor = target.score() == null ? "aucun score disponible" : "meilleur score : " + target.score() + " %";
        String statusFactor = switch (target.status()) {
            case TO_REVIEW -> "statut : à renforcer";
            case CONSOLIDATING -> "statut : en consolidation";
            case MASTERED -> "statut : maîtrisé";
            case NOT_STARTED -> "statut : à découvrir";
        };
        String reviewFactor = target.completedReviewStage() < 0
                ? "aucune révision différée validée"
                : "dernière révision validée : J" + List.of(1, 3, 7, 14, 30)
                        .get(Math.min(target.completedReviewStage(), 4));
        String reason = switch (target.status()) {
            case TO_REVIEW -> "Ce concept est sous le seuil recommandé et mérite un renforcement ciblé.";
            case CONSOLIDATING -> "Le score est suffisant, mais la maîtrise différée n'est pas encore confirmée.";
            case MASTERED -> "Ce concept est solide ; un transfert vers un contexte nouveau entretiendra l'autonomie.";
            case NOT_STARTED -> "C'est le prochain concept obligatoire non encore travaillé dans le parcours fixe.";
        };
        return new Recommendation(UUID.randomUUID(), reason, List.of(target.code()), target.labCode(),
                target.masteryQuestion() + " Preuve attendue : " + target.masteryProof(), difficulty,
                "Renforcer « " + target.name() + " » sans retirer aucune compétence obligatoire.",
                List.of(statusFactor, scoreFactor, reviewFactor),
                "PROPOSED", now.plus(1, ChronoUnit.DAYS), now, true);
    }

    private int priority(ConceptMastery concept) {
        return switch (concept.status()) {
            case TO_REVIEW -> 0;
            case CONSOLIDATING -> 1;
            case NOT_STARTED -> 2;
            case MASTERED -> 3;
        };
    }

    private Recommendation find(UUID id) {
        return jdbcTemplate.query(
                """
                select id, reason, targeted_concepts, lab_code, proposed_activity, difficulty,
                       expected_benefit, factors, status, expires_at, created_at
                from adaptation_recommendation where id = ?
                """, recommendationMapper(), id).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Recommandation introuvable : " + id));
    }

    private org.springframework.jdbc.core.RowMapper<Recommendation> recommendationMapper() {
        return (result, row) -> new Recommendation(
                result.getObject("id", UUID.class), result.getString("reason"),
                List.of(result.getString("targeted_concepts").split(",")), result.getString("lab_code"),
                result.getString("proposed_activity"), result.getString("difficulty"),
                result.getString("expected_benefit"), List.of(result.getString("factors").split(" \\| ")),
                result.getString("status"), result.getTimestamp("expires_at").toInstant(),
                result.getTimestamp("created_at").toInstant(), true);
    }

    private int count(String sql) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class);
        return value == null ? 0 : value;
    }

    private int clamp(int value) { return Math.max(0, Math.min(100, value)); }

    public enum Decision { ACCEPT, IGNORE, POSTPONE, REPLACE }
    public record Recommendation(UUID id, String reason, List<String> targetedConcepts, String labCode,
                                 String proposedActivity, String difficulty, String expectedBenefit,
                                 List<String> factors, String status, Instant expiresAt, Instant createdAt,
                                 boolean requiresConfirmation) {}
    public record Insights(int autonomyScore, int hintDependencyPercent, int transferScore,
                           int estimatedWeeksRemaining, List<String> factors, String disclaimer) {}
}
