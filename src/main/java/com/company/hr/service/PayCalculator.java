package com.company.hr.service;

import com.company.hr.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;


@Component
public class PayCalculator {

    private static final List<DayOfWeek> DAYS_OFF_ORDER = List.of(
            DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.THURSDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.TUESDAY, DayOfWeek.MONDAY);

    public int workingDaysInMonth(int workDaysPerWeek, YearMonth month) {
        int offCount = Math.min(Math.max(7 - workDaysPerWeek, 0), DAYS_OFF_ORDER.size());
        Set<DayOfWeek> off = EnumSet.noneOf(DayOfWeek.class);
        off.addAll(DAYS_OFF_ORDER.subList(0, offCount));

        int working = 0;
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            if (!off.contains(month.atDay(day).getDayOfWeek())) {
                working++;
            }
        }
        return working;
    }

    public BigDecimal dailyRate(User user, YearMonth month) {
        int days = workingDaysInMonth(user.getWorkDaysPerWeek(), month);
        return user.getBaseSalary().divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);
    }
}
