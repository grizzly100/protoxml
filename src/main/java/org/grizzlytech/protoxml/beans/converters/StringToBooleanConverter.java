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

public class StringToBooleanConverter implements Converter {
    /**
     * Supports more options that the JRE (which only maps "true")
     *
     * @param input the text to parse
     * @return the Boolean value (on failure throws IllegalArgumentException)
     */
    static Boolean parseBoolean(String input) {
        Boolean result = null;
        switch (input.toUpperCase()) {
            case "TRUE":
            case "T":
            case "YES": // YesNoIndicator
            case "Y":
            case "PLUS": // PlusOrMinusIndicator
                result = Boolean.TRUE;
                break;

            case "FALSE":
            case "F":
            case "NO": // YesNoIndicator
            case "N":
            case "MINUS": // PlusOrMinusIndicator
                result = Boolean.FALSE;
                break;
        }
        if (result == null) {
            throw new IllegalArgumentException("Cannot convert [" + input + "] to Boolean");
        }
        return result;
    }

    @Override
    public Object convert(Object value, Class toClass) {
        Boolean result = null;
        if (value != null && value instanceof String) {
            result = parseBoolean((String) value);
        }
        return result;
    }

    @Override
    public Class getFromClass() {
        return String.class;
    }

    @Override
    public Class getToClass() {
        return Boolean.class;
    }
}
