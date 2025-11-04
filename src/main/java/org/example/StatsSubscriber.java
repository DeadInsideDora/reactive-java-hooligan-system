package org.example;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

public class StatsSubscriber implements Subscriber<Hooligan> {
    private final int batch;
    private final int totalCount;
    private final CountDownLatch done;
    private Subscription subscription;

    private double sumOfAvgGrades = 0.0;
    private long countGrades = 0;
    private final ConcurrentMap<HooliganStatus, Long> courseStats = new ConcurrentHashMap<>();
    private long totalViolations = 0;
    private long explainedViolations = 0;
    private final ConcurrentMap<String, Long> explanationCategories = new ConcurrentHashMap<>();
    private long expelledCount = 0;

    HooliganStats result;

    private long processed = 0;

    StatsSubscriber(int batch, int totalCount, CountDownLatch done) {
        this.batch = batch;
        this.totalCount = totalCount;
        this.done = done;
    }

    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;
        s.request(batch);
    }

    @Override
    public void onNext(Hooligan h) {
        double avg = h.getInfo(0).grades().stream()
                .mapToInt(Integer::intValue)
                .average().orElse(0.0);
        sumOfAvgGrades += avg;
        countGrades++;

        HooliganStatus status = h.getStatus(0);
        courseStats.merge(status, 1L, Long::sum);
        if (status == HooliganStatus.EXPELLED) expelledCount++;

        List<Violation> violations = h.getViolation(0);
        for (Violation v : violations) {
            totalViolations++;
            if (v.hasExplanation) {
                explainedViolations++;
                explanationCategories.merge(v.behavior, 1L, Long::sum);
            }
        }

        processed++;
        if (processed % batch == 0) {
            subscription.request(batch);
        }
    }

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
        done.countDown();
    }

    @Override
    public void onComplete() {
        double avgGrade = countGrades == 0 ? 0 : sumOfAvgGrades / countGrades;
        double percentOfExplanation = totalViolations == 0 ? 0
                : (double) explainedViolations / totalViolations * 100.0;
        double expelledPercent = (double) expelledCount / totalCount * 100.0;

        result = new HooliganStats(
                avgGrade,
                courseStats,
                percentOfExplanation,
                explanationCategories,
                expelledPercent
        );

        done.countDown();
    }
}
