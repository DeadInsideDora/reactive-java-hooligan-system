package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class HooliganStatsBenchmark {

    public static void runBenchmarks(Map<String, HooliganStatsCalculator> calculators,
                                     List<Hooligan> hooligans,
                                     String filename) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
            writer.println("=== Benchmark started at " + LocalDateTime.now().format(formatter) + " ===");

            calculators.forEach((name, calculator) -> {
                BenchmarkUtils.ResultWithTime<HooliganStats> result = BenchmarkUtils.measure(name + " " + hooligans.size(),
                        () -> calculator.calculate(hooligans));
                String fullResult = LocalDateTime.now().format(formatter) + " - " + result;

                System.out.println(result);
                writer.println(fullResult);
            });

            writer.println("=== Benchmark completed ===");
        } catch (IOException e) {
            System.err.println("Ошибка записи в файл: " + e.getMessage());
        }
    }
}
