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

package org.grizzlytech.protoxml.xml;


import org.grizzlytech.protoxml.util.AbstractObjectFactory;
import org.grizzlytech.protoxml.util.ClassUtil;
import org.grizzlytech.protoxml.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import java.lang.reflect.Field;

/**
 * Extended to handle JAXB generated object factories
 */
public class XMLObjectFactory extends AbstractObjectFactory {

    private static final Logger LOG = LoggerFactory.getLogger(XMLObjectFactory.class);

    private static final XMLObjectFactory INSTANCE = new XMLObjectFactory();

    public static XMLObjectFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public <W, T> W createObjectW(Class<W> clazz, T factoryParameter, String factoryMethodName) {
        W result;

        LOG.trace("createObjectW: clazz [{}] factoryParameter [{}] factoryMethodName [{}]", clazz,
                Common.safeToName(factoryParameter), factoryMethodName);

        if (!JAXBElement.class.equals(clazz)) {
            // The override implementation only handles JAXBElement creation
            return super.createObjectW(clazz, factoryParameter, factoryMethodName);
        }

        // Start with provided arguments
        Class nextParameterClass = factoryParameter.getClass();
        String nextMethodName = factoryMethodName;

        do {
            result = super.createObjectW(clazz, factoryParameter, nextMethodName);

            if (result == null) {
                // The Object Factory does not contain a create method for the JAXBElement
                // Try the parent class of the ComplexType, assuming it is also an @XmlType
                Class superclazz = ClassUtil.getSuperclassElseNull(nextParameterClass);
                if (superclazz != null && superclazz.isAnnotationPresent(XmlType.class)) {
                    LOG.debug("createObjectW: Could not create JAXBElement for {} so trying {}",
                            nextParameterClass.getCanonicalName(), superclazz.getCanonicalName());
                    // Update the "next" variables for the next try
                    nextMethodName = getDefaultFactoryMethodName(clazz, superclazz);
                    nextParameterClass = superclazz;
                } else {
                    nextMethodName = null; // no option on the table
                }
            }
        } while (result == null && nextMethodName != null);

        Common.fatalAssertion(result != null, LOG, "createObjectW: Failed for {}({})", Common.safeToName(clazz),
                Common.safeToName(factoryParameter));
        return result;
    }


    @Override
    public <T> T createObject(Class<T> clazz, String factoryMethodName) {
        T result = super.createObject(clazz, factoryMethodName);
        Common.fatalAssertion(result != null, LOG, "createObject for {}", Common.safeToName(clazz));
        return result;
    }

    /**
     * By default, make the assumption that for a given ComplexType, there will
     * be a corresponding Element of the same name in the same namespace (Java package)
     * <p>
     * This assumption can be false, for example if:
     * 1. multiple Elements are defined using the same ComplexType
     * 2. a ComplexType has been sub-classed in a different namespace to the original Element
     *
     * @return the name of the factory method to use
     */
    @Override
    public String getDefaultFactoryMethodName(Class clazz) {
        assert clazz.isAnnotationPresent(XmlType.class);
        return getObjectFactoryClassName(clazz) + "#create" + clazz.getSimpleName();
    }

    /**
     * By default, make the assumption that for a given Element of name "foo", and type "Bar"
     * there will be a corresponding factory method "JAXBElement<Bar> createFoo(Bar value)".
     * <p>
     * In the trivial case, both the element name and the type are both called "foo"/"Foo"
     * This method returns the trivial assumption.
     * <p>
     * This assumption can be false, for example if Foo is being used in a Substitution Group,
     * substituting
     *
     * @return the name of the factory method to use
     */
    @Override
    public String getDefaultFactoryMethodName(Class clazz, Class inner) {
        assert JAXBElement.class.equals(clazz);
        assert inner.isAnnotationPresent(XmlType.class);
        return getObjectFactoryClassName(inner) + "#create" + inner.getSimpleName();
    }

    /**
     * Get the class name of the ObjectFactory associated with the ComplexType
     */
    private String getObjectFactoryClassName(Class xmlType) {
        final String OBJECT_FACTORY_CLASSNAME = ".ObjectFactory";
        return xmlType.getPackage().getName() + OBJECT_FACTORY_CLASSNAME;
    }

    private Object getFactoryForType(Class xmlType) {
        Class factoryClass = ClassUtil.getClassElseNull(getObjectFactoryClassName(xmlType));
        return OBJECT_FACTORIES.computeIfAbsent(factoryClass, ClassUtil::newInstanceElseNull);
    }

    /**
     * private final static QName _<SimpleName>_QNAME = new QName(<namespace>, <elementName>);
     */
    public QName getElementQName(Class xmlType, boolean errorIfNotFound) {
        final String fieldName = "_" + xmlType.getSimpleName() + "_QNAME";
        Object factory = null;
        QName result = null;
        try {
            factory = getFactoryForType(xmlType);
            Field field = factory.getClass().getDeclaredField(fieldName);
            result = getFieldValueElseNull(factory, field, QName.class);
        } catch (NoSuchFieldException ex) {
            if (errorIfNotFound) {
                LOG.error("Cannot find [{}] in [{}]", fieldName, Common.safeToName(factory));
            }
        }
        return result;
    }

    // Helper for getObjectFactoryQNames()
    @SuppressWarnings("unchecked")
    private <T> T getFieldValueElseNull(Object target, Field field, Class<T> clazz) {
        T result = null;
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            result = (T) field.get(target);
        } catch (IllegalAccessException | SecurityException | ClassCastException ex) {
            LOG.error("Cannot retrieve field [{}] for target [{}]", field.toString(),
                    target.getClass().getCanonicalName(), ex);
        }
        return result;
    }
}
