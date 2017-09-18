package org.grizzlytech.protoxml.beans.converters;


import org.grizzlytech.protoxml.util.DateTimeFormat;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;

import static org.junit.Assert.assertTrue;

public class StringToDateConverterTest {

    private static Logger LOG = LoggerFactory.getLogger(StringToDateConverterTest.class);

    final String CREATION_DATE_TIME = "2012-11-14T02:30:00.000Z";
    final String CREATION_DATE_TIME2 = "2012-11-14T02:30:45";
    final String CREATION_DATE_TIME3 = "2012-11-14T02:30:45+05:00";
    final String CREATION_DATE = "2016-01-25";

    @Test
    public void parse() {
        DateTimeFormat.parse(CREATION_DATE);
        DateTimeFormat.parse(CREATION_DATE_TIME);
        DateTimeFormat.parse(CREATION_DATE_TIME2);
        DateTimeFormat.parse(CREATION_DATE_TIME3);
    }

    /**
     * In this variant the Converter needs to determine whether to create a date or a time
     */
    @Test
    public void convert() {
        StringToDateConverter converter = new StringToDateConverter();
        XMLGregorianCalendar calendar1 = (XMLGregorianCalendar) converter.convert(CREATION_DATE, XMLGregorianCalendar.class);
        assertTrue(calendar1.getDay() == 25);

        XMLGregorianCalendar calendar2 = (XMLGregorianCalendar) converter.convert(CREATION_DATE_TIME, XMLGregorianCalendar.class);
        assertTrue(calendar2.getDay() == 14);
   }

}
