package org.example;

import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HooliganStatsSubscriber implements HooliganStatsCalculator {
    private final int batchSize;

    public HooliganStatsSubscriber() {
        this(0);
    }

    public HooliganStatsSubscriber(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public HooliganStats calculate(List<Hooligan> hooligans) throws InterruptedException {
        Flowable<Hooligan> flow = Flowable.fromIterable(hooligans)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .onBackpressureBuffer(
                        50_000,
                        () -> System.out.println("Buffer overflow!"),
                        BackpressureOverflowStrategy.DROP_OLDEST
                );

        CountDownLatch latch = new CountDownLatch(1);
        StatsSubscriber subscriber = new StatsSubscriber(batchSize, hooligans.size(), latch);
        flow.subscribe(subscriber);

        try {
            latch.await(60, TimeUnit.SECONDS);

            return subscriber.result;
        } catch (InterruptedException e) {
            throw e;
        }
    }
}
