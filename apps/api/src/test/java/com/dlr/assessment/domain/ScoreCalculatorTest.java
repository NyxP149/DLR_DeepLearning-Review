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
    void v2WeighsRecallAndUnassistedPracticeMoreThanTheSelfDeclaredChecklist() {
        var input = new ScoreCalculator.ScoreInput(
                value("100"), value("50"), value("65"), value("100"), value("100"), 70);

        var v1 = calculator.calculate(input);
        var v2 = calculator.calculate(input, ScoreCalculator.V2);

        // V1 : 40 + 10 + 13 + 10 + 10 ; V2 : 30 + 12.5 + 13 + 20 + 5
        assertThat(v1.score()).isEqualByComparingTo("83.00");
        assertThat(v2.score()).isEqualByComparingTo("80.50");
    }

    @Test
    void rejectsWeightsThatDoNotTotalOneHundred() {
        assertThatThrownBy(() -> new ScoreCalculator.Weights(30, 25, 20, 20, 10))
                .isInstanceOf(IllegalArgumentException.class);
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

