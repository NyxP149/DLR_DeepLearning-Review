package com.dlr.analytics.application;

import com.dlr.analytics.infrastructure.DashboardQueries;
import com.dlr.catalog.application.LabCatalog;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    private final DashboardQueries queries;
    private final LabCatalog catalog;

    public DashboardService(DashboardQueries queries, LabCatalog catalog) {
        this.queries = queries;
        this.catalog = catalog;
    }

    public Dashboard dashboard() {
        int totalLabs = catalog.findAll().size();
        int completed = Math.min(queries.completedLabs(), totalLabs);
        int xp = completed * 100;
        int level = xp / 500 + 1;
        String nextLab = catalog.findAll().stream()
                .skip(completed)
                .findFirst()
                .map(lab -> lab.code())
                .orElse(null);
        return new Dashboard(
                queries.profile(), totalLabs, completed,
                totalLabs == 0 ? 0 : (completed * 100) / totalLabs,
                queries.averageScore(), queries.inProgressAttempts(), queries.pendingReviews(),
                xp, level, nextLab, queries.recentAttempts());
    }

    public record Dashboard(
            DashboardQueries.Profile profile,
            int totalLabs,
            int completedLabs,
            int progressPercent,
            BigDecimal averageScore,
            int inProgressAttempts,
            int pendingReviews,
            int xp,
            int level,
            String nextLabCode,
            List<DashboardQueries.RecentAttempt> recentAttempts
    ) {
    }
}
