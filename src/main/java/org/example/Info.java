package org.example;

import java.time.LocalDate;
import java.util.List;

public record Info(
        LocalDate enrollmentDate,
        String faculty,
        List<Integer> grades,
        boolean livesInDormitory
) {}
