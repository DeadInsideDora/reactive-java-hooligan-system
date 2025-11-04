package org.example;

import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class HooliganSpliterator implements Spliterator<Hooligan> {
    private final List<Hooligan> hooligans;
    private int current;
    private final int end;

    public HooliganSpliterator(List<Hooligan> hooligans) {
        this(hooligans, 0, hooligans.size());
    }

    private HooliganSpliterator(List<Hooligan> hooligans, int start, int end) {
        this.hooligans = hooligans;
        this.current = start;
        this.end = end;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Hooligan> action) {
        if (current < end) {
            action.accept(hooligans.get(current++));
            return true;
        }
        return false;
    }

    @Override
    public Spliterator<Hooligan> trySplit() {
        if (end - current < 2) return null;
        int mid = current + (end - current) / 2;
        HooliganSpliterator split = new HooliganSpliterator(hooligans, current, mid);
        current = mid;
        return split;
    }

    @Override
    public long estimateSize() {
        return end - current;
    }

    @Override
    public int characteristics() {
        return ORDERED | SIZED | SUBSIZED | NONNULL | IMMUTABLE | DISTINCT;
    }
}
