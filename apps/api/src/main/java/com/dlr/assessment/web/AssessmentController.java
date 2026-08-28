package com.dlr.assessment.web;

import com.dlr.assessment.domain.ScoreCalculator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final ScoreCalculator scoreCalculator;

    public AssessmentController(ScoreCalculator scoreCalculator) {
        this.scoreCalculator = scoreCalculator;
    }

    @PostMapping("/score")
    public ScoreCalculator.ScoreResult calculate(@Valid @RequestBody ScoreRequest request) {
        return scoreCalculator.calculate(request.toInput());
    }

    public record ScoreRequest(
            @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal tests,
            @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal quiz,
            @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal practice,
            @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal connections,
            @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal selfAssessment,
            @Min(0) @Max(100) int threshold
    ) {
        ScoreCalculator.ScoreInput toInput() {
            return new ScoreCalculator.ScoreInput(
                    tests, quiz, practice, connections, selfAssessment, threshold);
        }
    }
}

