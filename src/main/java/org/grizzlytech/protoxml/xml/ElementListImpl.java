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


import org.grizzlytech.protoxml.util.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Default implementation of an ElementList.
 * <p>
 * For resource resolution, this implementation leverages ResourceUtil to identify a list of available
 * schema files located under getRelativeSchemaPath().
 * <p>
 * It is assumed that schemas have a unique name - e.g., the folder structures does not contain multiple XSDs
 * with the same name. This keep resolution easy.
 */
public abstract class ElementListImpl implements ElementList {

    public static final Predicate<String> XSD_FILTER = x -> x.toLowerCase().endsWith("xsd");
    private static final Logger LOG = LoggerFactory.getLogger(ElementListImpl.class);
    private final List<ElementMetadata> metadataList = new ArrayList<>();

    private final XMLNamespacePrefixMapper namespacePrefixMapper = new XMLNamespacePrefixMapper();

    private boolean initialized = false;

    /**
     * List of available XSDs
     */
    private List<String> resourcePaths = null;

    /**
     * Initialise the ElementList
     * Step 1. Locate all XSDs within the resourcePaths folder of the resources path
     * Step 2. Register each elementClass needed
     */
    public void init() {
        if (!initialized) {
            // Cache the path to all schemas under the searchPath
            this.resourcePaths = ResourceUtil.getResourcePaths(getClassLoader(),
                    getRelativeSchemaPath().normalize(), XSD_FILTER);
            // Register elements and namespace preferences
            registerAll();
            initialized = true;
        }
    }

    public List<ElementMetadata> getElements() {
        return this.metadataList;
    }

    public ResourceResolver getResourceResolver() {
        ResourceResolver resolver = new ResourceResolver();
        resolver.setResourcePaths(this.resourcePaths);
        return resolver;
    }

    public XMLNamespacePrefixMapper getNamespacePrefixMapper() {
        return this.namespacePrefixMapper;
    }

    // HELPERS

    /**
     * @return relative path of schema folder
     */
    protected abstract Path getRelativeSchemaPath();

    /**
     * Register each element by invoking register()
     * Register namespace prefixes by calling getRelativeSchemaPath and invoking register()
     */
    protected abstract void registerAll();

    /**
     * Add the element and associated schema resource to the metadataList
     *
     * @param elementClass the class to be registered
     * @param resourceName resource containing the XML Schema
     */
    protected void register(Class<?> elementClass, String resourceName) {
        assert (elementClass != null);
        // Get the URL for the named resource
        URL schemaURL = ResourceUtil.getResourceURLByName(getClassLoader(), resourceName, this.resourcePaths);

        // Look-up namespace via QName
        QName qname = XMLObjectFactory.getInstance().getElementQName(elementClass, true);
        String namespace = null;
        if (qname != null) {
            namespace = qname.getNamespaceURI();
        } else {
            LOG.warn("Cannot determine namespace that defines [{}]", elementClass.getSimpleName());
        }

        // Create and add the metadata
        ElementMetadata elementMD = new ElementMetadata(elementClass, namespace, schemaURL);
        metadataList.add(elementMD);
    }

    protected ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}

