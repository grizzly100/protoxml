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

package org.grizzlytech.protoxml.builder.functions;

import org.grizzlytech.protoxml.beans.Bean;
import org.grizzlytech.protoxml.beans.BeanImpl;
import org.grizzlytech.protoxml.util.DateAdjuster;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testdomain.zoo.Penguin;

import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DatesTest {
    private static final Logger LOG = LoggerFactory.getLogger(DatesTest.class);

    @Test
    public void parseDate() throws Exception {
        assertEquals("2017-10-05", Dates.parseDate(null, null, "2017-10-05").toString());
    }

    @Test
    public void parseNamed() throws Exception {
        assertEquals(LocalDate.now(), Dates.parseDate(null, null, "today"));

        assertTrue(Math.abs(LocalDateTime.now().get(ChronoField.MILLI_OF_SECOND) -
                Dates.parseDate(null, null, "now").get(ChronoField.MILLI_OF_SECOND)) < 10);

        assertTrue(Math.abs(Instant.now().get(ChronoField.MILLI_OF_SECOND) -
                Dates.parseDate(null, null, "nowUTC").get(ChronoField.MILLI_OF_SECOND)) < 10);

        assertEquals(OffsetDateTime.now(ZoneId.systemDefault()).get(ChronoField.HOUR_OF_DAY),
                Dates.parseDate(null, null, "nowSystem").get(ChronoField.HOUR_OF_DAY));

        // Verify offsetting works vs UTC TODO: WHY is this broken at 1:45am BST?
 //       assertEquals(OffsetDateTime.now(ZoneOffset.UTC).get(ChronoField.HOUR_OF_DAY) - 5,
   //             Dates.parseDate(null, null, "nowOffset-05:00").get(ChronoField.HOUR_OF_DAY));
    }

    @Test
    public void adjust() throws Exception {
        LOG.info("{}", DateAdjuster.listAdjusters());
        LocalDate date = (LocalDate) Dates.adjust(null, null, new String[]{
                "2017-01-01", "lastDayOfMonth", "plusDays", "4", "lastInMonth", "WEDNESDAY"
        });
        assertEquals("2017-02-22", date.toString());
    }

    @Test
    public void adjustDeanDate() throws Exception {
        Bean bean = new BeanImpl(new Penguin());
        // First set a date only
        bean.setPathValue("dateOfBirth", "2015-03-12");
        Temporal date = Dates.adjust(bean, "tbd", new String[]{
                "DateOfBirth", "lastDayOfMonth", "plusDays", "4"
        });
        assertEquals("2015-04-04", date.toString());

        // Now try a date/time - the date should be adjusted, not the time
        bean.setPathValue("dateOfBirth", "2015-03-12T00:51:01.076Z");
        Temporal date2 = Dates.adjust(bean, "tbd", new String[]{
                "DateOfBirth", "lastDayOfMonth", "plusDays", "4"
        });
        assertEquals("2015-03-06T00:51:01.076Z", date2.toString());

        // Now try a date/time - the date should be adjusted, not the time
        bean.setPathValue("dateOfBirth", "2015-03-12T00:51:01");
        Temporal date3 = Dates.adjust(bean, "tbd", new String[]{
                "DateOfBirth", "lastDayOfMonth", "plusDays", "4"
        });
        assertEquals("2015-03-06T00:51:01", date3.toString());

        // Now try a date/time - the date should be adjusted, not the time
        bean.setPathValue("dateOfBirth", "2015-03-12T00:51:01+01:00");
        Temporal date4 = Dates.adjust(bean, "tbd", new String[]{
                "DateOfBirth", "lastDayOfMonth", "plusDays", "4"
        });
        assertEquals("2015-03-06T00:51:01+01:00", date4.toString());

    }
}