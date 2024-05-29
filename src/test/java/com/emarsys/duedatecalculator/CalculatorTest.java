package com.emarsys.duedatecalculator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;

public class CalculatorTest {

    private Calculator calc = new Calculator();

    @Test
    void submitDateTimeRequired() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> calc.calculateDueDate(null, 1));
    }

    @Test
    void turnaroundTimeRequired() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> calc.calculateDueDate(LocalDateTime.of(2024, Month.MAY, 29, 16, 0), 0)
        );
    }

    @Test
    void turnaroundTimeNonNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> calc.calculateDueDate(LocalDateTime.of(2024, Month.MAY, 29, 16, 0), -1));
    }

    @Test
    void whenReportAfterWorkingHoursThenThrowException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> calc.calculateDueDate(LocalDateTime.of(2024, Month.MAY, 29, 17, 1), 1)
        );
    }

    @Test
    void whenReportBeforeWorkingHoursThenThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                calc.calculateDueDate(LocalDateTime.of(2024, Month.MAY, 29, 8, 59), 1));
    }

    @Test
    void whenReportOnSundayThenThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                calc.calculateDueDate(LocalDateTime.of(2024, Month.MAY, 26, 9, 0), 1)
        );
    }

    @Test
    void whenReportOnSaturdayThenThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            calc.calculateDueDate(LocalDateTime.of(2024, Month.MAY, 25, 9, 0), 1);
        });
    }

    @Test
    void testMainCaseWith2Days() {
        // -- given --
        //turnaround time is 16 hours
        int turnaround = 16;
        // Tue, May 28 at 2:12PM
        LocalDateTime reportedAt = LocalDateTime.of(2024, Month.MAY, 28, 16, 12);
        // -- when
        LocalDateTime dueDateTime = calc.calculateDueDate(reportedAt, turnaround);
        // -- assert
        Assertions.assertEquals(2024, dueDateTime.getYear());
        Assertions.assertEquals(Month.MAY, dueDateTime.getMonth());
        Assertions.assertEquals(30, dueDateTime.getDayOfMonth());
        Assertions.assertEquals(16, dueDateTime.getHour());
        Assertions.assertEquals(12, dueDateTime.getMinute());
    }

    @Test
    void testWith14hrs() {
        // -- given --
        int turnaround = 14;
        // Tue, May 28 at 2:12PM
        LocalDateTime reportedAt = LocalDateTime.of(2024, Month.MAY, 28, 14, 12);
        // -- when
        LocalDateTime dueDateTime = calc.calculateDueDate(reportedAt, turnaround);
        // -- assert
        Assertions.assertEquals(2024, dueDateTime.getYear());
        Assertions.assertEquals(Month.MAY, dueDateTime.getMonth());
        Assertions.assertEquals(30, dueDateTime.getDayOfMonth());
        Assertions.assertEquals(12, dueDateTime.getHour());
        Assertions.assertEquals(12, dueDateTime.getMinute());
    }

    @Test
    void testWith2Hours() {
        // -- given --
        //turnaround time is 2 hours
        int turnaround = 2;
        // Tue, May 28 at 2:12PM
        LocalDateTime reportedAt = LocalDateTime.of(2024, Month.MAY, 28, 14, 12);
        // -- when
        LocalDateTime dueDateTime = calc.calculateDueDate(reportedAt, turnaround);
        // -- assert
        Assertions.assertEquals(2024, dueDateTime.getYear());
        Assertions.assertEquals(Month.MAY, dueDateTime.getMonth());
        Assertions.assertEquals(28, dueDateTime.getDayOfMonth());
        Assertions.assertEquals(16, dueDateTime.getHour());
        Assertions.assertEquals(12, dueDateTime.getMinute());
    }

    @Test
    void testWith3HoursSpillOverToNextDay() {
        // -- given --
        //turnaround time is 2 hours
        int turnaround = 3;
        // Tue, May 28 at 2:12PM
        LocalDateTime reportedAt = LocalDateTime.of(2024, Month.MAY, 28, 14, 12);
        // -- when
        LocalDateTime dueDateTime = calc.calculateDueDate(reportedAt, turnaround);
        // -- assert
        Assertions.assertEquals(2024, dueDateTime.getYear());
        Assertions.assertEquals(Month.MAY, dueDateTime.getMonth());
        Assertions.assertEquals(29, dueDateTime.getDayOfMonth());
        Assertions.assertEquals(9, dueDateTime.getHour());
        Assertions.assertEquals(12, dueDateTime.getMinute());
    }

    @Test
    void testWith1Day() {
        // -- given --
        //turnaround time is 16 hours
        int turnaround = 8;
        // Tue, May 28 at 2:12PM
        LocalDateTime reportedAt = LocalDateTime.of(2024, Month.MAY, 28, 12, 12);
        // -- when
        LocalDateTime dueDateTime = calc.calculateDueDate(reportedAt, turnaround);
        // -- assert
        Assertions.assertEquals(2024, dueDateTime.getYear());
        Assertions.assertEquals(Month.MAY, dueDateTime.getMonth());
        Assertions.assertEquals(29, dueDateTime.getDayOfMonth());
        Assertions.assertEquals(12, dueDateTime.getHour());
        Assertions.assertEquals(12, dueDateTime.getMinute());
    }

    @Test
    void testWith3Day() {
        // -- given --
        //turnaround time is 16 hours
        int turnaround = 8 * 3;
        // Tue, May 28 at 2:12PM
        LocalDateTime reportedAt = LocalDateTime.of(2024, Month.MAY, 28, 12, 12);
        // -- when
        LocalDateTime dueDateTime = calc.calculateDueDate(reportedAt, turnaround);
        // -- assert
        Assertions.assertEquals(2024, dueDateTime.getYear());
        Assertions.assertEquals(Month.MAY, dueDateTime.getMonth());
        Assertions.assertEquals(31, dueDateTime.getDayOfMonth());
        Assertions.assertEquals(12, dueDateTime.getHour());
        Assertions.assertEquals(12, dueDateTime.getMinute());
    }

    @Test
    void testWith4DaysOnSaturdayShouldBecomeNextMonday() {
        // -- given --
        //turnaround time is 16 hours
        int turnaround = 8 * 4;
        // Tue, May 28 at 2:12PM
        LocalDateTime reportedAt = LocalDateTime.of(2024, Month.MAY, 28, 12, 12);
        // -- when
        LocalDateTime dueDateTime = calc.calculateDueDate(reportedAt, turnaround);
        // -- assert
        Assertions.assertEquals(2024, dueDateTime.getYear());
        Assertions.assertEquals(Month.JUNE, dueDateTime.getMonth());
        // Monday
        Assertions.assertEquals(DayOfWeek.MONDAY, dueDateTime.getDayOfWeek());
        Assertions.assertEquals(3, dueDateTime.getDayOfMonth());
        Assertions.assertEquals(12, dueDateTime.getHour());
        Assertions.assertEquals(12, dueDateTime.getMinute());
    }


    @Test
    void testWithWorkWeek() {
        // -- given --
        //turnaround time is 16 hours
        int turnaround = 8 * 5;
        // Tue, May 28 at 2:12PM
        LocalDateTime reportedAt = LocalDateTime.of(2024, Month.MAY, 28, 12, 12);
        // -- when
        LocalDateTime dueDateTime = calc.calculateDueDate(reportedAt, turnaround);
        // -- assert
        Assertions.assertEquals(2024, dueDateTime.getYear());
        Assertions.assertEquals(Month.JUNE, dueDateTime.getMonth());
        Assertions.assertEquals(4, dueDateTime.getDayOfMonth());
        Assertions.assertEquals(12, dueDateTime.getHour());
        Assertions.assertEquals(12, dueDateTime.getMinute());
    }
}
