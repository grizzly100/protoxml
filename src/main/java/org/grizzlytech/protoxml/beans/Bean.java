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

import org.grizzlytech.protoxml.util.NVP;
import org.grizzlytech.protoxml.xml.XMLPath;

import java.util.Map;

/**
 * The Bean interface provides an abstract API for manipulating an object graph.
 * <p>
 * Callers can get and set properties by path - e.g., setValuePath("a.b.c",100)
 * <p>
 * Properties can be set in any order
 * <p>
 * It is assumed that the object being manipulated follows Java Bean conventions
 */
public interface Bean {

    /**
     * Properties can be commented by appending the COMMENT_SUFFIX_S
     */
    String COMMENT_SUFFIX_S = "#";

    /**
     * Property value class can be substituted by appending the CLASS_SUFFIX_S
     */
    String CLASS_SUFFIX_S = "$";

    /**
     * Property value classes can be created via custom factory methods by appending the FACTORY_SUFFIX_S
     */
    String FACTORY_SUFFIX_S = "&";


    NVP<?> getPathValue(String path)
            throws PropertyException;

    // Typed version
    <T> NVP<T> getPathValue(String path, Class<T> clazz)
            throws PropertyException;

    NVP<?> setPathValue(String path, Object value)
            throws PropertyException;

    /**
     * Use CLASS_SUFFIX_S
     */
    NVP<Class> setPathValueClassName(String path, String className)
            throws PropertyException;

    /**
     * Use FACTORY_SUFFIX_S
     * <p>
     * format&=testdomain.music#createVinyl
     */
    NVP<String> setPathValueFactory(String path, String factoryName)
    ;

    /**
     * Use COMMENT_SUFFIX_S
     */
    NVP<String> setPathComment(String path, String comment)
            throws PropertyException;

    Map<PropertyPath, String> getPathComments();

    Map<XMLPath, String> getXmlPathComments();

    Object unwrap();
}
