package com.dlr.analytics.domain;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Component
public class StreakCalculator {

    public Streak calculate(List<LocalDate> sessionDates, LocalDate today) {
        List<LocalDate> dates = sessionDates.stream().distinct().sorted(Comparator.naturalOrder()).toList();
        if (dates.isEmpty()) return new Streak(0, 0);

        int best = 1;
        int running = 1;
        for (int index = 1; index < dates.size(); index++) {
            if (ChronoUnit.DAYS.between(dates.get(index - 1), dates.get(index)) == 1) {
                running++;
                best = Math.max(best, running);
            } else {
                running = 1;
            }
        }

        LocalDate latest = dates.getLast();
        long sinceLatest = ChronoUnit.DAYS.between(latest, today);
        if (sinceLatest < 0 || sinceLatest > 1) return new Streak(0, best);

        int current = 1;
        for (int index = dates.size() - 1; index > 0; index--) {
            if (ChronoUnit.DAYS.between(dates.get(index - 1), dates.get(index)) != 1) break;
            current++;
        }
        return new Streak(current, best);
    }

    public record Streak(int current, int best) {
    }
}
