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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper class to select the appropriate DateTimeFormatter for a given formatted date/time string
 * <p>
 * The DateTimeFormat also includes the correct Temporal class to use (LocalDate, LocalDateTime, OffsetDateTime,
 * Instant) in association with a given date/time type.
 */
public class DateTimeFormat {

    private static final Logger LOG = LoggerFactory.getLogger(DateTimeFormat.class);

    private static final Map<String, DateTimeFormat> FORMAT_MAP = new HashMap<>();

    static {
        /*
         * Register formatter classes together with their masks
         * @See https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
         */
        addFormat("YYYYMMDD", DateTimeFormatter.BASIC_ISO_DATE, LocalDate.class, "BASIC_ISO_DATE", "20111203");
        addFormat("YYYY-MM-DD", DateTimeFormatter.ISO_LOCAL_DATE, LocalDate.class, "ISO_LOCAL_DATE", "2011-12-03");
        addFormat("YYYY-MM-DDThh:mm:ss", DateTimeFormatter.ISO_LOCAL_DATE_TIME, LocalDateTime.class,
                "ISO_LOCAL_DATE_TIME", "2011-12-03T10:15:30");
        addFormat("YYYY-MM-DDThh:mm:ss-hh:mm", DateTimeFormatter.ISO_OFFSET_DATE_TIME, OffsetDateTime.class,
                "ISO_OFFSET_DATE_TIME", "2011-12-03T10:15:30-05:00");
        addFormat("YYYY-MM-DDThh:mm:ssZ", DateTimeFormatter.ISO_INSTANT, Instant.class, "ISO_INSTANT",
                "2011-12-03T10:15:30Z");
        addFormat("YYYY-MM-DDThh:mm:ss.uuuZ", DateTimeFormatter.ISO_INSTANT, Instant.class, "ISO_INSTANT",
                "2011-12-03T10:15:30.000Z");
    }

    private String format;
    private String mask;
    private DateTimeFormatter dateTimeFormatter;
    private Class<? extends Temporal> temporalClass;
    private Method fromMethod;
    private String description;
    private String example;

    public DateTimeFormat(String format, DateTimeFormatter dateTimeFormatter, Class<? extends Temporal> temporalClass,
                          String description, String example) {
        this.format = format;
        this.mask = digitMask(format);
        this.dateTimeFormatter = dateTimeFormatter;
        this.temporalClass = temporalClass;
        this.description = description;
        this.example = example;

        // Cache the from method of LocalDate, LocalDateTime or Instant to build the Temporal
        try {
            this.fromMethod = this.temporalClass.getDeclaredMethod("from", TemporalAccessor.class);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("Temporal Class " + Common.safeToName(temporalClass) +
                    " does not implement the 'from' method");
        }
    }

    /**
     * Parse a string into a Temporal (e.g., LocalDate etc.)
     *
     * @param value text to be parsed
     * @return the Temporal
     */
    public static Temporal parse(String value) {
        Temporal result = null;
        DateTimeFormat dtf = null;
        try {
            // Deduce the DateTimeFormat and attempt to parse the value into a TemporalAccessor
            dtf = getFormat(value);
            TemporalAccessor accessor = dtf.getDateTimeFormatter().parse(value);
            // Use the from method of LocalDate, LocalDateTime or Instant to build the Temporal
            result = (Temporal) dtf.fromMethod.invoke(null, accessor);
        } catch (IllegalAccessException ex) {
            Common.fatalException(ex, LOG, "Unable to invoke from() on {}", Common.safeToName(dtf.getTemporalClass()));
        } catch (InvocationTargetException | DateTimeException ex) {
            String description = (dtf != null) ? dtf.getDescription() : "null";
            Class temporalClass = (dtf != null) ? dtf.getTemporalClass() : null;
            throw new IllegalArgumentException(Common.concatenate("Unable to parse [", value, "] using [",
                    description, "] into [", Common.safeToName(temporalClass), "]: ",
                    ex.getCause().getMessage()));
        }
        //LOG.debug("Converted {} to {}", value, result);
        return result;
    }

    /**
     * Get the DateTimeFormat that corresponds to the formatted date/time string
     *
     * @param text the formatted date/time string (e.g., 2017-05-02)
     * @return the matching DateTimeFormat or null
     */
    public static DateTimeFormat getFormat(String text) {
        String masked = digitMask(text);
        DateTimeFormat format = FORMAT_MAP.get(masked);
        if (format == null) {
            throw new IllegalArgumentException(
                    Common.concatenate("Could not match ", masked, " in FORMAT_MAPS, original text ", text));
        }
        return format;
    }

    private static void addFormat(String format, DateTimeFormatter dateTimeFormatter,
                                  Class<? extends Temporal> temporalClass, String description, String example) {
        DateTimeFormat dtf = new DateTimeFormat(format, dateTimeFormatter, temporalClass, description, example);
        FORMAT_MAP.put(dtf.getMask(), dtf);
    }

    private static String digitMask(String text) {
        final String DIGIT_MASK = "n";
        final String DIGIT_CHARS = "YMDhmsu";

        return text.chars().mapToObj(c -> (char) c)
                .map(c -> ((c == '+') ? '-' : c)) // map plus (+) to minus (-)
                .map(c -> DIGIT_CHARS.indexOf(c) >= 0 || Character.isDigit(c) ? DIGIT_MASK : String.valueOf(c))
                .collect(Collectors.joining());
    }

    public String getFormat() {
        return format;
    }

    public String getMask() {
        return mask;
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public Class<? extends Temporal> getTemporalClass() {
        return temporalClass;
    }

    public String getDescription() {
        return description;
    }

    public String getExample() {
        return example;
    }

    public boolean hasTime() {
        final String TIME = "hh:mm:ss";
        return this.format.contains(TIME);
    }

    public boolean isNormalized() {
        final String ISO_NORMALIZED_SUFFIX = "Z";
        return this.format.endsWith(ISO_NORMALIZED_SUFFIX);
    }

    @Override
    public String toString() {
        return "DateTimeFormat{" + "format='" + format + '\'' +
                ", mask='" + mask + '\'' +
                ", dateTimeFormatter=" + dateTimeFormatter +
                ", temporalClass=" + Common.safeToName(temporalClass) +
                ", description='" + description + '\'' +
                ", example='" + example + '\'' +
                '}';
    }
}
