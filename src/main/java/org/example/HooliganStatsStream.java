package org.example;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

public class HooliganStatsStream implements HooliganStatsCalculator {
    @Override
    public HooliganStats calculate(List<Hooligan> hooligans) {
        double avgGrade = hooligans.stream()
                .mapToDouble(h -> h.getInfo().grades().stream()
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0))
                .average()
                .orElse(0);

        Map<HooliganStatus, Long> courseStats = hooligans.stream()
                .collect(groupingBy(Hooligan::getStatus, counting()));

        int[] result = hooligans.stream()
                .flatMap(h -> h.getViolation().stream())
                .reduce(
                        new int[]{0, 0},
                        (acc, v) -> {
                            acc[0]++;
                            if (v.hasExplanation) acc[1]++;
                            return acc;
                        },
                        (a, b) -> new int[]{a[0] + b[0], a[1] + b[1]}
                );
        double percentOfExplanation = result[0] == 0 ? 0 : (double) result[1] / result[0] * 100;

        Map<String, Long> explanationCategories = hooligans.stream()
                .flatMap(h -> h.getViolation().stream().filter(v -> v.hasExplanation))
                .collect(groupingBy(v -> v.behavior, counting()));

        long expelledCount = hooligans.stream()
                .filter(h -> h.getStatus() == HooliganStatus.EXPELLED)
                .count();
        double expelledPercent = (double) expelledCount / hooligans.size() * 100;

        return new HooliganStats(avgGrade, courseStats, percentOfExplanation, explanationCategories, expelledPercent);
    }
}
