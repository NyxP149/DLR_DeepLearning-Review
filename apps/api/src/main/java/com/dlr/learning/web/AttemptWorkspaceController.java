package com.dlr.learning.web;

import com.dlr.assessment.application.AssessmentRepository;
import com.dlr.assessment.application.AssessmentService;
import com.dlr.execution.application.ExecutionService;
import com.dlr.execution.domain.Submission;
import com.dlr.learning.application.AttemptService;
import com.dlr.learning.domain.Attempt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/labs/{labCode}/attempts")
public class AttemptWorkspaceController {

    private final AttemptService attemptService;
    private final ExecutionService executionService;
    private final AssessmentService assessmentService;

    public AttemptWorkspaceController(
            AttemptService attemptService,
            ExecutionService executionService,
            AssessmentService assessmentService
    ) {
        this.attemptService = attemptService;
        this.executionService = executionService;
        this.assessmentService = assessmentService;
    }

    @GetMapping("/current")
    public ResponseEntity<WorkspaceResponse> current(@PathVariable String labCode) {
        return attemptService.current(labCode)
                .map(this::workspace)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private WorkspaceResponse workspace(Attempt attempt) {
        Submission submission = executionService.latestSubmission(attempt.id()).orElse(null);
        AssessmentService.AssessmentState assessment = assessmentService.state(attempt.id());
        return new WorkspaceResponse(
                attempt,
                submission == null ? null : submission.sourceCode(),
                submission == null ? null : submission.origin().name(),
                assessment.answers().stream().map(QuizDraft::from).toList(),
                assessment.checklist());
    }

    public record WorkspaceResponse(
            Attempt attempt,
            String sourceCode,
            String sourceOrigin,
            List<QuizDraft> quizAnswers,
            List<Boolean> checklist
    ) {
    }

    public record QuizDraft(String questionId, Integer selectedChoice, String answerText, String feedback) {
        static QuizDraft from(AssessmentRepository.QuizAnswer answer) {
            return new QuizDraft(
                    answer.questionId(), answer.selectedChoice(), answer.answerText(), answer.feedback());
        }
    }
}
