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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Writer;

public class XMLSchemaOutputResolver extends SchemaOutputResolver {

    private static final Logger LOG = LoggerFactory.getLogger(XMLSchemaOutputResolver.class);

    private Writer xsdWriter;

    private String systemId;

    public XMLSchemaOutputResolver() {
    }

    @Override
    public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
        LOG.info("createOutput: namespaceUri [{}] & suggestedFileName [{}]", namespaceUri, suggestedFileName);

        // Create stream result
        StreamResult result = new StreamResult(xsdWriter);

        // Set system id
        result.setSystemId(systemId);

        return result;
    }

    public Writer getXSDWriter() {
        return xsdWriter;
    }

    public void setXSDWriter(Writer xsdWriter) {
        this.xsdWriter = xsdWriter;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }
}
