package com.dlr.execution.infrastructure;

import com.dlr.execution.application.SubmissionRepository;
import com.dlr.execution.domain.Submission;
import com.dlr.execution.domain.SubmissionOrigin;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcSubmissionRepository implements SubmissionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSubmissionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Submission save(Submission submission) {
        jdbcTemplate.update(
                """
                insert into submission (id, attempt_id, language, source_code, origin, created_at)
                values (?, ?, ?, ?, ?, ?)
                """,
                submission.id(),
                submission.attemptId(),
                submission.language(),
                submission.sourceCode(),
                submission.origin().name(),
                Timestamp.from(submission.createdAt()));
        return submission;
    }

    @Override
    public Optional<Submission> findById(UUID id) {
        return jdbcTemplate.query(
                        """
                        select id, attempt_id, language, source_code, origin, created_at
                        from submission
                        where id = ?
                        """,
                        this::mapSubmission,
                        id)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<Submission> findLatestByAttemptId(UUID attemptId) {
        return jdbcTemplate.query(
                        """
                        select id, attempt_id, language, source_code, origin, created_at
                        from submission where attempt_id = ? order by created_at desc limit 1
                        """,
                        this::mapSubmission,
                        attemptId)
                .stream()
                .findFirst();
    }

    private Submission mapSubmission(ResultSet result, int rowNumber) throws SQLException {
        return new Submission(
                result.getObject("id", UUID.class),
                result.getObject("attempt_id", UUID.class),
                result.getString("language"),
                result.getString("source_code"),
                SubmissionOrigin.valueOf(result.getString("origin")),
                result.getTimestamp("created_at").toInstant());
    }
}
