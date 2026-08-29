package com.dlr.execution.infrastructure;

import com.dlr.execution.application.ExecutionResultRepository;
import com.dlr.execution.domain.ExecutionResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

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
}

