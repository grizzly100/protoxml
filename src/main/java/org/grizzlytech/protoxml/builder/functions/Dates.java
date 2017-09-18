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
import org.grizzlytech.protoxml.beans.PropertyException;
import org.grizzlytech.protoxml.util.Common;
import org.grizzlytech.protoxml.util.DateAdjuster;
import org.grizzlytech.protoxml.util.DateTimeFormat;
import org.grizzlytech.protoxml.util.NVP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.temporal.Temporal;

public class Dates {

    final static public String TODAY = "TODAY"; // LocalDate
    final static public String NOW = "NOW"; // LocalDateTime,
    final static public String NOW_UTC = "NOWUTC"; // Instant
    final static public String NOW_SYSTEM = "NOWSYSTEM"; // OffsetDateTime, with systemDefault
    final static public String NOW_OFFSET = "NOWOFFSET"; // OffsetDateTime, with user provided

    private static final Logger LOG = LoggerFactory.getLogger(Dates.class);

    /**
     * Today's date
     */
    public static LocalDate today(Bean bean, String path, String[] args) {
        return (LocalDate) parseNamedDate(TODAY);
    }

    /**
     * Date and time now, no timezone
     */
    public static LocalDateTime now(Bean bean, String path, String[] args) {
        return (LocalDateTime) parseNamedDate(NOW);
    }

    /**
     * Date and time now, UTC (normalised) time
     */
    public static Instant nowUTC(Bean bean, String path, String[] args) {
        return (Instant) parseNamedDate(NOW_UTC);
    }

    /**
     * Date and time now, including the default timezone offset
     */
    public static OffsetDateTime nowZoned(Bean bean, String path, String[] args) {
        return (OffsetDateTime) parseNamedDate(NOW_SYSTEM);
    }

    /**
     * Date and time now, including the provided timezone offset
     * "nowOffset-05:00"
     */
    public static OffsetDateTime nowOffset(Bean bean, String path, String[] args) {
        String offset = (args.length == 1 && Common.notEmpty(args[0])) ? args[0] : null;
        return (OffsetDateTime) ((offset != null) ? parseNamedDate(NOW_OFFSET + offset) : parseNamedDate(NOW_SYSTEM));
    }

    /**
     * Parse one of the named NOW_* names and execute the required temporal call
     */
    public static Temporal parseNamedDate(String arg) {
        Temporal result = null;

        // Extract the zone for NOW_OFFSET
        ZoneOffset zoneOffset = null;
        if (arg.toUpperCase().startsWith(NOW_OFFSET)) {
            zoneOffset = parseZoneOffset(arg.substring(NOW_OFFSET.length(), arg.length()));
            arg = (zoneOffset != null) ? NOW_OFFSET : NOW_SYSTEM; // use ZONED instead if failed to parse
        }

        switch (arg.toUpperCase()) {
            case TODAY:
                result = LocalDate.now();
                break;

            case NOW:
                result = LocalDateTime.now();
                break;

            case NOW_UTC:
                result = Instant.now(Clock.systemUTC());
                break;

            case NOW_SYSTEM:
                result = OffsetDateTime.now(ZoneId.systemDefault());
                break;

            case NOW_OFFSET:
                assert zoneOffset != null;
                result = OffsetDateTime.now(zoneOffset);
                break;
        }

        return result;
    }

    /**
     * Parse a string argument for a date/time
     * The string can contain either a date/time or a relative/absolute path for de-referencing
     */
    public static Temporal parseDate(Bean bean, String path, String arg)
            throws PropertyException {
        assert arg != null;
        Temporal date = null;
        String parseError = "";

        try {
            // Plan A: check for "named dates" (today, now, nowUTC, nowZoned, nowOffset)
            date = parseNamedDate(arg);
            // Plan B: parse text for a date
            if (date == null) {
                date = DateTimeFormat.parse(arg);
            }
        } catch (IllegalArgumentException | DateTimeException ex) {
            parseError = ex.getMessage();
        }

        // Plan C: check for path reference
        if (date == null) {
            String resolved = Base.resolvePath(path, arg);
            if (bean != null) {
                NVP<Temporal> contents = bean.getPathValue(resolved, Temporal.class);
                date = contents.getValue();
            }
        }

        if (date == null) {
            LOG.error("All parsing plans failed for [{}]: {}", arg, parseError);
        }
        return date;
    }

    /**
     * Arguments(date, ...[adjuster, parameter])
     */
    public static Temporal adjust(Bean bean, String path, String[] args)
            throws PropertyException {
        assert args != null && args.length >= 2;
        // Parse the date
        int i = 0;
        Temporal date = parseDate(bean, path, args[i++]);

        // Apply all adjusters
        while (date != null && i < args.length) {
            DateAdjuster adjuster = new DateAdjuster();
            String adjusterName = args[i++];
            adjuster.setAdjusterName(adjusterName);
            if (adjuster.getParameterCount() == 1) {
                Common.argumentAssertion(i < args.length, LOG, "Missing parameter for {}", adjuster.getAdjusterName());
                String parameter = args[i++];
                adjuster.setParameter(parameter);
            }
            Temporal adjusted = date.with(adjuster);
            LOG.debug("[{}] adjust with {}({}) from {} to {}", path, adjuster.getAdjusterName(), adjuster.getParameter(),
                    date, adjusted);
            date = adjusted;
        }

        return date;
    }

    private static ZoneOffset parseZoneOffset(String arg) {
        ZoneOffset result = null;
        try {
            if (Common.notEmpty(arg)) {
                result = ZoneOffset.of(arg);
            }
        } catch (DateTimeException ex) {
            LOG.error("Unable to parse ZoneOffset from [{}]: {}", arg, ex.getMessage());
        }

        return result;
    }
}
