package org.example;

import java.util.List;
import java.util.Map;

public class HooliganStatsBenchmark {

    public static void runBenchmarks(Map<String, HooliganStatsCalculator> calculators, List<Hooligan> hooligans) {
        calculators.forEach((k, v) ->
                BenchmarkUtils.measure(k + " " + hooligans.size(), () -> v.calculate(hooligans)));
    }
}
