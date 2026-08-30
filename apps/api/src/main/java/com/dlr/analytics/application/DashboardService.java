package com.dlr.analytics.application;

import com.dlr.analytics.infrastructure.DashboardQueries;
import com.dlr.analytics.domain.StreakCalculator;
import com.dlr.catalog.application.LabCatalog;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;

@Service
public class DashboardService {

    private final DashboardQueries queries;
    private final LabCatalog catalog;
    private final StreakCalculator streakCalculator;

    public DashboardService(DashboardQueries queries, LabCatalog catalog, StreakCalculator streakCalculator) {
        this.queries = queries;
        this.catalog = catalog;
        this.streakCalculator = streakCalculator;
    }

    public Dashboard dashboard() {
        var javaLabs = catalog.findAll().stream()
                .filter(lab -> "JAVA".equals(lab.language()))
                .toList();
        var completedLabCodes = queries.completedLabCodes();
        int totalLabs = javaLabs.size();
        int completed = (int) javaLabs.stream()
                .filter(lab -> completedLabCodes.contains(lab.code()))
                .count();
        int completedAcrossCatalog = queries.completedLabs();
        int completedReviews = queries.completedReviews();
        int completedSessions = queries.completedSessions();
        int studyMinutes = queries.totalStudyMinutes();
        var streak = streakCalculator.calculate(queries.completedSessionDates(), LocalDate.now());
        int xp = completedAcrossCatalog * 100 + completedReviews * 25 + completedSessions * 20;
        int level = xp / 250 + 1;
        String nextLab = javaLabs.stream()
                .filter(lab -> !completedLabCodes.contains(lab.code()))
                .findFirst()
                .map(lab -> lab.code())
                .orElse(null);
        return new Dashboard(
                queries.profile(), totalLabs, completed,
                totalLabs == 0 ? 0 : (completed * 100) / totalLabs,
                queries.averageScore(), queries.inProgressAttempts(), queries.pendingReviews(),
                studyMinutes, streak.current(), streak.best(), xp, level,
                badges(completed, completedReviews, studyMinutes, streak.best()), nextLab, queries.recentAttempts());
    }

    private List<Badge> badges(int completedLabs, int completedReviews, int studyMinutes, int bestStreak) {
        return List.of(
                new Badge("FIRST_LAB", "Premier pas", "Terminer un laboratoire", completedLabs >= 1),
                new Badge("RHYTHM_3", "Rythme durable", "Étudier trois jours consécutifs", bestStreak >= 3),
                new Badge("REVIEWER", "Mémoire active", "Valider cinq révisions", completedReviews >= 5),
                new Badge("FOCUS_10H", "Concentration", "Cumuler dix heures d'étude", studyMinutes >= 600),
                new Badge("JAVA_FOUNDATIONS", "Fondations Java", "Terminer les six laboratoires fondamentaux", completedLabs >= 6),
                new Badge("JAVA_PRO", "Java professionnel", "Terminer les 22 laboratoires, le projet et le défi", completedLabs >= 24));
    }

    public record Dashboard(
            DashboardQueries.Profile profile,
            int totalLabs,
            int completedLabs,
            int progressPercent,
            BigDecimal averageScore,
            int inProgressAttempts,
            int pendingReviews,
            int studyMinutes,
            int currentStreak,
            int bestStreak,
            int xp,
            int level,
            List<Badge> badges,
            String nextLabCode,
            List<DashboardQueries.RecentAttempt> recentAttempts
    ) {
    }

    public record Badge(String code, String label, String description, boolean unlocked) {
    }
}
