package com.dlr.execution.application;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.catalog.domain.LabContent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Exercices de transfert : même notion que le laboratoire, contexte nouveau, sans indice et hors score.
 * Un transfert est réussi dès qu'une exécution de cet exercice, dans n'importe quelle tentative du laboratoire,
 * produit la sortie attendue.
 */
@Service
public class TransferService {

    private final JdbcTemplate jdbcTemplate;
    private final LabCatalog catalog;

    public TransferService(JdbcTemplate jdbcTemplate, LabCatalog catalog) {
        this.jdbcTemplate = jdbcTemplate;
        this.catalog = catalog;
    }

    public boolean passed(String labCode, String exerciseCode) {
        Integer count = jdbcTemplate.queryForObject(
                """
                select count(*) from execution_result er
                join submission s on s.id = er.submission_id
                join attempt a on a.id = s.attempt_id
                where a.lab_id = ? and s.exercise_code = ? and er.status = 'SUCCESS'
                """, Integer.class, labCode, exerciseCode);
        return count != null && count > 0;
    }

    /** Transferts réussis parmi ceux des laboratoires déjà terminés. */
    public Progress progress() {
        List<String> completedLabs = jdbcTemplate.queryForList(
                "select distinct lab_id from attempt where status in ('COMPLETED', 'COMPLETED_BELOW_THRESHOLD')", String.class);
        int available = 0;
        int passed = 0;
        for (String labCode : completedLabs) {
            for (LabContent.Exercise exercise : catalog.findByCode(labCode).map(LabContent::transferExercises).orElse(List.of())) {
                available++;
                if (passed(labCode, exercise.code())) passed++;
            }
        }
        return new Progress(available, passed);
    }

    public record Progress(int available, int passed) {
        public int percent() {
            return available == 0 ? 0 : Math.round(passed * 100f / available);
        }
    }
}
