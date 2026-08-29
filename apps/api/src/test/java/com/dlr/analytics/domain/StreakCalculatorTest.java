package com.dlr.analytics.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StreakCalculatorTest {

    private final StreakCalculator calculator = new StreakCalculator();
    private final LocalDate today = LocalDate.of(2026, 8, 29);

    @Test
    void calculatesCurrentAndBestStreaks() {
        var result = calculator.calculate(List.of(
                today.minusDays(8), today.minusDays(7),
                today.minusDays(2), today.minusDays(1), today), today);
        assertThat(result.current()).isEqualTo(3);
        assertThat(result.best()).isEqualTo(3);
    }

    @Test
    void keepsTheCurrentStreakDuringOneRestDay() {
        var result = calculator.calculate(List.of(today.minusDays(2), today.minusDays(1)), today);
        assertThat(result.current()).isEqualTo(2);
    }

    @Test
    void resetsAnOldStreakWithoutLosingTheRecord() {
        var result = calculator.calculate(List.of(today.minusDays(5), today.minusDays(4)), today);
        assertThat(result.current()).isZero();
        assertThat(result.best()).isEqualTo(2);
    }
}
