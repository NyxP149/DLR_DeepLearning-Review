package com.dlr.learning.infrastructure;

import com.dlr.learning.application.AttemptRepository;
import com.dlr.learning.domain.Attempt;
import com.dlr.learning.domain.AttemptStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcAttemptRepository implements AttemptRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAttemptRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Attempt save(Attempt attempt) {
        jdbcTemplate.update(
                """
                insert into attempt (id, lab_id, started_at, completed_at, status)
                values (?, ?, ?, ?, ?)
                """,
                attempt.id(),
                attempt.labCode(),
                Timestamp.from(attempt.startedAt()),
                attempt.completedAt() == null ? null : Timestamp.from(attempt.completedAt()),
                attempt.status().name());
        return attempt;
    }

    @Override
    public Optional<Attempt> findById(UUID id) {
        return jdbcTemplate.query(
                        """
                        select id, lab_id, started_at, completed_at, status, score, continued_below_threshold
                        from attempt
                        where id = ?
                        """,
                        this::mapAttempt,
                        id)
                .stream()
                .findFirst();
    }

    @Override
    public Attempt complete(
            UUID id,
            AttemptStatus status,
            BigDecimal score,
            ScoreBreakdown breakdown,
            java.time.Instant completedAt
    ) {
        jdbcTemplate.update(
                """
                update attempt
                set completed_at = ?, status = ?, score = ?, tests_score = ?, quiz_score = ?,
                    practice_score = ?, connections_score = ?, self_assessment_score = ?, score_version = ?
                where id = ?
                """,
                Timestamp.from(completedAt), status.name(), score, breakdown.tests(), breakdown.quiz(),
                breakdown.practice(), breakdown.connections(), breakdown.selfAssessment(), breakdown.version(), id);
        return findById(id).orElseThrow();
    }

    @Override
    public Attempt allowContinuation(UUID id) {
        jdbcTemplate.update("update attempt set continued_below_threshold = true where id = ?", id);
        return findById(id).orElseThrow();
    }

    private Attempt mapAttempt(ResultSet result, int rowNumber) throws SQLException {
        Timestamp completedAt = result.getTimestamp("completed_at");
        return new Attempt(
                result.getObject("id", UUID.class),
                result.getString("lab_id"),
                result.getTimestamp("started_at").toInstant(),
                completedAt == null ? null : completedAt.toInstant(),
                AttemptStatus.valueOf(result.getString("status")),
                result.getBigDecimal("score"),
                result.getBoolean("continued_below_threshold"));
    }
}
