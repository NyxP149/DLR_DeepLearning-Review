package com.dlr.mastery.application;

import com.dlr.shared.web.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private static final int[] NEXT_INTERVAL_DAYS = {3, 7, 14, 30};

    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    @Autowired
    public ReviewService(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, Clock.systemUTC());
    }

    ReviewService(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    public List<ReviewItem> pending() {
        return jdbcTemplate.query(
                """
                select id, attempt_id, lab_id, due_at, reason, status, repetition_stage, created_at, completed_at
                from review_item where status = 'PENDING' order by due_at
                """,
                (result, row) -> new ReviewItem(
                        result.getObject("id", UUID.class), result.getObject("attempt_id", UUID.class),
                        result.getString("lab_id"), result.getTimestamp("due_at").toInstant(),
                        result.getString("reason"), result.getString("status"), result.getInt("repetition_stage"),
                        result.getTimestamp("created_at").toInstant(), null));
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
        Instant now = Instant.now(clock);
        jdbcTemplate.update(
                "update review_item set status = 'COMPLETED', completed_at = ? where id = ?",
                Timestamp.from(now), id);

        ReviewItem next = null;
        int nextStage = successful ? current.stage() + 1 : current.stage();
        if (!successful || nextStage <= NEXT_INTERVAL_DAYS.length) {
            int days = successful ? NEXT_INTERVAL_DAYS[nextStage - 1] : 1;
            next = new ReviewItem(
                    UUID.randomUUID(), current.attemptId(), current.labCode(), now.plus(days, ChronoUnit.DAYS),
                    successful ? "Consolider la maîtrise par répétition espacée." : "Revoir rapidement après une difficulté.",
                    "PENDING", nextStage, now, null);
            jdbcTemplate.update(
                    """
                    insert into review_item
                        (id, attempt_id, lab_id, due_at, reason, status, repetition_stage, created_at)
                    values (?, ?, ?, ?, ?, 'PENDING', ?, ?)
                    """,
                    next.id(), next.attemptId(), next.labCode(), Timestamp.from(next.dueAt()), next.reason(),
                    next.stage(), Timestamp.from(now));
        }
        return new ReviewCompletion(id, successful, next);
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
            Instant completedAt
    ) {
    }

    public record ReviewCompletion(UUID completedReviewId, boolean successful, ReviewItem nextReview) {
    }
}
