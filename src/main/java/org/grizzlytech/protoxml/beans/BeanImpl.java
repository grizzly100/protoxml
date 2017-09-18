/*
 * The MIT License
 *
 * Copyright (c) 2017, GrizzlyTech.org
 *
 * Permission is hereby FOO granted, free of charge, to any person obtaining a copy
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

import org.grizzlytech.protoxml.util.*;
import org.grizzlytech.protoxml.xml.XMLPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.grizzlytech.protoxml.util.Tokens.CLASS_METHOD_DELIMITER_S;

/**
 * Wraps a Java BeanImpl and provides read/write access to immediate and nested properties
 * Nested means contained in a child BeanImpl
 * Setting a value of a child bean causes an instance of that bean to be be automatically created.
 * Supports indexed properties, where the index is into a List<T>
 */
public class BeanImpl implements Bean {

    private static final Logger LOG = LoggerFactory.getLogger(BeanImpl.class);
    /**
     * Object being wrapped by this Bean
     */
    private final Object underlying;

    /**
     * Map of paths to classes to enable subclassing of property objects.
     * When creating an empty property value, if there is an extension class present,
     * an object of the supplied subclass will be created instead.
     */
    private final Map<PropertyPath, Class> extensionClassMap = new HashMap<>();

    /**
     * Map of paths to factory methods to enable custom object creation.
     * When creating an empty property value, if there is an extension factory present,
     * that method will be used to create the value.
     */
    private final Map<PropertyPath, String> extensionFactoryMap = new HashMap<>();

    private final SubstitutionLog substitutionLog = new SubstitutionLog();

    /**
     * Map of Java property path to pair of xml path and comment text
     * The xml path is needed when decorating the comments post XML marshalling
     */
    private final Map<PropertyPath, String> comments = new HashMap<>();

    /**
     * Construct a BeanImpl
     */
    public BeanImpl(Object underlying) {
        this.underlying = underlying;
    }

    public NVP<?> getPathValue(String path)
            throws PropertyException {
        PropertyPath route = new PropertyPath(path);

        Object host = getHost(route);

        Object value = getValue(host, route.getName(route.lastStep()));

        return new NVP<>(route.toString(), value);
    }

    @SuppressWarnings("unchecked")
    public <T> NVP<T> getPathValue(String path, Class<T> clazz)
            throws PropertyException {
        PropertyPath route = new PropertyPath(path);

        Object host = getHost(route);

        Object value = getValue(host, route.getName(route.lastStep()));

        if (value != null && !clazz.isAssignableFrom(value.getClass())) {
            // Attempt a type conversion
            try {
                value = ConverterRegistry.getInstance().convert(clazz, value, path);
            } catch (IllegalArgumentException ex) {
                throw new PropertyException("Could not perform type conversion", path, ex).
                        setValue(value);
            }
        }
        // Check cast to T will succeed
        //assert value==null || clazz.isAssignableFrom(value.getClass());

        return new NVP<>(route.toString(), (T) value);
    }

    public NVP<?> setPathValue(String path, Object value)
            throws PropertyException {
        PropertyPath route = new PropertyPath(path);

        Object host = getHost(route);

        Object convertedValue = setValue(host, route.getName(route.lastStep()), value);

        return new NVP<>(route.toString(), convertedValue);
    }

    /**
     * Provide an extension class for the property.
     * <p>
     * When the property is set, the Property object will validate that the
     * requested class is a subclass of the value class.
     *
     * @param path      path to the property
     * @param className new className to use
     * @throws PropertyException if the property cannot be found
     */
    public NVP<Class> setPathValueClassName(String path, String className)
            throws PropertyException {
        // Remove the CLASS_SUFFIX_S before following the route
        PropertyPath route = new PropertyPath(path.replace(CLASS_SUFFIX_S, ""));

        Object host = getHost(route);

        Property prop = getProperty(host.getClass(), route.getName(route.lastStep()));

        // Set the extension
        Class clazz = ClassUtil.getClassElseNull(className);
        if (clazz != null) {
            this.extensionClassMap.put(route, clazz);
            return new NVP<>(route.toString() + CLASS_SUFFIX_S, clazz);
        } else {
            return null;
        }
    }

