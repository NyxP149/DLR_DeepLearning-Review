package com.dlr.assessment.infrastructure;

import com.dlr.assessment.application.AssessmentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcAssessmentRepository implements AssessmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAssessmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public QuizAnswer saveAnswer(QuizAnswer answer) {
        jdbcTemplate.update(
                "delete from quiz_answer where attempt_id = ? and question_id = ?",
                answer.attemptId(), answer.questionId());
        jdbcTemplate.update(
                """
                insert into quiz_answer
                    (id, attempt_id, question_id, selected_choice, answer_text, score, feedback, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                answer.id(), answer.attemptId(), answer.questionId(), answer.selectedChoice(), answer.answerText(),
                answer.score(), answer.feedback(), Timestamp.from(answer.updatedAt()));
        return answer;
    }

    @Override
    public List<QuizAnswer> findAnswers(UUID attemptId) {
        return jdbcTemplate.query(
                """
                select id, attempt_id, question_id, selected_choice, answer_text, score, feedback, updated_at
                from quiz_answer where attempt_id = ?
                """,
                this::mapAnswer,
                attemptId);
    }

    @Override
    public void deleteAnswer(UUID attemptId, String questionId) {
        jdbcTemplate.update("delete from quiz_answer where attempt_id = ? and question_id = ?", attemptId, questionId);
    }

    @Override
    public Checklist saveChecklist(Checklist checklist) {
        jdbcTemplate.update("delete from attempt_checklist where attempt_id = ?", checklist.attemptId());
        jdbcTemplate.update(
                """
                insert into attempt_checklist (attempt_id, completed_items, total_items, completed_state, updated_at)
                values (?, ?, ?, ?, ?)
                """,
                checklist.attemptId(), checklist.completedItems(), checklist.totalItems(),
                encode(checklist.completed()),
                Timestamp.from(checklist.updatedAt()));
        return checklist;
    }

    @Override
    public Optional<Checklist> findChecklist(UUID attemptId) {
        return jdbcTemplate.query(
                        """
                        select attempt_id, completed_items, total_items, completed_state, updated_at
                        from attempt_checklist where attempt_id = ?
                        """,
                        (result, row) -> new Checklist(
                                result.getObject("attempt_id", UUID.class),
                                result.getInt("completed_items"),
                                result.getInt("total_items"),
                                decode(result.getString("completed_state"), result.getInt("total_items")),
                                result.getTimestamp("updated_at").toInstant()),
                        attemptId)
                .stream()
                .findFirst();
    }

    private String encode(List<Boolean> completed) {
        return completed.stream().map(value -> Boolean.TRUE.equals(value) ? "1" : "0")
                .collect(java.util.stream.Collectors.joining(","));
    }

    private List<Boolean> decode(String state, int total) {
        if (state == null || state.isBlank()) {
            return java.util.Collections.nCopies(total, false);
        }
        return java.util.Arrays.stream(state.split(",")).map("1"::equals).toList();
    }

    @Override
    public void createReview(UUID attemptId, String labCode, Instant dueAt, String reason, Instant createdAt) {
        jdbcTemplate.update(
                """
                insert into review_item (id, attempt_id, lab_id, due_at, reason, status, created_at)
                values (?, ?, ?, ?, ?, 'PENDING', ?)
                """,
                UUID.randomUUID(), attemptId, labCode, Timestamp.from(dueAt), reason, Timestamp.from(createdAt));
    }

    private QuizAnswer mapAnswer(ResultSet result, int rowNumber) throws SQLException {
        return new QuizAnswer(
                result.getObject("id", UUID.class),
                result.getObject("attempt_id", UUID.class),
                result.getString("question_id"),
                result.getObject("selected_choice", Integer.class),
                result.getString("answer_text"),
                result.getBigDecimal("score"),
                result.getString("feedback"),
                result.getTimestamp("updated_at").toInstant());
    }
}
