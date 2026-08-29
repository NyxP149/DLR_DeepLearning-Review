package com.dlr.tutor.web;

import com.dlr.tutor.application.AiTutorPort;
import com.dlr.tutor.application.TutorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tutor")
public class TutorController {

    private final TutorService tutorService;

    public TutorController(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    @GetMapping("/status")
    public AiTutorPort.TutorStatus status() {
        return tutorService.status();
    }

    @PostMapping("/explain")
    public TutorService.TutorResponse explain(@Valid @RequestBody ExplainRequest request) {
        return tutorService.explain(request.labCode(), request.conceptCode(), request.question());
    }

    @PostMapping("/hint")
    public TutorService.TutorResponse hint(@Valid @RequestBody HintRequest request) {
        return tutorService.hint(request.labCode(), request.sourceCode(), request.level());
    }

    @PostMapping("/review-answer")
    public TutorService.TutorResponse reviewAnswer(@Valid @RequestBody ReviewAnswerRequest request) {
        return tutorService.reviewAnswer(request.labCode(), request.questionCode(), request.answer());
    }

    public record ExplainRequest(
            @NotBlank String labCode,
            @NotBlank String conceptCode,
            @Size(max = 2_000) String question
    ) {}

    public record HintRequest(
            @NotBlank String labCode,
            @Size(max = 65_536) String sourceCode,
            @Min(1) @Max(3) int level
    ) {}

    public record ReviewAnswerRequest(
            @NotBlank String labCode,
            @NotBlank String questionCode,
            @NotBlank @Size(max = 4_000) String answer
    ) {}
}
