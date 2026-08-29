package com.dlr.execution.web;

import com.dlr.execution.application.ExecutionService;
import com.dlr.execution.domain.ExecutionResult;
import com.dlr.execution.domain.Submission;
import com.dlr.execution.domain.SubmissionOrigin;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/attempts/{attemptId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse submit(
            @PathVariable UUID attemptId,
            @Valid @RequestBody SubmissionRequest request
    ) {
        Submission submission = executionService.createSubmission(
                attemptId, request.language(), request.sourceCode(), request.origin());
        return SubmissionResponse.from(submission);
    }

    @PostMapping("/submissions/{submissionId}/run")
    public ExecutionResult run(@PathVariable UUID submissionId) {
        return executionService.run(submissionId);
    }

    public record SubmissionRequest(
            @NotBlank String language,
            @NotBlank @Size(max = 65_536) String sourceCode,
            @NotNull SubmissionOrigin origin
    ) {
    }

    public record SubmissionResponse(
            UUID id,
            UUID attemptId,
            String language,
            SubmissionOrigin origin,
            Instant createdAt
    ) {
        static SubmissionResponse from(Submission submission) {
            return new SubmissionResponse(
                    submission.id(),
                    submission.attemptId(),
                    submission.language(),
                    submission.origin(),
                    submission.createdAt());
        }
    }
}

