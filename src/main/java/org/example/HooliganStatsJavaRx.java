package org.example;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HooliganStatsJavaRx implements HooliganStatsCalculator {
    private final int delay;
    private final int concurrency;

    public HooliganStatsJavaRx() {
        this(0, Math.max(2, Runtime.getRuntime().availableProcessors()));
    }

    public HooliganStatsJavaRx(int delay) {
        this(delay, Math.max(2, Runtime.getRuntime().availableProcessors()));
    }

    public HooliganStatsJavaRx(int delay, int concurrency) {
        this.delay = delay;
        this.concurrency = concurrency;
    }

    @Override
    public HooliganStats calculate(List<Hooligan> hooligans) {
        Observable<Hooligan> src = Observable.fromIterable(hooligans)
                .observeOn(Schedulers.computation())
                .publish()
                .autoConnect();

        Single<Double> avgGrade = src
                .flatMap((Hooligan h) ->
                                Single.fromCallable(() ->
                                                h.getInfo(delay).grades().stream()
                                                        .mapToInt(Integer::intValue)
                                                        .average().orElse(0.0)
                                        )
                                        .subscribeOn(Schedulers.io())
                                        .toObservable(),
                        concurrency
                )
                .toList()
                .map(list -> list.stream()
                        .mapToDouble(Double::doubleValue)
                        .average().orElse(0.0)
                );

        Single<ConcurrentMap<HooliganStatus, Long>> courseStats = src
                .flatMap(
                        (Hooligan h) ->
                                Single.fromCallable(() -> h.getStatus(delay))
                                        .subscribeOn(Schedulers.io())
                                        .toObservable(),
                        concurrency
                )
                .collect(
                        ConcurrentHashMap::new,
                        (map, status) -> map.merge(status, 1L, Long::sum)
                );

        Single<double[]> violationsAgg = src
                .flatMap(
                        (Hooligan h) ->
                                Observable.fromIterable(h.getViolation(delay))
                                        .subscribeOn(Schedulers.io()),
                        concurrency
                )
                .collect(() -> new double[]{0, 0}, (acc, v) -> {
                    acc[0] += 1;
                    if (v.hasExplanation) acc[1] += 1;
                });

        Single<ConcurrentMap<String, Long>> explanationCategories = src
                .flatMap(
                        (Hooligan h) ->
                                Observable.fromIterable(h.getViolation(delay))
                                        .subscribeOn(Schedulers.io()),
                        concurrency
                )
                .filter(v -> v.hasExplanation)
                .collect(
                        ConcurrentHashMap::new,
                        (map, v) -> map.merge(v.behavior, 1L, Long::sum)
                );

        Single<Long> expelledCount = src
                .flatMap(
                        (Hooligan h) ->
                                Single.fromCallable(() -> h.getStatus(delay))
                                        .subscribeOn(Schedulers.io())
                                        .toObservable(),
                        concurrency
                )
                .filter(st -> st == HooliganStatus.EXPELLED)
                .count();

        return Single.zip(
                avgGrade,
                courseStats,
                violationsAgg,
                explanationCategories,
                expelledCount,
                (avg, cs, viol, cats, expelled) -> {
                    double percentOfExplanation =
                            viol[0] == 0 ? 0.0 : (viol[1] / viol[0]) * 100.0;
                    double expelledPercent =
                            hooligans.isEmpty() ? 0.0 :
                                    (double) expelled / hooligans.size() * 100.0;

                    return new HooliganStats(
                            avg,
                            cs,
                            percentOfExplanation,
                            cats,
                            expelledPercent
                    );
                }
        ).blockingGet();
    }
}
