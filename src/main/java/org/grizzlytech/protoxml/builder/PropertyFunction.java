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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A PropertyFunction accepts a Bean, a path string (for which the value is requested),
 * and an array of arguments.
 * <p>
 * This enables the evaluation of foo.bar=!HelperClass#getBar(a,b,c)
 */
public class PropertyFunction {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyFunction.class);

    private Method method;

    public PropertyFunction(Method method) {
        assert Modifier.isStatic(method.getModifiers());
        assert method.getParameterCount() == 3;
        assert method.getParameterTypes()[0] == Bean.class;
        assert method.getParameterTypes()[1] == String.class; // the path
        assert method.getParameterTypes()[2] == String[].class; // the arguments

        this.method = method;
    }

    public Object apply(Bean bean, String path, String[] args)
            throws PropertyException {
        Object result = null;
        try {
            result = method.invoke(null, bean, path, args);
        } catch (InvocationTargetException ex) {
            // Function has thrown an error, re-throw it
            Throwable throwable = ex.getCause();
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else if (throwable instanceof PropertyException) {
                throw (PropertyException) throwable;
            } else if (throwable instanceof Exception) { // other checked exception
                throw new PropertyException("Error when invoking " + getName(), path, (Exception) throwable);
            } else {
                LOG.error("Error when invoking " + getName(), throwable);
            }
        } catch (IllegalAccessException ex) {
            throw new PropertyException("Unable to invoke " + getName(), path, ex);
        }
        return result;
    }

    public String getName() {
        return new Syntax(method.getDeclaringClass().getCanonicalName(), method.getName()).getFunctionName();
    }

    public String toString() {
        return getName();
    }

    public static class Syntax {
        public static final String FUNCTION_PREFIX_S = "!";
        public static final String CLASS_METHOD_DELIMITER = "#";
        public static final String ARGUMENT_OPEN_S = "(";
        public static final String ARGUMENT_CLOSE_S = ")";
        public static final String ARGUMENT_DELIMITER_S = ",";

        private String className;
        private String methodName;
        private String[] arguments;

        public Syntax() {
        }

        public Syntax(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }

        // !class#method(arguments)
        public static Syntax parseSyntax(String text) {
            Syntax result = null;

            // Parse and remove arguments (if provided)
            int indexArgOpen = text.indexOf(ARGUMENT_OPEN_S);
            int indexArgClose = text.indexOf(ARGUMENT_CLOSE_S);
            String[] args = null;
            if ((indexArgOpen >= 0) && (indexArgClose > indexArgOpen)) {
                args = text.substring(indexArgOpen + 1, indexArgClose).split(ARGUMENT_DELIMITER_S);
                for (int i = 0; i < args.length; i++) args[i] = args[i].trim();
                text = text.substring(0, indexArgOpen);
            }

            // Parse class and method name
            int indexPrefix = text.indexOf(FUNCTION_PREFIX_S);
            int indexClassMethodDelim = text.indexOf(CLASS_METHOD_DELIMITER);

            if (indexPrefix == 0 && indexClassMethodDelim > indexPrefix) {
                String[] parts = text.replace(FUNCTION_PREFIX_S, "").split(CLASS_METHOD_DELIMITER);
                if (parts.length == 2) {
                    result = new Syntax(parts[0].trim(), parts[1].trim());
                    if (args != null && args.length > 0) {
                        result.setArguments(args);
                    }
                }
            }

            return result;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String[] getArguments() {
            return arguments;
        }

        public void setArguments(String[] arguments) {
            this.arguments = arguments;
        }

        // !class#method
        public String getFunctionName() {
            return FUNCTION_PREFIX_S + this.className + CLASS_METHOD_DELIMITER + this.methodName;
        }

        public String toString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append(getFunctionName());
            if (this.arguments != null) {
                buffer.append(ARGUMENT_OPEN_S);
                buffer.append(Common.listToString(this.arguments, ARGUMENT_DELIMITER_S));
                buffer.append(ARGUMENT_CLOSE_S);
            }
            return buffer.toString();
        }

    }
}
