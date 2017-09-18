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

package org.grizzlytech.protoxml.beans.converters;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DateToDateConverterTest {

    private static Logger LOG = LoggerFactory.getLogger(DateToDateConverterTest.class);

    final String CREATION_DATE_TIME = "2012-11-14T02:31:45.000Z";

    @Test
    public void convert() {
        DateToDateConverter converter = new DateToDateConverter();

        LocalDate value = LocalDate.now();
        XMLGregorianCalendar result = (XMLGregorianCalendar)
                converter.convert(value, XMLGregorianCalendar.class);

        StringToDateConverter converter2 = new StringToDateConverter();

        XMLGregorianCalendar calendar = (XMLGregorianCalendar) converter2.convert(CREATION_DATE_TIME,
                XMLGregorianCalendar.class);
        assertTrue(calendar.getDay() == 14);

        LocalDateTime date = (LocalDateTime) converter.convert(calendar, LocalDateTime.class);
        assertTrue(date.getDayOfMonth() == 14);
    }

    @Test
    public void getDateClass() {

        assertEquals(XMLGregorianCalendar.class, DateToDateConverter.getDateClass(
                com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl.class));

    }

}