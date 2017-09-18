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

import org.grizzlytech.protoxml.beans.Converter;
import org.grizzlytech.protoxml.beans.ConverterRegistry;
import org.grizzlytech.protoxml.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Enable data type conversions between containers
 * <p>
 * Work in progress - many more to support
 */
public class DateToDateConverter implements Converter {
    private static final Logger LOG = LoggerFactory.getLogger(DateToDateConverter.class);

    public static boolean isDateClass(Class candidate) {
        return getDateClass(candidate) != null;
    }

    public static Class getDateClass(Class candidate) {
        final Class[] DATE_CLASSES = {XMLGregorianCalendar.class, LocalDate.class, LocalDateTime.class,
                OffsetDateTime.class, Instant.class,
                Temporal.class, Calendar.class, Date.class};
        for (Class clazz : DATE_CLASSES) {
            //noinspection unchecked
            if (clazz.isAssignableFrom(candidate)) return clazz;
        }
        return null;
    }

    public Object convert(Object value, Class toClass) {
        Object result = null;

        // The caller may request Temporal rather than a concrete date or datetime or instant
        // Determine which Temporal is the best fit
        if (Temporal.class.equals(toClass)) {
            toClass = equivalentTemporal(value);
        }
        String key = ConverterRegistry.toKey(getDateClass(value.getClass()), getDateClass(toClass));

        switch (key) {
            case "java.time.LocalDate-to-javax.xml.datatype.XMLGregorianCalendar":
                result = toXMLGregorianCalendar((LocalDate) value);
                break;

            case "java.time.LocalDateTime-to-javax.xml.datatype.XMLGregorianCalendar":
                result = toXMLGregorianCalendar((LocalDateTime) value);
                break;

            case "java.time.OffsetDateTime-to-javax.xml.datatype.XMLGregorianCalendar":
                result = toXMLGregorianCalendar((OffsetDateTime) value);
                break;

            case "java.time.Instant-to-javax.xml.datatype.XMLGregorianCalendar":
                result = toXMLGregorianCalendar((Instant) value);
                break;

            case "javax.xml.datatype.XMLGregorianCalendar-to-java.time.LocalDate":
                result = ((XMLGregorianCalendar) value).toGregorianCalendar().toZonedDateTime().toLocalDate();
                break;

            case "javax.xml.datatype.XMLGregorianCalendar-to-java.time.LocalDateTime":
                result = ((XMLGregorianCalendar) value).toGregorianCalendar().toZonedDateTime().toLocalDateTime();
                break;

            case "javax.xml.datatype.XMLGregorianCalendar-to-java.time.OffsetDateTime":
                result = ((XMLGregorianCalendar) value).toGregorianCalendar().toZonedDateTime().toOffsetDateTime();
                break;

            case "javax.xml.datatype.XMLGregorianCalendar-to-java.time.Instant":
                result = ((XMLGregorianCalendar) value).toGregorianCalendar().toZonedDateTime().toInstant();
                break;

            default:
                LOG.error("Unsupported date conversion request " + key);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Converted {}:{} to {}:{}", Common.safeToName(value), value,
                    Common.safeToName(result), result);
        }
        return result;
    }

    public Object toXMLGregorianCalendar(LocalDate date) {
        Object result;
        ZonedDateTime zdt = date.atStartOfDay(ZoneId.systemDefault());
        try {
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(
                    GregorianCalendar.from(zdt));
            suppressTimeFields(calendar);
            result = calendar;
        } catch (DatatypeConfigurationException ex) {
            throw new IllegalArgumentException(
                    Common.concatenate("Cannot convert ", date.toString(), " : ", ex.getMessage()));
        }
        return result;
    }

    public Object toXMLGregorianCalendar(LocalDateTime date) {
        Object result;
        ZonedDateTime zdt = ZonedDateTime.of(date, ZoneOffset.UTC);
        try {
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(
                    GregorianCalendar.from(zdt));
            calendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
            calendar.setMillisecond(0);
            result = calendar;
        } catch (DatatypeConfigurationException ex) {
            throw new IllegalArgumentException(
                    Common.concatenate("Cannot convert ", date.toString(), " : ", ex.getMessage()));
        }
        return result;
    }

    public Object toXMLGregorianCalendar(OffsetDateTime date) {
        Object result;
        ZonedDateTime zdt = ZonedDateTime.from(date);
        try {
            result = DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar(GregorianCalendar.from(zdt));
        } catch (DatatypeConfigurationException ex) {
            throw new IllegalArgumentException(
                    Common.concatenate("Cannot convert ", date.toString(), " : ", ex.getMessage()));
        }
        return result;
    }

    public Object toXMLGregorianCalendar(Instant instant) {
        Object result;
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
        try {
            result = DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar(GregorianCalendar.from(zdt));
        } catch (DatatypeConfigurationException ex) {
            throw new IllegalArgumentException(
                    Common.concatenate("Cannot convert ", instant.toString(), " : ", ex.getMessage()));
        }
        return result;
    }

    private void suppressTimeFields(XMLGregorianCalendar date) {
        date.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
        date.setTime(DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
                DatatypeConstants.FIELD_UNDEFINED);
    }

    @Override
    public Class getFromClass() {
        return Object.class;
    }

    @Override
    public Class getToClass() {
        return Object.class;
    }

    public String key() {
        return "DateToDate";
    }

    private Class<? extends Temporal> equivalentTemporal(Object value) {
        Class<? extends Temporal> result = null;
        if (XMLGregorianCalendar.class.equals(getDateClass(value.getClass()))) {
            result = equivalentTemporal((XMLGregorianCalendar) value);
        }
        LOG.debug("Best Temporal match for [{}] is [{}]", value.toString(), Common.safeToName(result));
        return result;
    }

    private Class<? extends Temporal> equivalentTemporal(XMLGregorianCalendar calendar) {
        if (calendar.getTimezone() != DatatypeConstants.FIELD_UNDEFINED) {
            return (calendar.getTimezone() == 0) ? Instant.class : OffsetDateTime.class;
        } else if (calendar.getHour() != DatatypeConstants.FIELD_UNDEFINED) {
            return LocalDateTime.class;
        }
        return LocalDate.class;
    }
}
