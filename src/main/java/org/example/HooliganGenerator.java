package org.example;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HooliganGenerator {

    private static final int maxAge = 90;

    private static int counter = 100000;

    private static final Random RANDOM = new Random();

    private static final String[] NAMES = {
            "Степан", "Прокопий", "Игорь", "Эрик", "Иван", "Петр", "Сергей", "Алексей", "Максим",
            "Дмитрий", "Анна", "Екатерина", "Мария", "Ольга", "Юлия", "Милана", "Владимир"
    };

    private static final String[] FACULTIES = {
            "ФСУиР", "ФПИиКТ", "ФБИТ", "ФИТиП", "ФПИ", "ФТИИ", "ИНФОХИМИЯ", "ФЭкотехнологий",
            "ФБиотехнологий", "ФТМиИ"
    };

    private static final String[] BEHAVIORS = {
            "Драка", "Опоздание на лекцию", "Хамство преподавателю",
            "Порча имущества универститета", "Почра имущества общежития",
            "Распитие алкоголя", "Разжигание межнациональной розни", "Курение",
            "Плагиат", "Подделка медицинскх справок", "Нахождение в нетрезвом состоянии",
            "Угроза преподавателю или студенту", "Кража", "Организация массовой драки",
            "Предоставление доступа постороннему к своему пропуску"
    };

    private static final String[] DEPARTMENTS = {
            "ГК", "Биржа", "Ломо", "Чайка", "Гривцова", "Хайпарк", "Вязьма", "Б6", "Ленсовета", "МСГ"
    };

    private static LocalDate generateBirthday(HooliganStatus status) {
        int minAge;

        switch (status) {
            case STUDYING_BACH_2, EXPELLED -> minAge = 18;
            case STUDYING_BACH_3 -> minAge = 19;
            case STUDYING_BACH_4 -> minAge = 20;
            case STUDYING_MASTER_1, ITMO_FAMILY-> minAge = 21;
            case STUDYING_MASTER_2 -> minAge = 22;
            case STUDYING_PHD -> minAge = 23;
            default -> minAge = 17;
        }

        int age = minAge + RANDOM.nextInt(maxAge - minAge + 1);
        LocalDate now = LocalDate.now();
        int year = now.getYear() - age;
        int month = 1 + RANDOM.nextInt(12);
        int maxDay = YearMonth.of(year, month).lengthOfMonth();
        int day = 1 + RANDOM.nextInt(maxDay);

        return LocalDate.of(year, month, day);
    }

    private static Info generateInfo(HooliganStatus status, LocalDate birthDate) {
        LocalDate now = LocalDate.now();

        int year;
        switch (status) {
            case STUDYING_BACH_1, STUDYING_MASTER_1 -> year = now.getYear();
            case STUDYING_BACH_2, STUDYING_MASTER_2 -> year = now.getYear() - 1;
            case STUDYING_BACH_3 -> year = now.getYear() - 2;
            case STUDYING_BACH_4 -> year = now.getYear() - 3;
            case STUDYING_PHD -> year = RANDOM.nextInt(birthDate.getYear() + 23, now.getYear() + 1);
            case ITMO_FAMILY -> year = RANDOM.nextInt(birthDate.getYear() + 21, now.getYear() + 1);
            case SABBATICAL_YEAR, EXPELLED -> year =RANDOM.nextInt(birthDate.getYear() + 17, now.getYear() + 1);
            default -> year = now.getYear();
        }

        LocalDate enrollmentDate = LocalDate.of(year, 9, 1);
        String faculty = FACULTIES[RANDOM.nextInt(FACULTIES.length)];

        boolean livesInDormitory;
        switch (status) {
            case SABBATICAL_YEAR, ITMO_FAMILY, EXPELLED -> livesInDormitory = false;
            default -> livesInDormitory = RANDOM.nextBoolean();
        }
        int count = 9 + RANDOM.nextInt(9);
        List<Integer> grades = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            grades.add(2 + RANDOM.nextInt(4));
        }

        return new Info(enrollmentDate, faculty, grades, livesInDormitory);
    }

    private static List<Violation> generateViolations(LocalDate enrollmentDate) {
        int count = RANDOM.nextInt(4);
        List<Violation> violations = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            boolean inDept = RANDOM.nextBoolean();
            Department department = new Department(
                    DEPARTMENTS[RANDOM.nextInt(DEPARTMENTS.length)],
                    inDept,
                    inDept ? 1000 + RANDOM.nextInt(6000): -1
            );

            String behavior =  BEHAVIORS[RANDOM.nextInt(BEHAVIORS.length)];
            boolean alcoholUsage;
            if (behavior.equalsIgnoreCase("Распитие алкоголя")
                    || behavior.equalsIgnoreCase("Нахождение в нетрезвом состоянии")) {
                alcoholUsage = true;
            } else {
                alcoholUsage = RANDOM.nextBoolean();
            }

            Punishment punishment = Punishment.values()[RANDOM.nextInt(Punishment.values().length)];

            Violation v = new Violation(
                    1 + RANDOM.nextInt(5),
                    alcoholUsage,
                    department,
                    LocalDate.now().minusDays(RANDOM.nextLong(ChronoUnit.DAYS.between(enrollmentDate, LocalDate.now()))),
                    behavior,
                    Punishment.values()[RANDOM.nextInt(Punishment.values().length)],
                    RANDOM.nextBoolean()
            );

            violations.add(v);

            if (punishment == Punishment.EXPULSION) {
                break;
            }
        }
        return violations;
    }

    public static Hooligan generateHooligan() {

        String name = NAMES[RANDOM.nextInt(NAMES.length)];

        HooliganStatus status = HooliganStatus.values()[RANDOM.nextInt(HooliganStatus.values().length)];

        LocalDate birthDate = generateBirthday(status);

        Info record = generateInfo(status, birthDate);

        List<Violation> violations = generateViolations(record.enrollmentDate());

        if (violations.stream().anyMatch(v -> v.punishment == Punishment.EXPULSION)) {
            status = HooliganStatus.EXPELLED;
        }

        return new Hooligan(counter++, name, birthDate, status, record, violations);
    }

    public static List<Hooligan> generateHooligans(int n) {
        List<Hooligan> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(generateHooligan());
        }
        return list;
    }
}
