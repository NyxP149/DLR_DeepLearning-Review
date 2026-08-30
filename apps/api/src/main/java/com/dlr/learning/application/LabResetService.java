package com.dlr.learning.application;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.shared.web.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LabResetService {

    private final LabCatalog catalog;
    private final JdbcTemplate jdbcTemplate;

    public LabResetService(LabCatalog catalog, JdbcTemplate jdbcTemplate) {
        this.catalog = catalog;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public ResetResult reset(String labCode) {
        String normalized = catalog.findByCode(labCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "LAB_NOT_FOUND", "Laboratoire introuvable : " + labCode))
                .code();

        int reviews = jdbcTemplate.update("delete from review_item where lab_id = ?", normalized);
        int executions = jdbcTemplate.update(
                """
                delete from execution_result
                where submission_id in (
                    select submission.id from submission
                    join attempt on attempt.id = submission.attempt_id
                    where attempt.lab_id = ?
                )
                """, normalized);
        int submissions = jdbcTemplate.update(
                "delete from submission where attempt_id in (select id from attempt where lab_id = ?)", normalized);
        jdbcTemplate.update(
                "delete from quiz_answer where attempt_id in (select id from attempt where lab_id = ?)", normalized);
        jdbcTemplate.update(
                "delete from attempt_checklist where attempt_id in (select id from attempt where lab_id = ?)", normalized);
        int attempts = jdbcTemplate.update("delete from attempt where lab_id = ?", normalized);
        jdbcTemplate.update("delete from adaptation_recommendation where lab_code = ?", normalized);

        return new ResetResult(normalized, attempts, submissions, executions, reviews);
    }

    public record ResetResult(
            String labCode,
            int attemptsDeleted,
            int submissionsDeleted,
            int executionsDeleted,
            int reviewsDeleted
    ) {}
}
