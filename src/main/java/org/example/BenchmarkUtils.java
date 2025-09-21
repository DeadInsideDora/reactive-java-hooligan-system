package org.example;

import java.util.function.Supplier;

public class BenchmarkUtils {

    public static <T> T measure(String label, Supplier<T> supplier) {
        long start = System.nanoTime();
        T result = supplier.get();
        long end = System.nanoTime();
        System.out.printf("%s: %d ms%n", label, (end - start) / 1_000_000);
        return result;
    }
}
