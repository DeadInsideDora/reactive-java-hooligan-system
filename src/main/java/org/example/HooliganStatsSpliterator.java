package org.example;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.*;

public class HooliganStatsSpliterator implements HooliganStatsCalculator {
    private final int delay;

    public HooliganStatsSpliterator() {
        this(0);
    }

    public HooliganStatsSpliterator(int delay) {
        this.delay = delay;
    }

    @Override
    public HooliganStats calculate(List<Hooligan> hooligans) {
        HooliganSpliterator spliterator = new HooliganSpliterator(hooligans);

        var stream = StreamSupport.stream(spliterator, true);

        double avgGrade = stream
                .mapToDouble(h -> h.getInfo(delay).grades().stream()
                        .mapToInt(Integer::intValue)
                        .average().orElse(0))
                .average().orElse(0);

        ConcurrentMap<HooliganStatus, Long> courseStats = StreamSupport.stream(new HooliganSpliterator(hooligans), true)
                .collect(groupingByConcurrent(h -> h.getStatus(delay), counting()));

        int[] result = StreamSupport.stream(new HooliganSpliterator(hooligans), true)
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

        ConcurrentMap<String, Long> explanationCategories = StreamSupport.stream(new HooliganSpliterator(hooligans), true)
                .flatMap(h -> h.getViolation(delay).stream().filter(v -> v.hasExplanation))
                .collect(groupingByConcurrent(v -> v.behavior, counting()));

        long expelledCount = StreamSupport.stream(new HooliganSpliterator(hooligans), true)
                .filter(h -> h.getStatus(delay) == HooliganStatus.EXPELLED)
                .count();

        double expelledPercent = (double) expelledCount / hooligans.size() * 100;

        return new HooliganStats(avgGrade, courseStats, percentOfExplanation, explanationCategories, expelledPercent);
    }
}
