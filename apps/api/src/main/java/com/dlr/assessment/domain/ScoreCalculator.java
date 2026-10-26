package com.dlr.assessment.domain;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ScoreCalculator {

    /** Pondérations V1 : tests 40, quiz 20, pratique 20, explication 10, checklist 10. */
    public static final Weights V1 = new Weights(40, 20, 20, 10, 10);

    /**
     * Pondérations V2 : moins de poids sur l'exécution seule (30) et sur la checklist déclarative (5),
     * davantage sur la récupération de mémoire (quiz 25, explication 20) et une pratique « sans aide » (20).
     */
    public static final Weights V2 = new Weights(30, 25, 20, 20, 5);

    public ScoreResult calculate(ScoreInput input) {
        return calculate(input, V1);
    }

    public ScoreResult calculate(ScoreInput input, Weights weights) {
        validate(input.tests(), "tests");
        validate(input.quiz(), "quiz");
        validate(input.practice(), "practice");
        validate(input.connections(), "connections");
        validate(input.selfAssessment(), "selfAssessment");

        BigDecimal total = weighted(input.tests(), weights.tests())
                .add(weighted(input.quiz(), weights.quiz()))
                .add(weighted(input.practice(), weights.practice()))
                .add(weighted(input.connections(), weights.connections()))
                .add(weighted(input.selfAssessment(), weights.selfAssessment()))
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

    public record Weights(int tests, int quiz, int practice, int connections, int selfAssessment) {
        public Weights {
            if (tests + quiz + practice + connections + selfAssessment != 100) {
                throw new IllegalArgumentException("Les pondérations doivent totaliser 100.");
            }
        }
    }
}