    /**
     * Provide an extension factory for the property.
     * <p>
     * When the factory is set, the Property object will validate that the
     * requested factory method produces the value class (or subclass)
     * <p>
     * Format: class#method
     *
     * @param path        path to the property
     * @param factoryName new name of the factory method to use
     */
    public NVP<String> setPathValueFactory(String path, String factoryName) {
        List<Method> candidates = AbstractObjectFactory.matchFactoryMethods(factoryName, x -> true);

        Common.argumentAssertion(candidates != null && candidates.size() > 0, LOG,
                "Could not locate 'public <non-void> factoryMethod(0 or 1 parameters) called [{}]", factoryName);

        //assert candidates != null && candidates.size() >0;
        //noinspection ConstantConditions
        Method firstMatch = candidates.get(0);
        String validatedFactoryName = firstMatch.getDeclaringClass().getCanonicalName() +
                CLASS_METHOD_DELIMITER_S + firstMatch.getName();

        // Store the validated factory name
        PropertyPath route = new PropertyPath(path.replace(FACTORY_SUFFIX_S, ""));
        this.extensionFactoryMap.put(route, validatedFactoryName);

        return new NVP<>(path, validatedFactoryName);
    }


    public NVP<String> setPathComment(String path, String comment)
            throws PropertyException {
        // Validate path correctness and capitalization
        PropertyPath route = new PropertyPath(path.replace(COMMENT_SUFFIX_S, ""));

        Object host = getHost(route);

        // Add comment to map (sans COMMENT_SUFFIX_S)
        getPathComments().put(route, comment);

        return new NVP<>(route.getPath() + COMMENT_SUFFIX_S, comment);
    }

    public Map<PropertyPath, String> getPathComments() {
        return this.comments;
    }

    /**
     * Transform the comment and substitution map into a {xml path, comment} map
     *
     * @return mapping between the expected xpath and the comment
     */
    public Map<XMLPath, String> getXmlPathComments() {
        return this.comments.keySet().stream()
                .collect(Collectors.toMap(this::getXmlPath, this.comments::get));
    }

    private XMLPath getXmlPath(PropertyPath path) {
        return new XMLPath(this.substitutionLog.apply(path.withXmlNames()));
    }

    public Object unwrap() {
        return this.underlying;
    }

    // IMPLEMENTATION

    private Property getProperty(Class hostClass, String propertyName)
            throws PropertyException {
        PropertyDictionary dictionary = PropertyDictionary.getInstance();
        Property prop = dictionary.getProperty(hostClass, propertyName);

        if (prop == null) {
            String message = String.format("Property [%s] does not exist in [%s]%s%s",
                    propertyName, hostClass.getCanonicalName(), Tokens.NEWLINE_S,
                    PropertyDebugger.classToString(hostClass));
            throw new PropertyException(message, propertyName);
        }
        return prop;
    }

    /**
     * Return the parent object (host) of the final property in the path
     *
     * @param path route to the property
     * @return the host of the final property
     * @throws PropertyException if problems navigating the path to the host
     */
    private Object getHost(PropertyPath path)
            throws PropertyException {
        Object host = this.underlying;

        int step = 0;
        for (; step <= path.lastStep(); step++) {
            // Overwrite the PropertyPath name/xmlName with the names registered in the Property
            Property prop = getProperty(host.getClass(), path.getName(step));
            String propertyName = prop.getName();
            path.setName(step, propertyName);
            path.setXmlName(step, prop.getXmlName());

            if (step < path.lastStep()) {
                // If not on the last step, then follow the named property
                host = follow(host, path, step);
            }
        }

        return host;
    }

    private Object getValue(Object host, String propertyName)
            throws PropertyException {
        return getProperty(host.getClass(), propertyName).getValue(host);
    }

    /**
     * Set the value of the given property.
     * Perform any value type conversion required beforehand.
     *
     * @param propertyName property to set
     * @param value        value to set
     * @throws PropertyException if problems navigating the path to the host
     */
    @SuppressWarnings({"unchecked"})
    public Object setValue(Object host, String propertyName, Object value)
            throws PropertyException {
        Property prop = getProperty(host.getClass(), propertyName);

        // Check if value needs to be converted prior to calling setValue
        if (value != null) {
            Class type = prop.getValueClass();
            // Check enums
            if (type.isEnum()) {
                // Lookup the Enum value
                try {
                    value = Enum.valueOf(type, (String) value);
                } catch (IllegalArgumentException ex) {
                    throw new PropertyException("Enum value not valid", propertyName, ex).setValue(value);
                }
            } else if (!type.equals(value.getClass())) {
                // Attempt a type conversion for non-primitive target classes
                try {
                    value = ConverterRegistry.getInstance().convert(type, value, propertyName);
                } catch (IllegalArgumentException ex) {
                    throw new PropertyException("Could not perform type conversion", propertyName, ex).
                            setValue(value);
                }
            }
        }

        // Now set the value into the host object
        prop.setValue(host, value);
        return value;
    }

