package com.dlr.notes.application;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.catalog.domain.LabContent;
import com.dlr.shared.web.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class PersonalNoteService {

    private final JdbcTemplate jdbcTemplate;
    private final LabCatalog labCatalog;

    public PersonalNoteService(JdbcTemplate jdbcTemplate, LabCatalog labCatalog) {
        this.jdbcTemplate = jdbcTemplate;
        this.labCatalog = labCatalog;
    }

    public Note note(String labCode) {
        LabContent lab = requireLab(labCode);
        return jdbcTemplate.query(
                        """
                        select n.content, n.updated_at,
                               exists(select 1 from attempt a where a.lab_id = n.lab_id
                                      and a.status in ('COMPLETED', 'COMPLETED_BELOW_THRESHOLD')) as completed
                        from lab_note n
                        where n.profile_id = (select id from user_profile order by id limit 1)
                          and n.lab_id = ?
                        """,
                        (result, row) -> new Note(lab.code(), lab.language(), lab.title(), result.getString("content"),
                                result.getTimestamp("updated_at").toInstant(), result.getBoolean("completed")),
                        lab.code())
                .stream()
                .findFirst()
                .orElse(new Note(lab.code(), lab.language(), lab.title(), "", null, completed(lab.code())));
    }

    public List<Note> notes() {
        return jdbcTemplate.query(
                """
                select n.lab_id, n.content, n.updated_at,
                       exists(select 1 from attempt a where a.lab_id = n.lab_id
                              and a.status in ('COMPLETED', 'COMPLETED_BELOW_THRESHOLD')) as completed
                from lab_note n
                where n.profile_id = (select id from user_profile order by id limit 1)
                  and length(trim(n.content)) > 0
                order by n.updated_at desc
                """,
                (result, row) -> {
                    LabContent lab = requireLab(result.getString("lab_id"));
                    return new Note(lab.code(), lab.language(), lab.title(), result.getString("content"),
                            result.getTimestamp("updated_at").toInstant(), result.getBoolean("completed"));
                });
    }

    public List<ReflectionAnalysis> analyses(String labCode) {
        LabContent lab = requireLab(labCode);
        return jdbcTemplate.query(
                """
                select question_id, content, updated_at from reflection_analysis
                where profile_id = (select id from user_profile order by id limit 1) and lab_id = ?
                order by updated_at
                """,
                (result, row) -> new ReflectionAnalysis(
                        lab.code(), result.getString("question_id"), result.getString("content"),
                        result.getTimestamp("updated_at").toInstant()),
                lab.code());
    }

    @Transactional
    public ReflectionAnalysis saveAnalysis(String labCode, String questionId, String content) {
        LabContent lab = requireLab(labCode);
        requireReflectionQuestion(lab, questionId);
        String normalized = content.strip();
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                delete from reflection_analysis
                where profile_id = (select id from user_profile order by id limit 1)
                  and lab_id = ? and question_id = ?
                """,
                lab.code(), questionId);
        jdbcTemplate.update(
                """
                insert into reflection_analysis (profile_id, lab_id, question_id, content, updated_at)
                select id, ?, ?, ?, ? from user_profile order by id limit 1
                """,
                lab.code(), questionId, normalized, Timestamp.from(now));
        return new ReflectionAnalysis(lab.code(), questionId, normalized, now);
    }

    @Transactional
    public void deleteAnalysis(String labCode, String questionId) {
        LabContent lab = requireLab(labCode);
        jdbcTemplate.update(
                """
                delete from reflection_analysis
                where profile_id = (select id from user_profile order by id limit 1)
                  and lab_id = ? and question_id = ?
                """,
                lab.code(), questionId);
    }

    @Transactional
    public Note save(String labCode, String content) {
        LabContent lab = requireLab(labCode);
        String normalized = content == null ? "" : content.strip();
        jdbcTemplate.update(
                "delete from lab_note where profile_id = (select id from user_profile order by id limit 1) and lab_id = ?",
                lab.code());
        if (!normalized.isEmpty()) {
            jdbcTemplate.update(
                    """
                    insert into lab_note (profile_id, lab_id, content, updated_at)
                    select id, ?, ?, ? from user_profile order by id limit 1
                    """,
                    lab.code(), normalized, Timestamp.from(Instant.now()));
        }
        return note(lab.code());
    }

    private boolean completed(String labCode) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                """
                select exists(select 1 from attempt where lab_id = ?
                              and status in ('COMPLETED', 'COMPLETED_BELOW_THRESHOLD'))
                """,
                Boolean.class,
                labCode));
    }

    private LabContent requireLab(String labCode) {
        return labCatalog.findByCode(labCode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "LAB_NOT_FOUND", "Laboratoire introuvable : " + labCode));
    }

    private void requireReflectionQuestion(LabContent lab, String questionId) {
        boolean exists = lab.quiz().stream()
                .anyMatch(question -> question.code().equals(questionId) && "FREE_TEXT".equals(question.type()));
        if (!exists) {
            throw new ResourceNotFoundException(
                    "REFLECTION_QUESTION_NOT_FOUND", "Question de réflexion introuvable : " + questionId);
        }
    }

    public record Note(
            String labCode,
            String language,
            String labTitle,
            String content,
            Instant updatedAt,
            boolean completed
    ) {
    }

    public record ReflectionAnalysis(String labCode, String questionId, String content, Instant updatedAt) {
    }
}
