package org.example;

public enum Punishment {
    REMARK("Замечание"),
    WARNING("Выговор"),
    EXPULSION("Отчисление");

    private final String description;

    Punishment(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public double getPunishmentValues(Punishment punishment) {
        double value;
        switch (punishment) {
            case REMARK -> value = 0.1;
            case WARNING -> value = 0.4;
            case EXPULSION -> value = 1;
            default -> value = 0;
        }
        return value;
    }
}
