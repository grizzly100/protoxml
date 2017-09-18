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


import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;
import org.w3c.dom.ls.LSResourceResolver;

import java.util.List;

/**
 * An ElementList holds a list of JAXB Elements and their associated XML Schema documents.
 * The implementation also provides a LSResourceResolver so that XML documents can be validated.
 */
public interface ElementList {

    /**
     * Initialise the element list
     */
    void init();

    /**
     * @return all root elements
     */
    List<ElementMetadata> getElements();

    /**
     * To enable schema validation, it may be necessary to retrieve additional schemas
     * referenced by the first schema.
     */
    LSResourceResolver getResourceResolver();

    /**
     * To enhance XML generation, it may be preferable to override the default ns1, ns2, .. naming
     */
    NamespacePrefixMapper getNamespacePrefixMapper();
}
