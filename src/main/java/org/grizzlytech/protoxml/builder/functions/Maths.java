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
import org.grizzlytech.protoxml.util.NVP;
import org.grizzlytech.protoxml.util.NumberUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Number parsing and basic mathematical operations
 */
public class Maths {

    /**
     * Parse a string argument for a number
     * The string can contain either a number or a relative/absolute path for de-referencing
     */
    public static Number parseNumber(Bean bean, String path, String arg)
            throws PropertyException {
        assert arg != null;
        NumberUtil.NumberAttributes attributes = new NumberUtil.NumberAttributes();
        Number number;
        if (NumberUtil.isNumber(arg, attributes)) {
            number = attributes.isInteger() ? new BigInteger(arg) : new BigDecimal(arg);
        } else {
            String resolved = Base.resolvePath(path, arg);
            NVP<Number> contents = bean.getPathValue(resolved, Number.class);
            number = contents.getValue();
        }
        return number;
    }

    /**
     * Parse an array of Strings into numbers
     */
    public static List<Number> parseNumbers(Bean bean, String path, String[] args)
            throws PropertyException {
        assert args != null && args.length >= 1;
        List<Number> terms = new ArrayList<>();
        for (String arg : args) {
            terms.add(parseNumber(bean, path, arg));
        }
        return terms;
    }

    // add(a, b, c, ...)
    public static String add(Bean bean, String path, String[] args)
            throws PropertyException {
        assert args != null && args.length >= 2;
        BigDecimal sum = BigDecimal.ZERO; // most generic container
        List<Number> numbers = parseNumbers(bean, path, args);
        for (Number number : numbers) {
            BigDecimal term = (number instanceof BigDecimal) ? (BigDecimal) number : new BigDecimal(number.toString());
            sum = sum.add(term);
        }
        return NumberUtil.removeTrailingZeros(sum.toString()); // maximise parsing options later on
    }

    // multiply(a, b, c, ...)
    public static String multiply(Bean bean, String path, String[] args)
            throws PropertyException {
        assert args != null && args.length >= 2;
        BigDecimal product = BigDecimal.ONE; // most generic container
        List<Number> numbers = parseNumbers(bean, path, args);
        for (Number number : numbers) {
            BigDecimal term = (number instanceof BigDecimal) ? (BigDecimal) number : new BigDecimal(number.toString());
            product = product.multiply(term);
        }
        return NumberUtil.removeTrailingZeros(product.toString()); // maximise parsing options later on
    }
}
