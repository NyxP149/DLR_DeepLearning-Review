package com.dlr.catalog.web;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.catalog.domain.LabContent;
import com.dlr.catalog.domain.PathDescriptor;
import com.dlr.shared.web.ResourceNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LabController {

    private final LabCatalog labCatalog;

    public LabController(LabCatalog labCatalog) {
        this.labCatalog = labCatalog;
    }

    @GetMapping("/labs")
    public List<LabSummaryResponse> listLabs() {
        return labCatalog.findAll().stream().map(LabSummaryResponse::from).toList();
    }

    @GetMapping("/paths/catalog")
    public List<PathDescriptor> listPaths() { return labCatalog.findPaths(); }

    @GetMapping("/labs/{code}")
    public LabDetailResponse getLab(@PathVariable String code) {
        LabContent lab = labCatalog.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("LAB_NOT_FOUND", "Laboratoire introuvable : " + code));
        return LabDetailResponse.from(lab);
    }

    public record LabSummaryResponse(
            String code,
            String language,
            int number,
            String title,
            String difficulty,
            int threshold
    ) {
        static LabSummaryResponse from(LabContent lab) {
            return new LabSummaryResponse(
                    lab.code(), lab.language(), lab.number(), lab.title(), lab.difficulty(), lab.threshold());
        }
    }

    public record LabDetailResponse(
            String code,
            String language,
            int number,
            String slug,
            String title,
            String difficulty,
            int threshold,
            List<String> objectives,
            List<LabContent.LessonSection> sections,
            List<LabContent.KeyConcept> keyConcepts,
            List<ExerciseResponse> exercises,
            List<QuizQuestionResponse> quiz,
            List<String> checklist
    ) {
        static LabDetailResponse from(LabContent lab) {
            return new LabDetailResponse(
                    lab.code(),
                    lab.language(),
                    lab.number(),
                    lab.slug(),
                    lab.title(),
                    lab.difficulty(),
                    lab.threshold(),
                    lab.objectives(),
                    lab.sections(),
                    lab.keyConcepts(),
                    lab.exercises().stream().map(ExerciseResponse::from).toList(),
                    lab.quiz().stream().map(QuizQuestionResponse::from).toList(),
                    lab.checklist());
        }
    }

    public record ExerciseResponse(String code, String title, String statement, String starterCode) {
        static ExerciseResponse from(LabContent.Exercise exercise) {
            return new ExerciseResponse(exercise.code(), exercise.title(), exercise.statement(), exercise.starterCode());
        }
    }

    public record QuizQuestionResponse(
            String code,
            String type,
            String prompt,
            List<String> choices
    ) {
        static QuizQuestionResponse from(LabContent.QuizQuestion question) {
            return new QuizQuestionResponse(
                    question.code(), question.type(), question.prompt(), question.choices());
        }
    }
}
