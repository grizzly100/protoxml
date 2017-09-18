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
import org.grizzlytech.protoxml.util.Common;
import org.grizzlytech.protoxml.xml.XMLObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import java.lang.reflect.*;
import java.util.Collection;

/**
 * Encapsulates a Java BeanImpl property
 * Used to dynamically get and set properties
 * Has support for generics, where a property is a collection
 */
public class Property implements Comparable<Property> {

    private static final Logger LOG = LoggerFactory.getLogger(Property.class);

    /**
     * name (sans "get"/"is" or "set") of the Java property
     */
    private String name;

    private Field field=null;
    private Method getter=null;
    private Method setter=null;

    /**
     * Class of the property value.
     * <p>
     * Generics, this is the RawType. E.g., List<Foo> would have valueClass of List
     */
    private Class valueClass;

    private boolean isGeneric;
    /**
     * Class of the parametrized property value.
     * <p>
     * E.g., for List<Foo> would have parameterClass of List
     */
    private Class valueParameterClass;

    /**
     * name of the XML type/attribute
     */
    private String xmlName;

    private int order = 256;

    private boolean required = false;

    public Property(String name) {
        this.name = name;
    }

    static String extractPropertyName(Method method, Accessor[] accessorHolder) {
        String propertyName = null;
        if (method == null || accessorHolder == null || accessorHolder.length != 1) {
            throw new IllegalArgumentException("method and accessorHolder must be provided");
        }
        // Initially assume the method is NOT an Accessor
        accessorHolder[0] = Accessor.NOT;

        String methodName = method.getName();

        // Determine accessor
        String methodNameUC = methodName.toUpperCase();
        if (methodNameUC.startsWith(Accessor.GET.getPrefix()) && method.getParameterCount() == 0) {
            accessorHolder[0] = Accessor.GET;
            propertyName = methodName.substring(Accessor.GET.getPrefix().length(), methodName.length());
        } else if (methodNameUC.startsWith(Accessor.IS.getPrefix()) && method.getParameterCount() == 0) {
            accessorHolder[0] = Accessor.GET; // consider "is" as a Getter
            propertyName = methodName.substring(Accessor.IS.getPrefix().length(), methodName.length());
        } else if (methodNameUC.startsWith(Accessor.SET.getPrefix()) && method.getParameterCount() == 1) {
            accessorHolder[0] = Accessor.SET;
            propertyName = methodName.substring(Accessor.SET.getPrefix().length(), methodName.length());
        }

        // Determine property name
        if (accessorHolder[0] != Accessor.NOT) {
            propertyName = Common.camelCase(propertyName);
        }
        return propertyName;
    }

    static Property.Accessor[] createAccessorHolder() {
        return new Property.Accessor[1];
    }

