package org.example;
import java.util.HashMap;
import java.util.Map;

public class HooliganStatsAccumulator {
    double totalGrade = 0;

    Map<HooliganStatus, Long> courseStats = new HashMap<>();

    int totalViolations = 0;
    int totalExplanations = 0;
    Map<String, Long> explanationCategories = new HashMap<>();

    int totalHooligans = 0;
    int expelledCount = 0;

    void add(Hooligan h) {
        double avg = h.getInfo().grades().stream()
                .mapToInt(Integer::intValue)
                .average().orElse(0);
        totalGrade += avg;

        courseStats.merge(h.getStatus(), 1L, Long::sum);

        h.getViolation().forEach(v -> {
            totalViolations++;
            if (v.hasExplanation) {
                totalExplanations++;
                explanationCategories.merge(v.behavior, 1L, Long::sum);
            }
        });

        if (h.getStatus() == HooliganStatus.EXPELLED) {
            expelledCount++;
        }
        totalHooligans++;
    }

    HooliganStats finish() {
        double avgGrade = totalHooligans == 0 ? 0 : totalGrade / totalHooligans;
        double percentOfExplanation = totalViolations == 0 ? 0 : (double) totalExplanations / totalViolations * 100;
        double expelledPercent = totalHooligans == 0 ? 0 : (double) expelledCount / totalHooligans * 100;

        return new HooliganStats(avgGrade, courseStats, percentOfExplanation, explanationCategories, expelledPercent);
    }

    void combine(HooliganStatsAccumulator other) {
        this.totalGrade += other.totalGrade;
        other.courseStats.forEach((k, v) -> this.courseStats.merge(k, v, Long::sum));
        this.totalViolations += other.totalViolations;
        this.totalExplanations += other.totalExplanations;
        other.explanationCategories.forEach((k, v) -> this.explanationCategories.merge(k, v, Long::sum));
        this.totalHooligans += other.totalHooligans;
        this.expelledCount += other.expelledCount;
    }
}

