package com.dlr.mastery.application;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.catalog.domain.LabContent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConceptMasteryService {

    private final LabCatalog catalog;
    private final JdbcTemplate jdbcTemplate;

    public ConceptMasteryService(LabCatalog catalog, JdbcTemplate jdbcTemplate) {
        this.catalog = catalog;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ConceptMastery> list() {
        Map<String, LabProgress> progressByLab = loadProgress().stream()
                .collect(Collectors.toMap(LabProgress::labCode, progress -> progress));

        return catalog.findAll().stream()
                .flatMap(lab -> lab.keyConcepts().stream().map(concept -> toMastery(lab, concept, progressByLab.get(lab.code()))))
                .toList();
    }

    private ConceptMastery toMastery(LabContent lab, LabContent.KeyConcept concept, LabProgress progress) {
        BigDecimal score = progress == null ? null : progress.score();
        int completedReviewStage = progress == null ? -1 : progress.completedReviewStage();
        MasteryStatus status;
        if (score == null) {
            status = MasteryStatus.NOT_STARTED;
        } else if (score.compareTo(BigDecimal.valueOf(lab.threshold())) < 0) {
            status = MasteryStatus.TO_REVIEW;
        } else if (completedReviewStage >= 4) {
            status = MasteryStatus.MASTERED;
        } else {
            status = MasteryStatus.CONSOLIDATING;
        }
        return new ConceptMastery(
                concept.code(), concept.name(), lab.code(), lab.number(), lab.title(), concept.definition(),
                concept.whyExists(), concept.whyImportant(), concept.minimalExample(), concept.commonMistake(),
                concept.masteryQuestion(), concept.masteryProof(), status, score, completedReviewStage);
    }

    private List<LabProgress> loadProgress() {
        return jdbcTemplate.query(
                """
                select a.lab_id,
                       max(a.score) as score,
                       coalesce(max(case when r.status = 'COMPLETED' then r.repetition_stage else -1 end), -1) as completed_stage
                from attempt a
                left join review_item r on r.attempt_id = a.id
                where a.status in ('COMPLETED', 'COMPLETED_BELOW_THRESHOLD')
                group by a.lab_id
                """,
                (result, row) -> new LabProgress(
                        result.getString("lab_id"), result.getBigDecimal("score"), result.getInt("completed_stage")));
    }

    public enum MasteryStatus {
        NOT_STARTED,
        TO_REVIEW,
        CONSOLIDATING,
        MASTERED
    }

    public record ConceptMastery(
            String code,
            String name,
            String labCode,
            int labNumber,
            String labTitle,
            String definition,
            String whyExists,
            String whyImportant,
            String minimalExample,
            String commonMistake,
            String masteryQuestion,
            String masteryProof,
            MasteryStatus status,
            BigDecimal score,
            int completedReviewStage
    ) {
    }

    private record LabProgress(String labCode, BigDecimal score, int completedReviewStage) {
    }
}
