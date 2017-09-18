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
import org.grizzlytech.protoxml.builder.functions.Base;
import org.grizzlytech.protoxml.builder.functions.Dates;
import org.grizzlytech.protoxml.builder.functions.Maths;
import org.grizzlytech.protoxml.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Predicate;

public class PropertyFunctionFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyFunctionFactory.class);

    public static PropertyFunction createPropertyFunction(String functionName) {
        PropertyFunction.Syntax syntax = PropertyFunction.Syntax.parseSyntax(functionName);
        return (syntax != null) ? createPropertyFunction(syntax) : null;
    }

    public static PropertyFunction createPropertyFunction(PropertyFunction.Syntax syntax) {
        Method method = getPropertyMethod(syntax.getClassName(), syntax.getMethodName());
        return (method != null) ? new PropertyFunction(method) : null;
    }

    // Helper to lookup a static method by name. Assume it accepts Bean and String and a String array
    private static Method getPropertyMethod(String className, String methodName) {
        final Predicate<String> NAME_FILTER = (s) -> s.equalsIgnoreCase(methodName);
        final Predicate<Method> ATTRIBUTE_FILTER = (m) -> Modifier.isStatic(m.getModifiers());
        final Class[] PARAMETER_TYPES = new Class[]{Bean.class, String.class, String[].class};

        Class clazz;
        // To minimise typing, the Base class can be referred to as just "Base"
        switch (className.toLowerCase()) {
            case "base":
                clazz = Base.class;
                break;
            case "maths":
                clazz = Maths.class;
                break;

            case "dates":
                clazz = Dates.class;
                break;

            default:
                clazz = ClassUtil.getClassElseNull(className);
        }
        className = clazz.getCanonicalName();

        List<Method> methods = ClassUtil.getMethods(clazz, NAME_FILTER, PARAMETER_TYPES, ATTRIBUTE_FILTER);

        if (methods.size() != 1) {
            LOG.warn("Unable to find unique match: found [{}] matches for [{}]", methods.size(),
                    new PropertyFunction.Syntax(className, methodName).getFunctionName());
        }

        return (methods.size() > 0) ? methods.get(0) : null;
    }

}
