package org.example;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class Hooligan {
    @Getter
    private final int id;
    @Getter
    @Setter
    private String name;
    @Getter
    private final LocalDate birthDate;
    @Getter
    @Setter
    private HooliganStatus status;
    @Getter
    @Setter
    private Info info;
    @Getter
    private final List<Violation> violation;

    public Hooligan(int id, String name, LocalDate birthDate, HooliganStatus status, Info info, List<Violation> violation) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.status = status;
        this.info = info;
        this.violation = violation;
    }

    private void sleep(int delay) {
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public Info getInfo(int delay) {
        sleep(delay);
        return getInfo();
    }

    public HooliganStatus getStatus(int delay) {
        sleep(delay);
        return getStatus();
    }

    public List<Violation> getViolation(int delay) {
        sleep(delay);
        return violation;
    }
}