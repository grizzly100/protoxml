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
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ResourceResolver implements LSResourceResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceResolver.class);

    /**
     * List of XSDs available - paths relative to "resourcePaths"
     */
    private List<String> resourcePaths;

    private static String getFilename(String filePath) {
        return (new File(filePath)).getName();
    }

    /**
     * Return an LSInput that can be used to read the requested resource.
     *
     * @param type         usually "http://www.w3.org/2001/XMLSchema"
     * @param namespaceURI for example, "http://www.grizzlytech.org/testdomain/employee"
     * @param publicId     not used?
     * @param systemId     relative path to XSD, for example, "generated/employee.xsd"
     * @param baseURI      for example, "file:/C:/<project-dir>/target/test-classes/schema%5ccompany.xsd"
     */
    public LSInput resolveResource(String type, String namespaceURI,
                                   String publicId, String systemId, String baseURI) {
        //LOG.debug("type:[{}], namespaceURl:[{}], publicld:[{}], systemId:[{}], baseURl:[{}]",
        //        type, namespaceURI, publicId, systemId, baseURI);

        String systemIDName = getFilename(systemId);
        String systemIdPath = ResourceUtil.getResourcePath(systemIDName, resourcePaths);
        //LOG.debug("systemIdName:[{}], systemIdPath:[{}]", systemIDName, systemIdPath);

        InputStream resourceAsStream = null;
        if (systemIdPath != null) {
            // Read from Classpath using relative path
            // LOG.debug("Plan A: Reading systemIdPath:[{}] from ClassLoader.getResourceAsStream", systemIdPath);
            resourceAsStream = this.getClass().getClassLoader()
                    .getResourceAsStream(systemIdPath);
        } else {
            LOG.error("Unable to locate systemIdPath for systemId:[{}]", systemId);
        }

        if (systemIdPath != null && resourceAsStream == null) {
            try {
                LOG.debug("Plan B: Reading systemIdPath:[{}] direct from FileInputStream", systemIdPath);
                resourceAsStream = new FileInputStream(systemIdPath);
            } catch (IOException ex) {
                LOG.error("Unable to create FileInputStream for [{}]", systemId, ex);
            }
        }
        if (resourceAsStream == null) {
            LOG.error("Unable to locate systemId:[{}]", systemId);
        }

        return (resourceAsStream != null) ? new Input(publicId, systemId, resourceAsStream) : null;
    }

    public List<String> getResourcePaths() {
        return resourcePaths;
    }

    public void setResourcePaths(List<String> resourcePaths) {
        this.resourcePaths = resourcePaths;
    }
}
