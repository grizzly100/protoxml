/*
 * The MIT License
 *
 * Copyright (c) 2017, GrizzlyTech.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.grizzlytech.protoxml.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;

public class DateAdjusterTest {
    private static final Logger LOG = LoggerFactory.getLogger(DateAdjusterTest.class);

    public static TemporalAdjuster nextMonday() {
        return (Temporal t) -> {
            do {
                t = t.plus(1, ChronoUnit.DAYS);
            } while (t.getLong(ChronoField.DAY_OF_WEEK) != DayOfWeek.MONDAY.getValue());
            return t;
        };
    }

    public static TemporalAdjuster nextDOW(DayOfWeek dow) {
        return (Temporal t) -> {
            do {
                t = t.plus(1, ChronoUnit.DAYS);
            } while (t.getLong(ChronoField.DAY_OF_WEEK) != dow.getValue());
            return t;
        };
    }

    @Test
    public void builtInAdjustments() {
        DateAdjuster adjuster = new DateAdjuster();
        LocalDate date = LocalDate.of(2017, 1, 1);
        LOG.info("{}", date);

        adjuster.setAdjusterName("lastDayOfMonth");
        date = date.with(adjuster);
        LOG.info("{}", date);

        adjuster.setAdjusterName("firstInMonth");
        adjuster.setParameter(DayOfWeek.FRIDAY);
        date = date.with(adjuster);
        LOG.info("{}", date);

        adjuster.setAdjusterName("firstInMonth");
        adjuster.setParameter("SUNDAY");
        date = date.with(adjuster);
        LOG.info("{}", date);

        adjuster.setAdjusterName("plusMonths");
        adjuster.setParameter("-4");
        date = date.with(adjuster);
        LOG.info("{}", date);
    }

    @Test
    public void customAdjustments()
            throws NoSuchMethodException {
        DateAdjuster.registerTemporalAdjuster(getClass().getDeclaredMethod("nextMonday"));
        DateAdjuster adjuster = new DateAdjuster();
        LocalDate date = LocalDate.of(2017, 6, 21);

        adjuster.setAdjusterName("nextMonday");
        date = date.with(adjuster);
        LOG.info("nextMonday: {}", date);

        DateAdjuster.registerTemporalAdjuster(getClass().getDeclaredMethod("nextDOW", DayOfWeek.class));
        adjuster.setAdjusterName("nextDOW");
        adjuster.setParameter(DayOfWeek.valueOf("WEDNESDAY"));
        date = date.with(adjuster);
        LOG.info("nextDOW: {}", date);
    }

}