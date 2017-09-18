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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * See "JAXB Mapping of XML Schema Built-in Data Types"
 */
public class StringToNumberConverter implements Converter {

    private static final Logger LOG = LoggerFactory.getLogger(StringToNumberConverter.class);

    public Object convert(Object value, Class toClass) {
        Object result = null;
        String fromClassName = value.getClass().getCanonicalName();
        String input = (String) value;

        switch (toClass.getCanonicalName()) {
            // xs:integer
            case "java.math.BigInteger":
                result = new BigInteger(input);
                break;

            // xs:decimal
            case "java.math.BigDecimal":
                result = new BigDecimal(input);
                break;

            default:
                LOG.error("Could not convert {} of class {} into {}", value, fromClassName,
                        toClass.getCanonicalName());

        }

        return result;
    }

    @Override
    public Class getFromClass() {
        return String.class;
    }

    @Override
    public Class getToClass() {
        return Number.class;
    }

    public String key() {
        return "StringToNumber";
    }

}
