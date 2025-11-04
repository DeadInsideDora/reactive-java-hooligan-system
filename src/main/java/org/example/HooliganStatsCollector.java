package org.example;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class HooliganStatsCollector implements Collector<Hooligan, HooliganStatsAccumulator, HooliganStats>, HooliganStatsCalculator {
    @Override
    public Supplier<HooliganStatsAccumulator> supplier() {
        return HooliganStatsAccumulator::new;
    }

    @Override
    public BiConsumer<HooliganStatsAccumulator, Hooligan> accumulator() {
        return HooliganStatsAccumulator::add;
    }

    @Override
    public BinaryOperator<HooliganStatsAccumulator> combiner() {
        return (a1, a2) -> {
            a1.combine(a2);
            return a1;
        };
    }

    @Override
    public Function<HooliganStatsAccumulator, HooliganStats> finisher() {
        return HooliganStatsAccumulator::finish;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();

    }

    @Override
    public HooliganStats calculate(List<Hooligan> hooligans) {
        Collector<Hooligan, HooliganStatsAccumulator, HooliganStats> myCollector =
                Collector.of(
                        HooliganStatsAccumulator::new,
                        HooliganStatsAccumulator::add,
                        (a1, a2) -> { a1.combine(a2); return a1; },
                        HooliganStatsAccumulator::finish
                );
        return hooligans.stream().collect(myCollector);
    }
}
