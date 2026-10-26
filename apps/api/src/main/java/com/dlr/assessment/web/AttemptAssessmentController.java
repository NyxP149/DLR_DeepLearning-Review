package com.dlr.assessment.web;

import com.dlr.assessment.application.AssessmentRepository;
import com.dlr.assessment.application.AssessmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attempts/{attemptId}")
public class AttemptAssessmentController {

    private final AssessmentService assessmentService;

    public AttemptAssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PutMapping("/quiz/{questionId}")
    public QuizAnswerResponse answer(
            @PathVariable UUID attemptId,
            @PathVariable String questionId,
            @RequestBody QuizAnswerRequest request
    ) {
        return QuizAnswerResponse.from(assessmentService.answer(
                attemptId, questionId, request.selectedChoice(), request.answerText(), request.confidence()));
    }

    @DeleteMapping("/quiz/{questionId}")
    public void removeAnswer(@PathVariable UUID attemptId, @PathVariable String questionId) {
        assessmentService.removeAnswer(attemptId, questionId);
    }

    @PutMapping("/checklist")
    public ChecklistResponse checklist(
            @PathVariable UUID attemptId,
            @Valid @RequestBody ChecklistRequest request
    ) {
        AssessmentRepository.Checklist saved = assessmentService.saveChecklist(attemptId, request.completed());
        return new ChecklistResponse(saved.completedItems(), saved.totalItems(), saved.score());
    }

    @PostMapping("/complete")
    public AssessmentService.CompletionResult complete(@PathVariable UUID attemptId) {
        return assessmentService.complete(attemptId);
    }

    public record QuizAnswerRequest(Integer selectedChoice, String answerText, Integer confidence) {
    }

    public record QuizAnswerResponse(
            String questionId,
            java.math.BigDecimal score,
            String feedback,
            boolean correct,
            Integer correctChoice,
            String explanation,
            String modelAnswer,
            List<String> missingIdeas
    ) {
        static QuizAnswerResponse from(AssessmentService.AnswerResult result) {
            return new QuizAnswerResponse(result.answer().questionId(), result.answer().score(), result.answer().feedback(),
                    result.correct(), result.correctChoice(), result.explanation(), result.modelAnswer(),
                    result.missingIdeas());
        }
    }

    public record ChecklistRequest(@NotEmpty List<Boolean> completed) {
    }

    public record ChecklistResponse(int completedItems, int totalItems, java.math.BigDecimal score) {
    }
}
