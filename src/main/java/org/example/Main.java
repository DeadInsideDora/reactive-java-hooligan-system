package org.example;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        int[] sizes = new int[]{5000, 50000, 250000, 1000000};
        Map<String, HooliganStatsCalculator> calculators = Map.of(
                "Iterative", new HooliganStatsIterative(),
                "StreamAPI", new HooliganStatsStream(),
                "Collector", new HooliganStatsCollector()
        );

        for (int size : sizes) {
            List<Hooligan> hooligans = HooliganGenerator.generateHooligans(size);
            HooliganStatsBenchmark.runBenchmarks(calculators, hooligans, "result.txt");
        }
    }


}