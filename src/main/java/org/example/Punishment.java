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
}
