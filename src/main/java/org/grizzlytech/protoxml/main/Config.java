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

package org.grizzlytech.protoxml.main;

/**
 * Configuration for the ProtoAPI
 */
public class Config {
    /**
     * Use the mapper to prefix namespaces with the provided prefixes (instead of ns1, ns2 etc.)
     */
    private boolean usePrefixMapper = true;
    /**
     * Output the location of the schema containing the root element
     *  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     *  xsi:schemaLocation="http://www.foo.org/bar /C:/Dev/../schema\foo\bar.xsd"
     */
    private boolean outputSchemaLocations = false;

    public boolean usePrefixMapper() {
        return usePrefixMapper;
    }

    public void usePrefixMapper(boolean usePrefixMapper) {
        this.usePrefixMapper = usePrefixMapper;
    }

    public boolean outputSchemaLocations() {
        return outputSchemaLocations;
    }

    public void outputSchemaLocations(boolean outputSchemaLocations) {
        this.outputSchemaLocations = outputSchemaLocations;
    }
}
