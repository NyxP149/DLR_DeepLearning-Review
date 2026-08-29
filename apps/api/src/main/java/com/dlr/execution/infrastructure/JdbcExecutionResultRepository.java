package com.dlr.execution.infrastructure;

import com.dlr.execution.application.ExecutionResultRepository;
import com.dlr.execution.domain.ExecutionResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import com.dlr.execution.domain.ExecutionStatus;

@Repository
public class JdbcExecutionResultRepository implements ExecutionResultRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcExecutionResultRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ExecutionResult save(ExecutionResult result) {
        jdbcTemplate.update(
                """
                insert into execution_result
                    (id, submission_id, status, exit_code, standard_output, error_output, duration_ms, created_at)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                result.id(),
                result.submissionId(),
                result.status().name(),
                result.exitCode(),
                result.standardOutput(),
                result.errorOutput(),
                result.durationMs(),
                Timestamp.from(result.createdAt()));
        return result;
    }

    @Override
    public Optional<ExecutionResult> findLatestByAttemptId(UUID attemptId) {
        return jdbcTemplate.query(
                        """
                        select er.id, er.submission_id, er.status, er.exit_code, er.standard_output,
                               er.error_output, er.duration_ms, er.created_at
                        from execution_result er
                        join submission s on s.id = er.submission_id
                        where s.attempt_id = ?
                        order by er.created_at desc
                        limit 1
                        """,
                        this::mapResult,
                        attemptId)
                .stream()
                .findFirst();
    }

    private ExecutionResult mapResult(ResultSet result, int rowNumber) throws SQLException {
        Integer exitCode = result.getObject("exit_code", Integer.class);
        return new ExecutionResult(
                result.getObject("id", UUID.class),
                result.getObject("submission_id", UUID.class),
                ExecutionStatus.valueOf(result.getString("status")),
                exitCode,
                result.getString("standard_output"),
                result.getString("error_output"),
                result.getLong("duration_ms"),
                result.getTimestamp("created_at").toInstant());
    }
}
