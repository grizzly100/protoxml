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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

// get, set and is
public class PropertyDictionary {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyDictionary.class);

    private static final PropertyDictionary instance = new PropertyDictionary();
    private final HashMap<Class, Map<String, Property>> mappings = new HashMap<>();

    public static PropertyDictionary getInstance() {
        return instance;
    }

    /**
     * Get the key to be used in the map
     *
     * @param propertyName name to be converted into the key
     * @return the key
     */
    public static String toKey(String propertyName) {
        return propertyName.toUpperCase();
    }

    public Property getProperty(Class clazz, String propertyName) {
        return getPropertyMap(clazz).get(toKey(propertyName));
    }

    /**
     * Build and cache the property map for a given class
     *
     * @param clazz the class for which a map is required
     * @return the corresponding map
     */
    public Map<String, Property> getPropertyMap(Class clazz) {
        return mappings.computeIfAbsent(clazz, this::extractPropertyMap);
    }

    /**
     * Read the public methods for the given class and infer associated Properties
     *
     * @param clazz the Class to inspect
     * @return the propertyName to Property map
     */
    Map<String, Property> extractPropertyMap(Class clazz) {
        Map<String, Property> map = new HashMap<>();

        // Populate map with Property objects
        for (Method method : clazz.getMethods()) {
            Property.Accessor[] accessorHolder = Property.createAccessorHolder();
            String propertyName = Property.extractPropertyName(method, accessorHolder);

            if (accessorHolder[0] != Property.Accessor.NOT && !ignore(propertyName)) {
                // Take positive stance and create the named Property in the map
                Property prop = getOrCreateProperty(map, propertyName);

                // Store the GET or SET method
                if (accessorHolder[0] == Property.Accessor.GET) {
                    if (prop.getGetter() == null) {
                        prop.setGetter(method);
                    } else {
                        LOG.error("Multiple getters for property {} - retaining {}, ignoring {}",
                                propertyName, prop.getGetter().toString(), method.toString());
                    }
                } else if (accessorHolder[0] == Property.Accessor.SET) {
                    if (prop.getSetter() == null) {
                        prop.setSetter(method);
                    } else {
                        LOG.error("Multiple setters for property {} - retaining {}, ignoring {}",
                                propertyName, prop.getSetter().toString(), method.toString());
                    }
                }
            }
        }

        // Extract Type and Class information for each Property
        storeFields(clazz, map);
        map.values().forEach(Property::storeTypeInfo);

        // Use XML Annotations (if they exist)
        extractXMLAnnotations(clazz, map);

        return map;
    }

    /**
     * Read XmlType annotations to provide property ordering
     *
     * @param clazz the Class to inspect
     * @param map   the map to enrich
     */
    private void extractXMLAnnotations(Class clazz, Map<String, Property> map) {

        // Property order is contained in XmlType annotation
        @SuppressWarnings({"unchecked"})
        XmlType xmlType = (XmlType) clazz.getAnnotation(XmlType.class);
        if (xmlType != null) {
            int order = 1;
            for (String propName : xmlType.propOrder()) {
                Property prop = map.get(toKey(propName));
                if (prop != null) {
                    prop.setOrder(order++);
                }
            }
        }
    }

    private void storeFields(Class clazz, Map<String, Property> map) {
        // Property name/optionality is contained in XmlElement annotation
        do {
            for (Field field : clazz.getDeclaredFields()) {
                String name = field.getName();
                Property prop = map.get(toKey(name));
                // Set property field, if not yet set
                if (prop != null && prop.getField() == null) {
                    prop.setField(field);
                }
            }
            // It is possible that fields are declared in a parent class
            // so move up the type hierarchy to find any missing fields
            clazz = ClassUtil.getSuperclassElseNull(clazz);
        } while (clazz != null);
    }

    /**
     * Return true if the property should be ignored
     * Use to filter out java meta data
     *
     * @param propertyName property name to inspect
     * @return true if the property is not to be added to the dictionary
     */
    private boolean ignore(String propertyName) {
        boolean filter = false;
        switch (propertyName.toLowerCase()) {
            case "class":
            case "declaringclass":
                filter = true;
        }
        return filter;
    }


    private Property getOrCreateProperty(Map<String, Property> map, String propertyName) {
        return map.computeIfAbsent(toKey(propertyName), k -> new Property(propertyName));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Class clazz : this.mappings.keySet()) {
            builder.append(PropertyDebugger.classToString(clazz));
        }
        return builder.toString();
    }
}
