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

package org.grizzlytech.protoxml.builder;


import org.grizzlytech.protoxml.beans.Bean;
import org.grizzlytech.protoxml.beans.PropertyDebugger;
import org.grizzlytech.protoxml.beans.PropertyException;
import org.grizzlytech.protoxml.builder.readers.NVPStringReader;
import org.grizzlytech.protoxml.util.AssertUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testdomain.employee.Employee;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BeanBuilderTest {
    private static final Logger LOG = LoggerFactory.getLogger(BeanBuilderTest.class);

    public static int getID(Bean bean, String path, String args[]) {
        int id = -1;
        if ("id".equals(path) && "bob".equals(args[0])) {
            id = 99;
        }
        return id;
    }

    @Test
    public void createEmployeeBean() throws PropertyException {
        NVPStringReader reader = new NVPStringReader();

        List<String> mappings = new ArrayList<>();
        mappings.add("doctype=testdomain.employee.Employee");
        mappings.add("name=bob");
        mappings.add("name#=person name");
        mappings.add("salary=1234.30");
        mappings.add("ADDRESS.CITY=New York");
        mappings.add("#phone numbers");
        mappings.add("PHoNES[0]#=mobile");
        mappings.add("phones[0].LocalNumber=999");
        mappings.add("honey=abc");
        mappings.add("id=! org.grizzlytech.protoxml.builder.BeanBuilderTest # getID( bob )");

        reader.setMappings(mappings);

        StringWriter traceWriter = new StringWriter();

        BeanBuilder builder = new BeanBuilder();
        Bean bean = builder.createBean(reader, traceWriter);
        String traceText = traceWriter.toString();

        LOG.info(traceText);

        // Test bean created
        assertTrue(bean.unwrap() != null);
        assertTrue(bean.unwrap() instanceof Employee);

        Employee emp = (Employee) bean.unwrap();
        assertEquals(emp.getId(), 99);

        assertEquals(bean.getPathValue("id", Integer.class).getValue(), new Integer(99));

        // Test trace output produced, and that capitalization has been fixed
        AssertUtil.assertContains(LOG, traceText,
                new String[]{"address.city=New York", "phones[0]#=mobile", "honey=abc [FAIL]"}
        );

        LOG.info(PropertyDebugger.objectToString(bean.unwrap()));
    }

    @Test
    public void createZooBean() {
        NVPStringReader reader = new NVPStringReader();

        List<String> mappings = new ArrayList<>();
        mappings.add("doctype=testdomain.zoo.Zoo");
        mappings.add("name=London Zoo");
        mappings.add("mascot$=testdomain.zoo.Penguin");
        mappings.add("mascot.id=1");
        mappings.add("mascot.nickname=Charles");
        mappings.add("mascot.fishPerDay=5");
        mappings.add("animals[0]$=testdomain.zoo.Dolphin");
        mappings.add("animals[0].id=2");
        mappings.add("animals[0].nickname=Claire");
        mappings.add("animals[0].length=20");

        reader.setMappings(mappings);

        StringWriter traceWriter = new StringWriter();

        BeanBuilder builder = new BeanBuilder();
        Bean bean = builder.createBean(reader, traceWriter);
        String traceText = traceWriter.toString();

        LOG.info(PropertyDebugger.objectToString(bean.unwrap()));
        LOG.info(traceText);

        // Test bean created
        assertTrue(bean.unwrap() != null);

        // Test trace output produced, and that capitalization has been fixed
        AssertUtil.assertContains(LOG, traceText,
                new String[]{"name=London Zoo", "mascot.fishPerDay=5", "animals[0].nickname=Claire"}
        );

    }

}
