package org.example;

import org.example.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class JMHHooliganRxBenchmark {

    @Param({"500", "2000"})
    public int size;

    @Param({"5"})
    public int delayMillis;

    private List<Hooligan> data;
    private HooliganStatsCalculator streamsCalc;
    private HooliganStatsCalculator rxCalc;

    @Setup(Level.Trial)
    public void setup() {
        data = HooliganGenerator.generateHooligans(size);
        streamsCalc = new HooliganStatsSpliterator(delayMillis);
        rxCalc = new HooliganStatsJavaRx(delayMillis, Math.max(2, Runtime.getRuntime().availableProcessors()));
    }

    @Benchmark
    public void parallelStreams(Blackhole bh) throws InterruptedException {
        HooliganStats stats = streamsCalc.calculate(data);
        bh.consume(stats);
    }

    @Benchmark
    public void rxObservable(Blackhole bh) throws InterruptedException {
        HooliganStats stats = rxCalc.calculate(data);
        bh.consume(stats);
    }
}


