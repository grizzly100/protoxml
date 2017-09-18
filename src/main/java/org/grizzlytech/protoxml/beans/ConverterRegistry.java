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

package org.grizzlytech.protoxml.beans;


import org.grizzlytech.protoxml.beans.converters.DateToDateConverter;
import org.grizzlytech.protoxml.util.ClassUtil;
import org.grizzlytech.protoxml.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Objects;

/**
 * Registry of converter classes that can convert a value object to one type to another
 *
 * @See BeanImpl#setValue
 */
public class ConverterRegistry {

    public static final ConverterRegistry instance = new ConverterRegistry();
    private static final Logger LOG = LoggerFactory.getLogger(ConverterRegistry.class);

    static {
        getInstance().registerAll();
    }

    public final HashMap<String, Converter> converters;

    public ConverterRegistry() {
        this.converters = new HashMap<>();
    }

    public static ConverterRegistry getInstance() {
        return instance;
    }

    public static String toKey(Class from, Class to) {
        return Common.safeToName(from) + "-to-" + Common.safeToName(to);
    }

    public ConverterRegistry register(Converter converter) {
        String name = converter.key();
        this.converters.put(name, converter);
        return this;
    }

    public void registerAll() {
        ClassUtil.getImplementations("org.grizzlytech.protoxml.beans.converters",
                x -> x.endsWith("Converter"),
                Converter.class
        ).stream()
                .map(x -> (Converter) ClassUtil.newInstanceElseNull(x))
                .filter(Objects::nonNull)
                .forEach(this::register);
    }

    public Converter findConverter(String key) {
        return this.converters.get(key);
    }

    public Converter findConverter(Class fromClass, Class toClass) {
        String key = toKey(fromClass, toClass);

        // Check regular x-to-y converters
        Converter converter = findConverter(key);

        // Check special cases
        if (converter == null && isUnboxing(fromClass, toClass)) {
            converter = findConverter("NOP"); //e.g., java.lang.Double to double
        }

        if (converter == null && toClass == String.class) {
            converter = findConverter("ObjectToString");
        }

        if (converter == null && fromClass == String.class && Number.class.isAssignableFrom(toClass)) {
            converter = findConverter("StringToNumber"); //e.g., String to Double
        }

        if (converter == null && fromClass == String.class && toClass.isPrimitive()) {
            converter = findConverter("StringToPrimitive"); //e.g., String to double
        }

        if (converter == null && fromClass == String.class && DateToDateConverter.isDateClass(toClass)) {
            converter = findConverter("StringToDate");
        }

        if (converter == null && DateToDateConverter.isDateClass(fromClass)
                && DateToDateConverter.isDateClass(toClass)) {
            converter = findConverter("DateToDate");
        }
        return converter;
    }

    public Object convert(Class<?> toClass, Object value, String path) {
        if (toClass.isAssignableFrom(value.getClass())) {
            // value can be assigned to type (i.e., is a subclass / implements same interface)
            return value;
        }

        Converter converter = findConverter(value.getClass(), toClass);
        if (converter != null) {
            //LOG.debug("Using converter [{}] for [{}]", converter.getClass(), path);
            return converter.convert(value, toClass);
        } else {
            String message = "Unable to find a converter for " + toKey(value.getClass(), toClass);
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int reg = 0;

        builder.append("{");
        for (String key : this.converters.keySet()) {
            if (reg++ > 0) {
                builder.append(",");
            }
            builder.append(key);
        }
        builder.append("}");

        return builder.toString();
    }

    // java.lang.Double-to-double
    // java.lang.Integer-to-int
    private boolean isUnboxing(Class from, Class to) {
        return
                ClassUtil.isCoreJavaClass(from) &&
                        from.getSimpleName().toLowerCase().startsWith(to.getCanonicalName());
    }
}