    public String getName() {
        return this.name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getIndexedName(int index) {
        return getName() + ((index >= 0) ? "[" + index + "]" : "");
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    Method getGetter() {
        return getter;
    }

    void setGetter(Method getter) {
        this.getter = getter;
    }

    Method getSetter() {
        return setter;
    }

    void setSetter(Method setter) {
        this.setter = setter;
    }

    public Class getValueClass() {
        return this.valueClass;
    }

    public Class getValueParameterClass() {
        return this.valueParameterClass;
    }

    public boolean isGeneric() {
        return isGeneric;
    }

    public boolean isCollection() {
        return isGeneric && Collection.class.isAssignableFrom(this.valueClass);
    }

    public String getValueTypeName() {
        String typeName = Common.safeToName(this.valueClass);
        if (this.isGeneric) {
            typeName += '<' + Common.safeToName(this.valueParameterClass) + '>';
        }
        return typeName;
    }

    public String getValueTypeSimpleName() {
        String typeName = this.valueClass.getSimpleName();
        if (this.isGeneric) {
            typeName += '<' + this.valueParameterClass.getSimpleName() + '>';
        }
        return typeName;
    }

    public String getXmlName() {
        final String DEFAULT = "##default";
        return (Common.isEmpty(xmlName) || DEFAULT.equals(xmlName)) ? getName() : xmlName;
    }

    public void setXmlName(String xmlName) {
        this.xmlName = xmlName;
    }

    public String getXmlName(Class substitution) {
        final String DEFAULT = "##default";
        String xmlName = null;
        XmlElements elements = (this.field != null) ? this.field.getAnnotation(XmlElements.class) : null;

        if (elements != null) {
            for (XmlElement e : elements.value()) {
                if (e.type().equals(substitution)) {
                    xmlName = e.name();
                    break;
                }
            }
        }
        if (xmlName == null || DEFAULT.equals(xmlName)) {
            LOG.warn("No @XmlElements defined for [{}]: {}", toString(), substitution.getCanonicalName());
            xmlName = Common.camelCase(substitution.getSimpleName());
        }
        return xmlName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    // Extraction methods for the Value's Class and Type

    void storeTypeInfo() {
        try {
            // Set the valueClass
            this.valueClass = extractValueClass();

            // Set generic attributes
            ParameterizedType valueParameterType = extractValueParameterType();

            this.isGeneric = valueParameterType != null;

            if (this.isGeneric) {
                this.valueParameterClass = extractValueParameterClass(valueParameterType);
            }

            if (this.field != null) {
                storeAnnotationInfo();
            }
        } catch (PropertyException ex) {
            Common.fatalException(ex, LOG, "extractTypeInfo failed for property {}", this.name);
        }
    }

    void storeAnnotationInfo() {
        // Check in order to likelihood
        XmlElement xmlElement = this.field.getAnnotation(XmlElement.class);
        if (xmlElement != null) {
            this.xmlName = xmlElement.name();
            this.required = xmlElement.required();
            return;
        }

        XmlAttribute xmlAttribute = this.field.getAnnotation(XmlAttribute.class);
        if (xmlAttribute != null) {
            this.xmlName = xmlAttribute.name();
            this.required = xmlAttribute.required();
            return;
        }

        XmlElementRef xmlElementRef = this.field.getAnnotation(XmlElementRef.class);
        if (xmlElementRef != null) {
            this.xmlName = xmlElementRef.name();
            this.required = xmlElementRef.required();
        }
    }

    private Class extractValueClass() {
        Class clazz = null;
        if (this.getter != null) {
            clazz = this.getter.getReturnType();
        } else if (this.setter != null) {
            clazz = this.setter.getParameterTypes()[0];
        }
        return clazz;
    }

    // Methods to obtain empty values

    private ParameterizedType extractValueParameterType() {
        Type type = null;
        if (this.getter != null) {
            type = this.getter.getGenericReturnType();
        } else if (this.setter != null) {
            type = this.setter.getGenericParameterTypes()[0];
        }
        return (type instanceof ParameterizedType) ? (ParameterizedType) type : null;
    }

    private Class extractValueParameterClass(ParameterizedType valueParameterType)
            throws PropertyException {
        Class clazz = null;
        Type[] arguments = valueParameterType.getActualTypeArguments();
        if (arguments.length > 1) {
            throw new PropertyException("Unsupported: Property is a generic with more than one parameter: "
                    + valueParameterType.getTypeName(), name);
        }
        String className = arguments[0].getTypeName();

        // Handle "? extends X" case
        final String EXTENDS = "? extends ";
        if (className.startsWith(EXTENDS)) {
            className = className.replace(EXTENDS, "");
        }
        // Handle "X<?>" case
        final String ANY = "<?>";
        if (className.endsWith(ANY)) {
            className = className.replace(ANY, "");
        }
        // Ignore the single character "T" and "?" cases
        if (className.length() > 1) {
            // Attempt to load the class
            clazz = ClassUtil.getClassElseNull(className);
            if (clazz == null) {
                LOG.warn("Unable to parse value class from [{}] for property [{}]. You must use setPathValueClassName",
                        className, toString());
            }
        }
        return (clazz != null) ? clazz : Object.class;
    }

    // Getter and Setter for Value

    public Object getValue(Object target) throws PropertyException {
        Object value;
        if (this.getter == null) {
            throw new PropertyException("No getter defined", this.name);
        }
        try {
            value = this.getter.invoke(target, (Object[]) null);

            if (value == null && isCollection()) {
                // We rely on the host being able to instantiate generics.
                // The Bean#follow() method will fail otherwise.
                String message = String.format(
                        "The getter implementation [%s] returned null as opposed to the expected instance of [%s]",
                        target.getClass().getCanonicalName() + "#" + this.getter.getName(), getValueTypeName());
                throw new PropertyException(message, getName());
            }
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException ex) {
            throw new PropertyException("Cannot invoke getter: " + this.getter.getName(), this.name, ex);
        }

        return value;
    }

    public Object getValueElseNull(Object target) {
        Object value = null;
        try {
            value = getValue(target);
        } catch (PropertyException ex) {
            LOG.error(ex.toString());
        }
        return value;
    }

    public void setValue(Object target, Object value)
            throws PropertyException {
        if (this.setter == null) {
            throw new PropertyException("No setter defined", this.name);
        }

        // Try to invoke the setter
        try {
            this.setter.invoke(target, value);
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException ex) {
            String error = String.format("Cannot invoke setter %1s with value %2s on object %3s",
                    this.setter.getName(), (value != null) ?
                            (value.getClass().getName() + ": " + value.
                                    toString()) : "null", target.getClass().getName());
            throw new PropertyException(error, this.getName(), ex);
        }
    }

    // Getter and Setter for Beans holding the Value

    // For mutable value classes - methods to create an empty value

    Object emptyValue(Class<?> clazz)
            throws PropertyException {

        return emptyValue(clazz, null);
    }

    Object emptyValue(Class<?> clazz, String factoryName)
            throws PropertyException {
        Object value = null;
        try {
            if (clazz.isEnum()) {
                throw new PropertyException("Property is an Enum and cannot therefore be constructed", this
                        .name);
            } else if (clazz.isPrimitive()) {
                throw new PropertyException("Property is a Primitive and cannot therefore be constructed", this
                        .name);
            }

            // Use the JAXB Object Factory for @XMLType classes
            if (clazz.isAnnotationPresent(XmlType.class)) {
                XMLObjectFactory factory = XMLObjectFactory.getInstance();
                if (JAXBElement.class.equals(this.valueClass) || JAXBElement.class.equals(this.valueParameterClass)) {
                    Object parameter = factory.createObject(clazz);
                    value = factory.createObjectW(JAXBElement.class, parameter, factoryName);
                } else {
                    value = factory.createObject(clazz, factoryName);
                }
            }
            // Otherwise just use newInstance
            if (value == null) {
                value = clazz.newInstance();
            }

        } catch (InstantiationException | IllegalAccessException ex) {
            throw new PropertyException("Cannot load class: " + clazz.getName(), this.name, ex);
        }
        return value;
    }

    @Override
    public String toString() {
        return "Property(" +
                "name=" + Common.safeToString(name) +
                ", type=" + getValueTypeName() +
                ", field=" + Common.safeToString(field) +
                ", getter=" + Common.safeToString(getter) +
                ", setter=" + Common.safeToString(setter) +
                ", xmlName=" + getXmlName() +
                ", order=" + order +
                ", required=" + isRequired() +
                ")";
    }

    @Override
    public int compareTo(Property property) {
        return this.getOrder() - property.getOrder();
    }

    /**
     * Assumes that className "is-or-extends" valueClass
     * <p>
     * Used if the user wishes to request a subclass of value
     */
    @SuppressWarnings("unchecked")
    public Object emptyValueUnused(String subclassName)
            throws PropertyException {
        // Load the requested class
        Class requestedClass;
        try {
            requestedClass = Class.forName(subclassName);
        } catch (ClassNotFoundException ex) {
            throw new PropertyException("Cannot load requested class: " + subclassName, this.name, ex);
        }
        // Verify a subclass has been requested, otherwise setting the value will fail later
        if (!this.valueClass.isAssignableFrom(requestedClass)) {
            throw new PropertyException(
                    "Requested class [" + subclassName + "] is not a subclass of valueClass: " +
                            Common.safeToName(this.valueClass), this.name);
        }
        return emptyValue(requestedClass);
    }


    public enum Accessor {
        GET("GET"), IS("IS"), SET("SET"), NOT("NOT");

        private final String prefix;

        Accessor(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return this.prefix;
        }
    }

}
