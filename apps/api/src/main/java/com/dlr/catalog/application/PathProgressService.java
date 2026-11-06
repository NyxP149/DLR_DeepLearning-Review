package com.dlr.catalog.application;

import com.dlr.catalog.domain.LabContent;
import com.dlr.shared.web.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PathProgressService {

    private static final double CREDIT_VALIDATED = 1.0;
    private static final double CREDIT_TO_CONSOLIDATE = 0.6;
    private static final double CREDIT_IN_PROGRESS = 0.25;

    private final LabCatalog catalog;
    private final JdbcTemplate jdbcTemplate;

    public PathProgressService(LabCatalog catalog, JdbcTemplate jdbcTemplate) {
        this.catalog = catalog;
        this.jdbcTemplate = jdbcTemplate;
    }

    public PathProgress progress(String pathCode) {
        String normalized = pathCode.toUpperCase(Locale.ROOT);
        catalog.findPaths().stream()
                .filter(path -> path.code().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PATH_NOT_FOUND", "Parcours introuvable : " + pathCode));

        List<LabContent> labs = catalog.findAll().stream()
                .filter(lab -> belongsToPath(lab, normalized))
                .sorted(java.util.Comparator.comparingInt(LabContent::number))
                .toList();
        Map<String, AttemptProgress> attempts = loadAttempts();
        List<LabProgress> labProgress = labs.stream()
                .map(lab -> toProgress(lab, attempts))
                .toList();
        int completed = (int) labProgress.stream().filter(item -> item.state() == LabState.COMPLETED).count();
        String next = labProgress.stream()
                .filter(item -> item.state() == LabState.AVAILABLE || item.state() == LabState.IN_PROGRESS || item.state() == LabState.ACTION_REQUIRED)
                .findFirst().map(LabProgress::code).orElse(null);
        return new PathProgress(normalized, labs.size(), completed,
                labs.isEmpty() ? 0 : completed * 100 / labs.size(), next, labProgress);
    }

    /**
     * Progression générale : tous les laboratoires de tous les parcours comptent, chacun selon son état réel.
     * Validé 100 %, terminé sous le seuil 60 % (à consolider), en cours 25 %, pas commencé 0 %.
     */
    public OverallProgress overall() {
        Map<String, AttemptProgress> attempts = loadAttempts();
        List<LabContent> allLabs = catalog.findAll();
        List<PathShare> paths = new java.util.ArrayList<>();
        int total = 0;
        int validated = 0;
        int toConsolidate = 0;
        int inProgress = 0;
        double credit = 0;
        for (var path : catalog.findPaths()) {
            int pathTotal = 0;
            int pathValidated = 0;
            double pathCredit = 0;
            for (LabContent lab : allLabs) {
                if (!belongsToPath(lab, path.code())) continue;
                AttemptProgress attempt = attempts.getOrDefault(lab.code(), AttemptProgress.EMPTY);
                pathTotal++;
                if (attempt.qualifies()) {
                    pathValidated++;
                    pathCredit += CREDIT_VALIDATED;
                } else if (attempt.actionRequired()) {
                    toConsolidate++;
                    pathCredit += CREDIT_TO_CONSOLIDATE;
                } else if (attempt.inProgress()) {
                    inProgress++;
                    pathCredit += CREDIT_IN_PROGRESS;
                }
            }
            if (pathTotal == 0) continue;
            total += pathTotal;
            validated += pathValidated;
            credit += pathCredit;
            paths.add(new PathShare(path.code(), path.title(), pathTotal, pathValidated, percent(pathCredit, pathTotal)));
        }
        return new OverallProgress(percent(credit, total), total, validated, toConsolidate, inProgress,
                total - validated - toConsolidate - inProgress, paths);
    }

    /** Pourcentage à une décimale. */
    private static double percent(double credit, int total) {
        return total == 0 ? 0 : Math.round(credit * 1000.0 / total) / 10.0;
    }

    public void requireUnlocked(LabContent lab) {
        if (lab.prerequisites().isEmpty()) return;
        Map<String, AttemptProgress> attempts = loadAttempts();
        List<String> missing = lab.prerequisites().stream()
                .filter(code -> !attempts.getOrDefault(code, AttemptProgress.EMPTY).qualifies())
                .toList();
        if (!missing.isEmpty()) throw new LabPrerequisiteException(lab.code(), missing);
    }

    private LabProgress toProgress(LabContent lab, Map<String, AttemptProgress> attempts) {
        AttemptProgress attempt = attempts.getOrDefault(lab.code(), AttemptProgress.EMPTY);
        LabState state;
        if (attempt.qualifies()) state = LabState.COMPLETED;
        else if (attempt.actionRequired()) state = LabState.ACTION_REQUIRED;
        else if (attempt.inProgress()) state = LabState.IN_PROGRESS;
        else if (lab.prerequisites().stream().allMatch(code -> attempts.getOrDefault(code, AttemptProgress.EMPTY).qualifies())) state = LabState.AVAILABLE;
        else state = LabState.LOCKED;
        return new LabProgress(lab.code(), lab.title(), lab.activityType(), lab.prerequisites(), state, attempt.bestScore());
    }

    private Map<String, AttemptProgress> loadAttempts() {
        Map<String, AttemptProgress> progress = new HashMap<>();
        jdbcTemplate.query(
                """
                select lab_id,
                       max(case when status = 'COMPLETED' or (status = 'COMPLETED_BELOW_THRESHOLD' and continued_below_threshold = true) then 1 else 0 end) as qualifies,
                       max(case when status = 'IN_PROGRESS' then 1 else 0 end) as in_progress,
                       max(case when status = 'COMPLETED_BELOW_THRESHOLD' and continued_below_threshold = false then 1 else 0 end) as action_required,
                       max(score) as best_score
                from attempt
                group by lab_id
                """,
                (org.springframework.jdbc.core.RowCallbackHandler) result -> progress.put(result.getString("lab_id"), new AttemptProgress(
                        result.getInt("qualifies") == 1,
                        result.getInt("in_progress") == 1,
                        result.getInt("action_required") == 1,
                        result.getBigDecimal("best_score"))));
        return progress;
    }

    private boolean belongsToPath(LabContent lab, String pathCode) {
        String prefix = "LEARN_LLM".equals(pathCode) ? "LLM-" : pathCode + "-";
        return lab.code().startsWith(prefix);
    }

    public record PathShare(String pathCode, String title, int totalLabs, int validatedLabs, double progressPercent) {}
    public record OverallProgress(double progressPercent, int totalLabs, int validatedLabs, int toConsolidateLabs,
                                  int inProgressLabs, int notStartedLabs, List<PathShare> paths) {}
    public enum LabState { LOCKED, AVAILABLE, IN_PROGRESS, ACTION_REQUIRED, COMPLETED }
    public record LabProgress(String code, String title, String activityType, List<String> prerequisites,
                              LabState state, BigDecimal bestScore) {}
    public record PathProgress(String pathCode, int totalLabs, int completedLabs, int progressPercent,
                               String nextLabCode, List<LabProgress> labs) {}
    private record AttemptProgress(boolean qualifies, boolean inProgress, boolean actionRequired, BigDecimal bestScore) {
        private static final AttemptProgress EMPTY = new AttemptProgress(false, false, false, null);
    }
}
