package com.dlr.assessment.domain;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ScoreCalculator {

    public ScoreResult calculate(ScoreInput input) {
        validate(input.tests(), "tests");
        validate(input.quiz(), "quiz");
        validate(input.practice(), "practice");
        validate(input.connections(), "connections");
        validate(input.selfAssessment(), "selfAssessment");

        BigDecimal total = weighted(input.tests(), 40)
                .add(weighted(input.quiz(), 20))
                .add(weighted(input.practice(), 20))
                .add(weighted(input.connections(), 10))
                .add(weighted(input.selfAssessment(), 10))
                .setScale(2, RoundingMode.HALF_UP);

        return new ScoreResult(total, total.compareTo(BigDecimal.valueOf(input.threshold())) >= 0);
    }

    private BigDecimal weighted(BigDecimal score, int weight) {
        return score.multiply(BigDecimal.valueOf(weight))
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }

    private void validate(BigDecimal score, String field) {
        if (score == null || score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException(field + " doit être compris entre 0 et 100");
        }
    }

    public record ScoreInput(
            BigDecimal tests,
            BigDecimal quiz,
            BigDecimal practice,
            BigDecimal connections,
            BigDecimal selfAssessment,
            int threshold
    ) {
        public ScoreInput {
            if (threshold < 0 || threshold > 100) {
                throw new IllegalArgumentException("threshold doit être compris entre 0 et 100");
            }
        }
    }

    public record ScoreResult(BigDecimal score, boolean thresholdReached) {
    }
}

