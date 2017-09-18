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

package org.grizzlytech.protoxml.builder;

import org.grizzlytech.protoxml.beans.Bean;
import org.grizzlytech.protoxml.beans.PropertyException;
import org.grizzlytech.protoxml.util.Common;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyFunctionTest {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyFunctionTest.class);

    public static Object helpMe(Bean bean, String path, String[] args)
            throws PropertyException {
        if ("bang".equals(args[0])) {
            throw new PropertyException("Oh no!", path);
        }
        return "Help from " + path + " with args " + Common.listToString(args, ",");
    }

    @Test
    public void createPropertyFunction() throws PropertyException {
        String functionName = "!" + PropertyFunctionTest.class.getCanonicalName() + "#HelpMe( a, b, c )";
        PropertyFunction.Syntax syntax = PropertyFunction.Syntax.parseSyntax(functionName);
        PropertyFunction fn = PropertyFunctionFactory.createPropertyFunction(syntax.getFunctionName());

        assert fn != null;
        Object result = fn.apply(null, "hello", syntax.getArguments());
        LOG.info("Result: [{}] -> {}", syntax.toString(), result);
    }

    @Test(expected = PropertyException.class)
    public void createPropertyFunction2() throws PropertyException {
        String functionName = "!" + PropertyFunctionTest.class.getCanonicalName() + "#HelpMe( bang )";
        PropertyFunction.Syntax syntax = PropertyFunction.Syntax.parseSyntax(functionName);
        PropertyFunction fn = PropertyFunctionFactory.createPropertyFunction(syntax.getFunctionName());

        assert fn != null;
        Object result = fn.apply(null, "hello", syntax.getArguments());
        LOG.info("Result: [{}] -> {}", syntax.toString(), result);
    }

}