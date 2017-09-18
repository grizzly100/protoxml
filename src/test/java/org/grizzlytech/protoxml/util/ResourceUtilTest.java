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


import org.grizzlytech.protoxml.xml.ElementListImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.assertTrue;

public class ResourceUtilTest {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceUtilTest.class);

    final Predicate<String> XSD_FILTER = x -> x.toLowerCase().endsWith("xsd");

    final static String SCHEMA = "books.xsd";

    @Test
    public void getResourcePaths() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        List<String> resourcePaths = ResourceUtil.getResourcePaths(classLoader, Paths.get("schema"), ElementListImpl.XSD_FILTER);
        assertTrue(resourcePaths != null);
        resourcePaths.forEach(x -> LOG.info("Path Element:{}", x));

        String resourcePath = ResourceUtil.getResourcePath(SCHEMA, resourcePaths);
        assertTrue(resourcePath != null);
        LOG.info("resourcePath={}", resourcePath);

        URL resourceURL = ResourceUtil.getResourceURLByName(classLoader, SCHEMA, resourcePaths);
        assertTrue(resourceURL != null);
        LOG.info("resourceURL={}", resourceURL);
    }

    @Test(expected = FatalException.class)
    public void findResourcesInDir() throws Exception {
        File dir = Paths.get(TestPaths.getTestResourcesDir().getAbsolutePath(),
                "schema", "missing-dir", "generated").toFile();
        ResourceUtil.findResourcesInDir(dir, Paths.get("generated"), XSD_FILTER);
    }
}
