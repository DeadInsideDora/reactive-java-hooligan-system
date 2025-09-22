package org.example;

import java.time.LocalDate;

public class Violation {
    public final int aggressionLevel;
    public final boolean alcoholUsage;
    public final Department department;
    public final LocalDate violDate;
    public final String behavior;
    public final Punishment punishment;
    public final boolean hasExplanation;

    public Violation(int aggressionLevel, boolean alcoholUsage, Department department, LocalDate violDate,
                     String behavior, Punishment punishment, boolean hasExplanation) {
        this.aggressionLevel = aggressionLevel;
        this.alcoholUsage = alcoholUsage;
        this.department = department;
        this.violDate = violDate;
        this.behavior = behavior;
        this.punishment = punishment;
        this.hasExplanation = hasExplanation;
    }

    public double getPunishmentValue() {
        return punishment.getPunishmentValues(punishment);
    }
}
