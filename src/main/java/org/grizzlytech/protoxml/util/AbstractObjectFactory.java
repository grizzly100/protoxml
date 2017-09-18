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

package org.grizzlytech.protoxml.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.grizzlytech.protoxml.util.Tokens.CLASS_METHOD_DELIMITER_S;

/**
 * Abstract Object Factory
 * <p>
 * Assumes that objects can be created in two ways - no argument and single argument factory methods
 *
 * @See XMLObjectFactory
 */
public abstract class AbstractObjectFactory {

    // Cache of Object Factories
    protected static final Map<Class, Object> OBJECT_FACTORIES = new HashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(AbstractObjectFactory.class);

    /**
     * Return a list of methods that are candidates to be factory methods
     *
     * @param factoryMethodName fully qualified class#method name
     * @param criteria          any further criteria to apply
     * @return matching method(s)
     */
    public static List<Method> matchFactoryMethods(String factoryMethodName, Predicate<Method> criteria) {
        List<Method> matches = null;
        String parts[] = factoryMethodName.split(CLASS_METHOD_DELIMITER_S);
        if (parts.length == 2) {
            final String className = parts[0].trim();
            final String methodName = parts[1].trim();
            Class factoryClass = ClassUtil.getClassElseNull(className);
            Common.fatalAssertion(factoryClass != null, LOG, "factoryClass is null for {}", className);

            if (factoryClass != null && Common.notEmpty(methodName)) {
                matches = Arrays.stream(factoryClass.getDeclaredMethods())
                        .filter(m -> Modifier.isPublic(m.getModifiers()))
                        .filter(m -> m.getParameterCount() <= 1)
                        .filter(m -> !Void.class.equals(m.getReturnType()))
                        .filter(m -> m.getName().equalsIgnoreCase(methodName))
                        .filter(criteria)
                        .collect(Collectors.toList());
            }
        }

        return matches;
    }

    // Helper for case where the default factoryMethod is requested
    public <T> T createObject(Class<T> clazz) {
        return createObject(clazz, null);
    }

    /**
     * Create an instance of clazz using the provided factory method
     *
     * @param clazz             instance of this class is required
     * @param factoryMethodName method which returns an instance of the class
     * @param <T>               the type of the class
     * @return empty instance of T
     */
    public <T> T createObject(Class<T> clazz, String factoryMethodName) {
        T result = null;
        try {
            // Use default name if one is not provided
            if (factoryMethodName == null) {
                factoryMethodName = getDefaultFactoryMethodName(clazz);
            }
            // Find the method
            Method factoryMethod = getFactoryMethod(factoryMethodName);

            if (factoryMethod != null) {
                //noinspection unchecked
                result = (T) factoryMethod.invoke(getFactory(factoryMethod));
            }
        } catch (IllegalAccessException ex) {
            LOG.error("Error invoking " + factoryMethodName, ex);
        } catch (InvocationTargetException ex) {
            LOG.error("Error invoking " + factoryMethodName, ex.getCause());
        }
        return result;
    }

    // Helper for case where the default factoryMethod is requested
    public <W, T> W createObjectW(Class<W> clazz, T factoryParameter) {
        return createObjectW(clazz, factoryParameter, null);
    }

    /**
     * Create an instance of clazz using the provided factory method
     *
     * @param clazz             instance of this class is required
     * @param factoryParameter  parameter to the factory method
     * @param factoryMethodName method which returns an instance of the class
     * @param <T>               the type of the parameter class
     * @param <W>               the type of the outer class
     * @return empty instance of W
     */
    public <W, T> W createObjectW(Class<W> clazz, T factoryParameter, String factoryMethodName) {
        W result = null;
        try {
            // Use default name if one is not provided
            if (factoryMethodName == null) {
                factoryMethodName = getDefaultFactoryMethodName(clazz, factoryParameter.getClass());
            }
            // Find the method
            Method factoryMethod = getFactoryMethodW(factoryMethodName, factoryParameter.getClass());
            // Invoke it
            if (factoryMethod != null) {
                //noinspection unchecked
                result = (W) factoryMethod.invoke(getFactory(factoryMethod), factoryParameter);
            }
        } catch (IllegalAccessException ex) {
            LOG.error("Error invoking " + factoryMethodName, ex);
        } catch (InvocationTargetException ex) {
            LOG.error("Error invoking " + factoryMethodName, ex.getCause());
        }
        return result;
    }

    /**
     * Return the Method identified by the class#method name
     *
     * @param factoryMethodName class#method
     * @return the Method or null
     */
    public Method getFactoryMethod(String factoryMethodName) {
        assert Common.notEmpty(factoryMethodName);
        Method result = null;
        List<Method> candidates = matchFactoryMethods(factoryMethodName, m -> m.getParameterCount() == 0);

        if (candidates != null && candidates.size() == 1) {
            result = candidates.get(0);
        } else {
            LOG.debug("factoryMethod: Null or Non-unique match for [{}]: {}", factoryMethodName,
                    Common.listToString(candidates, ", "));
        }
        return result;
    }

    /**
     * Return the Method identified by the class#method name, that takes a single parameter
     *
     * @param factoryMethodName class#method
     * @param factoryParameter  the parameter
     * @param <T>               the type of the parameter class
     * @return the Method or null
     */
    public <T> Method getFactoryMethodW(String factoryMethodName, Class<T> factoryParameter) {
        assert Common.notEmpty(factoryMethodName);
        Method result = null;
        List<Method> candidates = matchFactoryMethods(factoryMethodName,
                m -> m.getParameterCount() == 1 && m.getParameterTypes()[0].isAssignableFrom(factoryParameter));

        if (candidates != null && candidates.size() == 1) {
            result = candidates.get(0);
        } else {
            LOG.debug("factoryMethodW: Null or Non-unique match named [{}]: {}", factoryMethodName,
                    Common.listToString(candidates, ", "));
        }
        return result;
    }

    /**
     * Get the factory class that implements the method
     */
    protected Object getFactory(Method factoryMethod) {
        assert factoryMethod != null;
        Object factory = null;
        if (!Modifier.isStatic(factoryMethod.getModifiers())) {
            factory = OBJECT_FACTORIES.computeIfAbsent(factoryMethod.getDeclaringClass(),
                    ClassUtil::newInstanceElseNull);
            Common.fatalAssertion(factory != null, LOG, "Unable to create factory for {}",
                    Common.safeToName(factoryMethod));
        }
        return factory;
    }

    public abstract String getDefaultFactoryMethodName(Class clazz);

    public abstract String getDefaultFactoryMethodName(Class clazz, Class inner);
}
