package org.example;

import java.time.LocalDate;
import java.util.List;

public class Hooligan {
    private final int id;
    private String name;
    private LocalDate bithDate;
    private HooliganStatus status;
    private Info info;
    private final List<Violation> violation;

    public Hooligan(int id, String name, LocalDate bithDate, HooliganStatus status, Info info, List<Violation> violation) {
        this.id = id;
        this.name = name;
        this.bithDate = bithDate;
        this.status = status;
        this.info = info;
        this.violation = violation;
    }

    public HooliganStatus getStatus() {
        return status;
    }

    public void setStatus(HooliganStatus status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBithDate() {
        return bithDate;
    }

    public void setBithDate(LocalDate bithDate) {
        this.bithDate = bithDate;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public List<Violation> getViolation() {
        return violation;
    }
}
