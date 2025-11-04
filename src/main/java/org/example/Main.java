package org.example;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        List<Hooligan> hooligans = HooliganGenerator.generateHooligans(150_000);

        {
            HooliganStatsSubscriber sub = new HooliganStatsSubscriber(1024);
            var result = sub.calculate(hooligans);
            System.out.printf("HooliganStatsSubscriber: avgGrade=(%.2f%%) expelledPercent=(%.2f%%), explanationPercent=(%.2f%%)\n", result.avgGrade, result.expelledPercent, result.percentOfExplanation);
        }

        {
            HooliganStatsParallelStream sub = new HooliganStatsParallelStream(0);
            var result = sub.calculate(hooligans);
            System.out.printf("HooliganStatsParallelStream: avgGrade=(%.2f%%) expelledPercent=(%.2f%%), explanationPercent=(%.2f%%)\n", result.avgGrade, result.expelledPercent, result.percentOfExplanation);
        }
    }
}