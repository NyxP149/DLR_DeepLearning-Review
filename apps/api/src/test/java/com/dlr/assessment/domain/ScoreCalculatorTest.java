package com.dlr.assessment.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScoreCalculatorTest {

    private final ScoreCalculator calculator = new ScoreCalculator();

    @Test
    void calculatesTheWeightedScoreWithoutIntermediateRounding() {
        var input = new ScoreCalculator.ScoreInput(
                value("85"), value("70"), value("80"), value("60"), value("90"), 70);

        var result = calculator.calculate(input);

        assertThat(result.score()).isEqualByComparingTo("79.00");
        assertThat(result.thresholdReached()).isTrue();
    }

    @Test
    void keepsProgressPossibleWhileReportingThatThresholdWasNotReached() {
        var input = new ScoreCalculator.ScoreInput(
                value("50"), value("60"), value("55"), value("40"), value("80"), 70);

        var result = calculator.calculate(input);

        assertThat(result.score()).isEqualByComparingTo("55.00");
        assertThat(result.thresholdReached()).isFalse();
    }

    @Test
    void rejectsASubScoreOutsideTheExpectedRange() {
        var input = new ScoreCalculator.ScoreInput(
                value("101"), value("60"), value("55"), value("40"), value("80"), 70);

        assertThatThrownBy(() -> calculator.calculate(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tests");
    }

    private BigDecimal value(String value) {
        return new BigDecimal(value);
    }
}

