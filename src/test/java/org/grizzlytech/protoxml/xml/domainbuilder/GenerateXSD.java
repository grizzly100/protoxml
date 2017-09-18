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

package org.grizzlytech.protoxml.xml.domainbuilder;


import org.grizzlytech.protoxml.main.ProtoAPI;
import org.grizzlytech.protoxml.util.TestPaths;
import org.grizzlytech.protoxml.xml.domainbuilder.employee.*;
import org.grizzlytech.protoxml.xml.domainbuilder.zoo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;

/**
 * Generate XSDs used by the unit tests
 */
public class GenerateXSD {
    private static final Logger LOG = LoggerFactory.getLogger(GenerateXSD.class);

    public static void main(String args[]) throws Exception {
        // Generate Employee XSD
        createSchema(new Class[]{Employee.class}, "employee.xsd");

        // Create Zoo XSD
        createSchema(new Class[]{Zoo.class, Penguin.class, Dolphin.class}, "zoo.xsd");
    }

    public static void createSchema(Class[] classes, String xsdFileName) throws Exception {
        // Point schema path to resources (if true) or test-classes (if false)
        String schemaPath = Paths.get(TestPaths.getTestResourcesDir().getAbsolutePath(),
                "schema", "generated", xsdFileName).toString();
        File schemaFile = new File(schemaPath);

        if (schemaFile.exists()) {
            if(!schemaFile.delete())
            {
                LOG.error("Failed to delete [{}]", schemaFile.getAbsoluteFile());
            }
        }

        // Create the Schema
        ProtoAPI api = new ProtoAPI();
        api.createXSD(classes, null, schemaFile);
    }

}
