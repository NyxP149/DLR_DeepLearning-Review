package com.dlr.analytics.infrastructure;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class DashboardQueries {

    private final JdbcTemplate jdbcTemplate;

    public DashboardQueries(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Profile profile() {
        return jdbcTemplate.query(
                        "select display_name, target_months, weekday_minutes, weekend_minutes from user_profile order by id limit 1",
                        (result, row) -> new Profile(
                                result.getString("display_name"), result.getInt("target_months"),
                                result.getInt("weekday_minutes"), result.getInt("weekend_minutes")))
                .stream().findFirst().orElse(new Profile("Apprenant DLR", 4, 90, 60));
    }

    public int completedLabs() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(distinct lab_id) from attempt where status in ('COMPLETED', 'COMPLETED_BELOW_THRESHOLD')",
                Integer.class);
        return count == null ? 0 : count;
    }

    public int inProgressAttempts() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from attempt where status = 'IN_PROGRESS'", Integer.class);
        return count == null ? 0 : count;
    }

    public int pendingReviews() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from review_item where status = 'PENDING'", Integer.class);
        return count == null ? 0 : count;
    }

    public BigDecimal averageScore() {
        BigDecimal average = jdbcTemplate.queryForObject(
                "select avg(score) from attempt where score is not null", BigDecimal.class);
        return average == null ? BigDecimal.ZERO : average.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public List<RecentAttempt> recentAttempts() {
        return jdbcTemplate.query(
                """
                select lab_id, status, score, started_at, completed_at
                from attempt
                order by started_at desc
                limit 5
                """,
                (result, row) -> {
                    Timestamp completed = result.getTimestamp("completed_at");
                    return new RecentAttempt(
                            result.getString("lab_id"), result.getString("status"), result.getBigDecimal("score"),
                            result.getTimestamp("started_at").toInstant(),
                            completed == null ? null : completed.toInstant());
                });
    }

    public record Profile(String displayName, int targetMonths, int weekdayMinutes, int weekendMinutes) {
    }

    public record RecentAttempt(String labCode, String status, BigDecimal score, Instant startedAt, Instant completedAt) {
    }
}
