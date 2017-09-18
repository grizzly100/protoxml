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
import org.grizzlytech.protoxml.util.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.Temporal;

/**
 * See "JAXB Mapping of XML Schema Built-in Data Types"
 */
public class StringToDateConverter implements Converter {

    private static final Logger LOG = LoggerFactory.getLogger(StringToDateConverter.class);

    private final DateToDateConverter dateToDateConverter = new DateToDateConverter();

    public Object convert(Object value, Class toClass) {
        Object result = null;
        Temporal temporal = DateTimeFormat.parse((String) value);

        if (temporal != null) {
            if (toClass.equals(temporal.getClass())) {
                result = temporal;
            } else {
                // Leverage the DateToDateConverter for transformations between date/time containers
                result = dateToDateConverter.convert(temporal, toClass);
            }
        }
        return result;
    }

    @Override
    public Class getFromClass() {
        return String.class;
    }

    @Override
    public Class getToClass() {
        return Temporal.class;
    }

    public String key() {
        return "StringToDate";
    }
}
