package org.example;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static java.util.stream.Collectors.*;

public class HooliganStatsParallelStream implements HooliganStatsCalculator {
    private final int delay;

    public HooliganStatsParallelStream() {
        this(0);
    }

    public HooliganStatsParallelStream(int delay) {
        this.delay = delay;
    }

    @Override
    public HooliganStats calculate(List<Hooligan> hooligans) {
        double avgGrade = hooligans.parallelStream()
                .mapToDouble(h -> h.getInfo(delay).grades().stream()
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0))
                .average()
                .orElse(0);

        ConcurrentMap<HooliganStatus, Long> courseStats = hooligans.parallelStream()
                .collect(groupingByConcurrent(h -> h.getStatus(delay), counting()));

        int[] result = hooligans.parallelStream()
                .flatMap(h -> h.getViolation(delay).stream())
                .collect(
                        () -> new int[]{0, 0},
                        (acc, v) -> {
                            acc[0]++;
                            if (v.hasExplanation) acc[1]++;
                        },
                        (a, b) -> {
                            a[0] += b[0];
                            a[1] += b[1];
                        }
                );

        double percentOfExplanation = result[0] == 0 ? 0 : (double) result[1] / result[0] * 100;

        ConcurrentMap<String, Long> explanationCategories = hooligans.parallelStream()
                .flatMap(h -> h.getViolation(delay).stream().filter(v -> v.hasExplanation))
                .collect(groupingByConcurrent(v -> v.behavior, counting()));

        long expelledCount = hooligans.parallelStream()
                .filter(h -> h.getStatus(delay) == HooliganStatus.EXPELLED)
                .count();
        double expelledPercent = (double) expelledCount / hooligans.size() * 100;

        return new HooliganStats(avgGrade, courseStats, percentOfExplanation, explanationCategories, expelledPercent);
    }
}

