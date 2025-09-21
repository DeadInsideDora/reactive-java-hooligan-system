package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HooliganStatsIterative implements HooliganStatsCalculator {
    @Override
    public HooliganStats calculate(List<Hooligan> hooligans) {
        double totalGrades = 0;
        int totalExplanations = 0;
        int totalViolations = 0;
        int expelledCount = 0;

        Map<HooliganStatus, Long> courseStats = new HashMap<>();
        Map<String, Long> explanationCategories = new HashMap<>();

        for (Hooligan h : hooligans) {
            double sumGrades = 0;
            for (int grade : h.getInfo().grades()) {
                sumGrades += grade;
            }
            totalGrades += sumGrades / h.getInfo().grades().size();

            courseStats.merge(h.getStatus(), 1L, Long::sum);

            for (Violation v : h.getViolation()) {
                totalViolations++;
                if (v.hasExplanation) {
                    totalExplanations++;
                    explanationCategories.merge(v.behavior, 1L, Long::sum);
                }
            }

            if (h.getStatus() == HooliganStatus.EXPELLED) {
                expelledCount++;
            }
        }

        double avgGrade = totalGrades / hooligans.size();
        double percentOfExplanation = totalViolations > 0 ? (double) totalExplanations / hooligans.size() * 100 : 0;
        double expelledPercent = (double) expelledCount / hooligans.size() * 100;

        return new HooliganStats(avgGrade, courseStats, percentOfExplanation, explanationCategories, expelledPercent);
    }
}

