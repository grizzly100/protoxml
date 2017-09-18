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
import org.grizzlytech.protoxml.beans.BeanImpl;
import org.grizzlytech.protoxml.beans.PropertyDebugger;
import org.grizzlytech.protoxml.beans.PropertyException;
import org.grizzlytech.protoxml.util.ClassUtil;
import org.grizzlytech.protoxml.util.Common;
import org.grizzlytech.protoxml.util.NVP;
import org.grizzlytech.protoxml.xml.XMLObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlType;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * This class is for single use and is not thread safe.
 * <p>
 * NOTE: Tracing re-creates the property file - and fixes property name case
 */
final public class BeanBuilder implements NVPReader.Callback {

    public static final String CLASS_NAME = "doctype";
    public static final String COPY_PREFIX_S = "%";

    private static final Logger LOG = LoggerFactory.getLogger(BeanBuilder.class);

    /**
     * Bean being constructed
     */
    private Bean bean = null;

    /**
     * Trace output (used for debugging)
     */
    private BufferedWriter traceWriter = null;

    /**
     * Create a Bean using the properties provided by the reader.
     * <p>
     * The first property must be the CLASS_NAME
     *
     * @param reader reader providing the properties
     * @param trace  writer for tracing output
     * @return the populated Bean
     */
    public Bean createBean(NVPReader reader, Writer trace) {

        try (BufferedWriter bw = new BufferedWriter(trace)) {
            // Store reference to tracer to enable trace() method
            this.traceWriter = bw;
            // Read the NVPs and set them into the Bean
            reader.read(this);
        } catch (IOException ex) {
            LOG.error("Error writing to trace log [{}]", trace, ex);
        } finally {
            this.traceWriter = null;
        }

        if (LOG.isDebugEnabled()) {
            Object underlying = (this.bean != null) ? this.bean.unwrap() : null;
            LOG.debug("createBean: {}", (underlying != null) ? PropertyDebugger.objectToString(underlying) : "null");
        }
        return bean;
    }

    // NVP Callback handler implementation
    @Override
    public void onComment(String comment, int lineNum) {
        trace(comment);
    }

    // NVP Callback handler implementation
    @Override
    public void onNVP(NVP<String> pair, int lineNum) {
        String path = pair.getName();
        Object value = pair.getValue(); // define as Object, as could be overwritten by a PropertyFunction

        NVP<?> result = null; // result holds the correct path capitalization and any value conversion

        try {
            // Apply a PropertyFunction if specified, to replace the value
            if (pair.getValue().startsWith(PropertyFunction.Syntax.FUNCTION_PREFIX_S)) {
                value = getPropertyFunctionValue(pair);
            }

            if (CLASS_NAME.equalsIgnoreCase(path) && Common.notEmpty(value.toString())) {
                // Create the bean using the CLASS_NAME as the className
                this.bean = instantiateBean(value.toString());
                result = new NVP<>(CLASS_NAME, this.bean.unwrap().getClass().getCanonicalName());
            } else {
                Common.fatalAssertion(this.bean != null, LOG,
                        "You must specify the [{}] property as the first property", CLASS_NAME);

                // Set Comment
                if (path.endsWith(Bean.COMMENT_SUFFIX_S)) {
                    result = this.bean.setPathComment(path, value.toString());
                }
                // Set Path Value Class
                else if (path.endsWith(Bean.CLASS_SUFFIX_S)) {
                    result = this.bean.setPathValueClassName(path, value.toString());
                }
                // Set Path Value Factory Class
                else if (path.endsWith(Bean.FACTORY_SUFFIX_S)) {
                    result = this.bean.setPathValueFactory(path, value.toString());
                }
                // Set Path Value
                else {
                    result = this.bean.setPathValue(path, value);
                }
            }
        } catch (PropertyException ex) {
            LOG.error("Line {}: {}={} [{}]", lineNum, path, Common.safeToString(value), ex.getMessage());
        } finally {
            // Trace result (and hence correct capitalization)
            if (result != null) {
                trace(String.format("%s=%s", result.getName(), pair.getValue()));
            } else {
                trace(String.format("%s=%s [FAIL]", path, pair.getValue()));
            }
        }
    }

    /**
     * Create an instance of the underlying className and wrap it in a Bean.
     * Use the associated AbstractObjectFactory for XmlTYpe classes
     *
     * @param className name of the underlying class
     * @return a Bean wrapping the underlying
     */
    private Bean instantiateBean(String className) {
        // Load the Class
        Class<?> clazz = ClassUtil.getClassElseNull(className);
        Common.fatalAssertion(clazz != null, LOG, "Cannot get class for [{}]", className);

        // Instantiate the Bean, using an AbstractObjectFactory if an XMLType
        assert clazz != null;
        Object underlying = (clazz.isAnnotationPresent(XmlType.class)) ?
                XMLObjectFactory.getInstance().createObject(clazz) : ClassUtil.newInstanceElseNull(clazz);
        Common.fatalAssertion(underlying != null, LOG, "Cannot instantiate class [{}]", clazz.getCanonicalName());

        return new BeanImpl(underlying);
    }

    /**
     * Create and apply the PropertyFunction specified in the NVP value.
     * If successful, return the value of the function, else the original NVP value.
     *
     * @param pair pair containing the path and the value "!class#method(args)"
     * @return the value computed by the function
     */
    private Object getPropertyFunctionValue(NVP<String> pair)
            throws PropertyException {
        Object value = pair.getValue(); // default to the provided value
        PropertyFunction.Syntax syntax = PropertyFunction.Syntax.parseSyntax(pair.getValue());

        if (syntax != null) {
            PropertyFunction fn = PropertyFunctionFactory.createPropertyFunction(syntax);
            if (fn != null) {
                pair.setValue(syntax.toString()); // correct PropertyFunction layout
                value = fn.apply(this.bean, pair.getName(), syntax.getArguments());
            }
        }
        return value;
    }

    /**
     * Write message to trace writer
     */
    private void trace(String message) {
        try {
            traceWriter.write(message);
            traceWriter.newLine();
        } catch (IOException ex) {
            LOG.error("Cannot log to trace file", ex);
        }
    }
}
