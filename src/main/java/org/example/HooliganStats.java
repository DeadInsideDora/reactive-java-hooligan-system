package org.example;

import java.util.Map;

public class HooliganStats {
    double avgGrade;
    Map<HooliganStatus, Long> courseStats;
    double percentOfExplanation;
    Map<String, Long> explanationCategories;
    double expelledPercent;

    public HooliganStats(double avgGrade,
                         Map<HooliganStatus, Long> courseStats,
                         double percentOfExplanation,
                         Map<String, Long> explanationCategories,
                         double expelledPercent) {
        this.avgGrade = avgGrade;
        this.courseStats = courseStats;
        this.percentOfExplanation = percentOfExplanation;
        this.explanationCategories = explanationCategories;
        this.expelledPercent = expelledPercent;
    }

    @Override
    public String toString() {
        return "Средний балл: " + avgGrade + "\n" +
                "По курсам: " + courseStats + "\n" +
                "Процент объяснительных: " + percentOfExplanation + "%\n" +
                "По категориям: " + explanationCategories + "\n" +
                "Отчислены: " + expelledPercent + "%";
    }
}
