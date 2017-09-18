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

import org.grizzlytech.protoxml.util.ClassUtil;
import org.grizzlytech.protoxml.util.Tokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Functions for walking the Property Dictionary
 */
public class PropertyDebugger {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyDebugger.class);

    /**
     * Construct a string representation of the object using the dictionary
     * to identify and order properties
     *
     * @param source object to format
     * @return String representation of the object
     */
    public static String objectToString(Object source) {
        final char QUOTE = '\'';
        String result;

        // Simple type formatting
        if (source == null) {
            result = "'null'";
        } else if (source instanceof String) {
            result = QUOTE + ((String) source) + QUOTE;
        } else if (source instanceof Collection) {
            // Enumerate collections
            result = ((Collection<?>) source).stream().map(PropertyDebugger::objectToString).collect(Collectors.joining());
        } else if (ClassUtil.isCoreJavaClass(source)) {
            // All other (non-Collection) java.* classes output as a string
            result = source.toString();
        } else {
            // Use PropertyDictionary for non-Base Java classes
            final String FORMAT = "%s=%s";
            final String DELIMITER = ", ";

            Map<String, Property> properties = PropertyDictionary.getInstance().getPropertyMap(source.getClass());

            result = properties.values().stream().sorted()
                    .map(p -> String.format(FORMAT, p.getName(), objectToString(p.getValueElseNull(source))))
                    .reduce((p1, p2) -> p1 + DELIMITER + p2).orElse(null);

            result = source.getClass().getSimpleName() + "[" + result + "]";
        }
        return result;
    }

    /**
     * Output the Properties of the given Class
     *
     * @param clazz the class to inspect
     * @return String containing list of the Properties
     */
    public static String classToString(Class clazz) {
        StringBuilder builder = new StringBuilder();

        // Header
        builder.append(clazz.getName());
        builder.append(Tokens.NEWLINE_S);

        // Properties
        Map<String, Property> map = PropertyDictionary.getInstance().getPropertyMap(clazz);
        List<Property> properties = new ArrayList<>(map.values());
        Collections.sort(properties);

        int count = 0;
        for (Property prop : properties) {
            if (count++ > 0) {
                builder.append(Tokens.NEWLINE_S);
            }
            builder.append("+ ");
            builder.append(prop.getName());
            builder.append("[");
            builder.append(prop.getValueTypeSimpleName());
            builder.append("]");
            builder.append("[");
            builder.append(prop.getOrder());
            builder.append(":");
            builder.append(prop.getXmlName());
            builder.append("]");
        }

        return builder.toString();
    }

    /**
     * Emit the recursive Property View for a given class
     * Collections are replaced by a sample [0] array index
     * <p>
     * # Properties of testdomain.employee.Employee:
     * #Name=String
     * #Salary=double
     * #Designation=String
     * #Address=testdomain.Address
     * #Address.City=String
     * #Address.Line1=String
     * #Address.Line2=String
     * #Address.State=String
     * #Address.Zipcode=long
     * #Phones[0]=testdomain.Phone
     * #Phones[0].CountryCode=String
     * #Phones[0].LocalNumber=String
     *
     * @param clazz class to inspect
     * @return walk
     */
    public static String propertyTreeToString(Class clazz) {
        StringBuilder builder = new StringBuilder();
        builder.append(clazz.getName());
        builder.append(Tokens.NEWLINE_S);
        propertyTreeToString(builder, clazz, "");
        return builder.toString();
    }

    private static void propertyTreeToString(StringBuilder builder, Class clazz, String path) {
        // Retrieve the properties of the class into a sorted list
        Map<String, Property> map = PropertyDictionary.getInstance().getPropertyMap(clazz);
        List<Property> properties = new ArrayList<>(map.values());
        Collections.sort(properties);

        for (Property prop : properties) {
            Class propClass = prop.getValueClass();

            // Extend the path using the name of the property and a zero index if generic
            String newPath = path + prop.getName();
            if (Collection.class.isAssignableFrom(prop.getValueClass())) {
                newPath = newPath + "[0]"; // sample index
                propClass = prop.getValueParameterClass();
            } else if (ClassUtil.isCoreJavaClass(propClass)) {
                // Echo the path and type of the parameter
                builder.append("#");
                builder.append(newPath);
                builder.append("=");
                builder.append(prop.getValueTypeSimpleName());
                if (prop.isRequired()) {
                    builder.append(" (Required)");
                }
                builder.append(Tokens.NEWLINE_S);
            }
            if (!ClassUtil.isCoreJavaClass(propClass)) {
                propertyTreeToString(builder, propClass, newPath + ".");
            }
        }
    }
}
