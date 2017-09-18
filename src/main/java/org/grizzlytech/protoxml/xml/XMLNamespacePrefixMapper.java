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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of (link NamespacePrefixMapper) that maps the schema namespaces more to readable names.
 * Used by the JAXB marshaller.
 * <p>
 * Requires setting the property "com.sun.xml.internal.bind.namespacePrefixMapper" to an instance
 * of this class.
 * Results in a dependency on the JAXB implementation jars
 * </p>
 */
public class XMLNamespacePrefixMapper extends NamespacePrefixMapper {

    private static final Logger LOG = LoggerFactory.getLogger(XMLNamespacePrefixMapper.class);

    private final Map<String, String> namespaceMap = new HashMap<>();

    public XMLNamespacePrefixMapper() {
    }

    public void register(String namespaceUri, String preferredPrefix) {
        this.namespaceMap.put(namespaceUri.toUpperCase(), preferredPrefix);
    }

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        String prefix = namespaceMap.getOrDefault(namespaceUri.toUpperCase(), suggestion);

        LOG.debug("Preferred prefix for [{}] set to [{}], suggestion was [{}], requires [{}]",
                namespaceUri, prefix, suggestion, requirePrefix);
        return prefix;
    }
}
