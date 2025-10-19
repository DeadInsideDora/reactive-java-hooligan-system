package org.example;

import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class JMHHooliganStatsBenchmark {
    @Param({"1000", "10000", " "})
    private int size;

    @Param({"0", "1"})
    private int delay;

    private List<Hooligan> hooligans;
    private HooliganStatsCalculator sequential;
    private HooliganStatsCalculator parallel;

    @Setup(Level.Trial)
    public void setup() {
        hooligans = HooliganGenerator.generateHooligans(size);
        sequential = new HooliganStatsStream(delay);
        parallel = new HooliganStatsParallelStream(delay);
    }

    @Benchmark
    public HooliganStats sequential() {
        return sequential.calculate(hooligans);
    }

    @Benchmark
    public HooliganStats parallel() {
        return parallel.calculate(hooligans);
    }
}