    /**
     * Follow (get) the named property on the host object.
     * If the getter returns null, then follow() creates an empty value and sets it back into the bean.
     * <p>
     * You cannot follow enum/primitive types as they are immutable.
     *
     * @param host   object to invoke the getter on
     * @param target the property to get or create
     * @param step   the property index
     * @return the existing or new empty value for the property
     * @throws PropertyException if problems encountered
     */
    private Object follow(Object host, PropertyPath target, int step)
            throws PropertyException {
        PropertyPath path = target.subPath(step);
        PropertyPath pathSansLastIndex = path.cloneOf().setIndex(step, QualifiedPath.NO_INDEX);
        String propertyName = target.getName(step);
        int index = target.getIndex(step);
        Property prop = getProperty(host.getClass(), propertyName);

        Common.fatalAssertion(prop != null, LOG, "Cannot find property {}", propertyName);

        // Get the current value of the property.
        // Note that Property#getValue checks that getters returning List<T> return a non-null value
        // i.e., follow() is not expected to create instances of generic values
        Object value = getValue(host, propertyName);

        // Determine if the property value class of this bean has been extended
        // First check whether this specific index has been extended
        assert prop != null;
        Class extClass = this.extensionClassMap.get(path);
        if (extClass == null) {
            // Otherwise check whether this property as a whole has been extended
            extClass = this.extensionClassMap.get(pathSansLastIndex);
        }

        // Determine if the property value factory method has been extended
        // First check whether this specific index has been extended
        String extFactory = this.extensionFactoryMap.get(path);
        if (extFactory == null) {
            // Otherwise check whether this property as a whole has been extended
            extFactory = this.extensionFactoryMap.get(pathSansLastIndex);
        }

        // If there is no existing value, create an empty value using the default or extended value class
        if (value == null) {
            if (prop.getValueClass().isEnum() || prop.getValueClass().isPrimitive()) {
                String message = String.format(
                        "Enum/Primitive types are immutable and cannot be followed. [%s] is of type [%s]",
                        propertyName, prop.getValueClass());
                throw new PropertyException(message, propertyName);
            } else {
                Class valueClazz = (extClass != null) ? extClass : prop.getValueClass();
                value = prop.emptyValue(valueClazz, extFactory);
            }
            // Set the empty value into the bean
            setValue(host, propertyName, value);
        } else {
            // Handle case where the existing value is a list
            if (value instanceof List<?>) {
                // The property is a List<T>, which means we must either get an existing entry or create a new one
                @SuppressWarnings({"unchecked"})
                List<Object> listOfValues = (List<Object>) value;

                if (index < listOfValues.size()) {
                    value = listOfValues.get(index);
                } else if (index == listOfValues.size()) {
                    Class valueClazz = (extClass != null) ? extClass : prop.getValueParameterClass();
                    value = prop.emptyValue(valueClazz, extFactory);
                    listOfValues.add(value);
                } else {
                    String message = String.format(
                            "IndexOutOfBounds: constraint is index <= size, but size = [%d] and index = [%d]",
                            listOfValues.size(), index);
                    throw new PropertyException(message, propertyName);
                }
            }
        }

        // Handle case where a type or element substitution has occurred
        if (value != null) {
            checkSubstitution(target, step, prop, index, value);
        }

        return value;
    }

    private void checkSubstitution(PropertyPath path, int step, Property prop, int index, Object value) {
        if (value instanceof JAXBElement) {
            substitutionLog.recordElementSubstitution(path, step, prop, index, (JAXBElement) value);
        } else if (value.getClass().isAnnotationPresent(XmlType.class)) {
            boolean noSubstitution = value.getClass().equals(prop.getValueClass()) ||
                    value.getClass().equals(prop.getValueParameterClass());
            // Ignore HREFs as we do not expect them to be commented
            if (!noSubstitution && !"href".equalsIgnoreCase(prop.getName())) {
                substitutionLog.recordTypeSubstitution(path, step, prop, index, value.getClass());
            }
        }
    }


    @Override
    public String toString() {
        return "BeanImpl(" +
                "underlying=" + underlying +
                ", extensionClassMap=" + extensionClassMap.toString() + "}";
    }
}
