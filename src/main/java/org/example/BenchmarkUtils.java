package org.example;

import java.util.function.Supplier;

public class BenchmarkUtils {

    public static <T> ResultWithTime<T> measure(String label, Supplier<T> supplier) {
        long start = System.nanoTime();
        T result = supplier.get();
        long end = System.nanoTime();
        String timeInfo = String.format("%s: %d ms", label, (end - start) / 1_000_000);
        return new ResultWithTime<>(result, timeInfo, (end - start) / 1_000_000);
    }

    public record ResultWithTime<T>(T result, String timeString, long timeMs) {

        @Override
            public String toString() {
                return timeString;
            }
        }
}
