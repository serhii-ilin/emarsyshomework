package com.emarsys.duedatecalculator;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;

public class Calculator {

    private static final int DAYS_IN_WORK_WEEK = 5;
    private static final int HOUR_START_WORK_DAY = 9;
    private static final int HOUR_END_WORK_DAY = 17;
    private static final LocalTime START_OF_DAY = LocalTime.of(HOUR_START_WORK_DAY, 0);
    private static final LocalTime END_OF_DAY = LocalTime.of(HOUR_END_WORK_DAY, 0);
    private static final int WORK_HOURS_IN_DAY = 8;
    private static final EnumSet<DayOfWeek> WEEKEND = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    /**
     * Calculate due date
     * <p>
     * Note: Intentionally use duration in hours instead of Duration or Period, they can accept different time units,
     * and perform implicit conversions that can be confusing.
     *
     * @param submitDateTime  - submit date/time
     * @param turnaroundHours - turnaround time in hours
     * @return - returns the date/time when the issue is resolved
     */
    LocalDateTime calculateDueDate(LocalDateTime submitDateTime, int turnaroundHours) {
        checkRequiredArgs(submitDateTime, turnaroundHours);
        checkOnWorkDay(submitDateTime);
        checkBusinessHours(submitDateTime);

        int days = turnaroundHours / WORK_HOURS_IN_DAY;
        int workWeeks = days / DAYS_IN_WORK_WEEK;
        int daysRemainder = days % DAYS_IN_WORK_WEEK;
        int hoursRemainder = turnaroundHours % WORK_HOURS_IN_DAY;

        // We can add days, it will adjust the month accordingly if needed
        // We need to handle work week (skipping weekends) need to handle weekends
        LocalDateTime result = submitDateTime.plusWeeks(workWeeks).plusDays(daysRemainder);
        skipWeekends(result);

        // 14:12 + 2 -> 16:12               17:00 - 14:12 = 2 fullHoursTillEndOfWorkDay.   Just add 2 to 14:12 -> 16:12
        // 14:12 + 3 -> +1 day 9:12 =       17:00 - 14:12 = 2 fullHoursTillEndOfWorkDay    2 < 3   +1 day withHours (9 + )
        int fullHoursTillEndOfWorkDay = HOUR_END_WORK_DAY - (result.getHour() + (result.getMinute() == 0 ? 0 : 1));

        if (fullHoursTillEndOfWorkDay >= hoursRemainder) {
            result = result.plusHours(hoursRemainder);
        } else {
            int nextDayHours = hoursRemainder - fullHoursTillEndOfWorkDay - 1;
            result = result.plusDays(1).withHour(HOUR_START_WORK_DAY + nextDayHours);
        }
        result = skipWeekends(result);
        return result;
    }

    private static LocalDateTime skipWeekends(LocalDateTime result) {
        if (DayOfWeek.SATURDAY == result.getDayOfWeek()) {
            result = result.plusDays(2);
        }
        if (DayOfWeek.SUNDAY == result.getDayOfWeek()) {
            result = result.plusDays(1);
        }
        return result;
    }

    private void checkBusinessHours(LocalDateTime submitDateTime) {
        LocalTime time = submitDateTime.toLocalTime();
        if (time.isBefore(START_OF_DAY)) {
            throw new IllegalArgumentException("Requests are not accepted before 9:00AM");
        }
        if (time.isAfter(END_OF_DAY)) {
            throw new IllegalArgumentException("Requests are not accepted after 5:00 PM");
        }
    }

    private static void checkOnWorkDay(LocalDateTime submitDateTime) {
        if (WEEKEND.contains(submitDateTime.getDayOfWeek())) {
            throw new IllegalArgumentException("Cannot submit on weekend");
        }
    }

    private static void checkRequiredArgs(LocalDateTime submitDateTime, int turnaroundHours) {
        if (submitDateTime == null) {
            throw new IllegalArgumentException("Required parameters: submitDateTime");
        }
        if (turnaroundHours <= 0) {
            throw new IllegalArgumentException("Required parameters: turnaroundHours");
        }
    }
}
