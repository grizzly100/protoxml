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

import org.grizzlytech.protoxml.beans.converters.StringToPrimitiveConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Custom Adjuster to modify the date portion of Temporal objects
 * <p>
 * Surfaces the built-in adjusters available on TemporalAccessors and LocalDate, plus
 * allows registration of additional user defined adjusters
 */
public class DateAdjuster implements TemporalAdjuster {

    private static final Logger LOG = LoggerFactory.getLogger(DateAdjuster.class);

    private static final Map<String, Method> ADJUSTERS = new HashMap<>();

    static {
        storeTemporalAdjusters();
    }

    /**
     * Name of adjuster to use
     */
    private Method adjuster;

    /**
     * (Boxed) Primitive or Enum parameter
     */
    private Object parameter;

    public DateAdjuster() {
    }

    private static void storeTemporalAdjusters() {

        Arrays.stream(TemporalAdjusters.class.getDeclaredMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers())) // static methods only
                .filter(m -> m.getParameterCount() <= 1) // with zero or one parameters
                .filter(m -> m.getParameterCount() == 0 || // where parameter must be a primitive or enum
                        (m.getParameterTypes()[0].isPrimitive() || m.getParameterTypes()[0].isEnum()))
                .filter(m -> TemporalAdjuster.class.equals(m.getReturnType())) // and return a TemporalAdjuster
                .forEach(m -> ADJUSTERS.put(m.getName().toUpperCase(), m));

        Arrays.stream(LocalDate.class.getDeclaredMethods())
                .filter(m -> !Modifier.isStatic(m.getModifiers())) // non-static methods only
                .filter(m -> m.getParameterCount() <= 1) // with zero or one parameters
                .filter(m -> m.getParameterCount() == 0 || // where parameter must be a primitive or enum
                        (m.getParameterTypes()[0].isPrimitive() || m.getParameterTypes()[0].isEnum()))
                .filter(m -> LocalDate.class.equals(m.getReturnType())) // and return a LocalDate
                .forEach(m -> ADJUSTERS.put(m.getName().toUpperCase(), m));
    }

    /**
     * Register an additional method which returns a TemporalAdjusters
     *
     * @param m a method with signature "public static TemporalAdjuster <method>(<optional> primitive/enum param)"
     */
    public static void registerTemporalAdjuster(Method m) {
        Common.argumentAssertion(Modifier.isStatic(m.getModifiers()), LOG, "Only static methods supported");
        Common.argumentAssertion(m.getParameterCount() <= 1, LOG, "Only 0 or 1 parameters supported");
        Common.argumentAssertion(m.getParameterCount() == 0 || (m.getParameterTypes()[0].isPrimitive() ||
                m.getParameterTypes()[0].isEnum()), LOG, "Only primitive or enum parameter supported");
        Common.argumentAssertion(TemporalAdjuster.class.equals(m.getReturnType()), LOG,
                "Method does not return a TemporalAdjuster");

        ADJUSTERS.put(m.getName().toUpperCase(), m);
    }

    public static String listAdjusters() {
        List<String> methodNames = ADJUSTERS.values().stream()
                .map(Method::getName).sorted().collect(Collectors.toCollection(ArrayList::new));
        return Common.listToString(methodNames, ", ");
    }

    public String getAdjusterName() {
        return Common.safeToName(this.adjuster);
    }

    public void setAdjusterName(String name) {
        Method method = ADJUSTERS.get(name.toUpperCase());
        if (method != null) {
            this.adjuster = method;
        } else {
            throw new IllegalArgumentException(Common.concatenate("Unknown adjusterName [", name, "]"));
        }
    }

    public int getParameterCount() {
        return (this.adjuster != null) ? this.adjuster.getParameterCount() : -1;
    }

    public Class getParameterClass() {
        return (getParameterCount() == 1) ? this.adjuster.getParameterTypes()[0] : null;
    }

    public Object getParameter() {
        return this.parameter;
    }

    /**
     * @param value only primitives and enum constants are allowed
     */
    public void setParameter(Object value) {
        assert this.adjuster != null;

        // Guard parameter setting
        if (getParameterCount() == 0) {
            throw new IllegalArgumentException(Common.concatenate("Cannot set parameter for Adjuster [",
                    getAdjusterName(), "] as it takes zero parameters"));
        } else if (!(getParameterClass().isEnum() || getParameterClass().isPrimitive())) {
            throw new IllegalArgumentException(Common.concatenate("Cannot set parameter for Adjuster [",
                    getAdjusterName(), "] due to TYPE MISMATCH. Expected [", Common.safeToName(getParameterClass()),
                    "] but was provided [", Common.safeToName(value), "]"));
        }

        // Convert string if necessary
        if (value != null && value instanceof String) {
            if (getParameterClass().isEnum()) {
                // Attempt to lookup the enum value
                //noinspection unchecked
                value = Enum.valueOf(getParameterClass(), (String) value);
            } else if (!String.class.equals(getParameterClass())) {
                // Attempt to convert the String to a long, int etc.
                value = (new StringToPrimitiveConverter()).convert(value, getParameterClass());
            }
        }

        // Set the value
        this.parameter = value;
    }

    @Override
    public Temporal adjustInto(Temporal temporal) {
        if (temporal instanceof LocalDate) {
            return adjustDate((LocalDate) temporal);
        } else if (temporal instanceof Instant) {
            // Need to convert Instance to a UTC LocalDateTime, to enable date based adjustment
            temporal = LocalDateTime.ofInstant((Instant) temporal, ZoneOffset.UTC);
            temporal = temporal.with(adjustDate(toLocalDate(temporal)));
            return ((LocalDateTime) temporal).toInstant(ZoneOffset.UTC);
        } else {
            return temporal.with(adjustDate(toLocalDate(temporal)));
        }
    }

    private LocalDate adjustDate(LocalDate date) {
        LocalDate result = null;
        String plan = Common.concatenate("adjust ", Common.safeToName(date),
                " with adjuster ", Common.safeToName(this.adjuster),
                "(", (getParameterCount() == 1) ? Common.safeToName(getParameter()) + ":" +
                        Common.safeToString(getParameter()) : "", ")");
        // Get the adjuster
        try {
            if (TemporalAdjuster.class.equals(this.adjuster.getReturnType())) {
                // Retrieve the Temporal Adjuster and apply it
                TemporalAdjuster theAdjuster = (TemporalAdjuster) ((getParameterCount() == 0) ?
                        this.adjuster.invoke(null) : this.adjuster.invoke(null, getParameter()));
                result = date.with(theAdjuster);
            } else if (LocalDate.class.equals(this.adjuster.getDeclaringClass())) {
                // Invoke the LocalDate method
                result = (LocalDate) ((getParameterCount() == 0) ?
                        this.adjuster.invoke(date) : this.adjuster.invoke(date, getParameter()));
            } else {
                throw new IllegalArgumentException(Common.concatenate("Unable to ", plan));
            }
        } catch (IllegalAccessException | InvocationTargetException ex) {
            Throwable thr = (ex instanceof InvocationTargetException) ? ex.getCause() : ex;
            LOG.error(Common.concatenate("Exception [", ex.getMessage(), "] trying to ", plan));
        }
        return result;
    }

    private LocalDate toLocalDate(Temporal temporal) {
        return LocalDate.of(temporal.get(ChronoField.YEAR), temporal.get(ChronoField.MONTH_OF_YEAR),
                temporal.get(ChronoField.DAY_OF_WEEK));
    }
}
