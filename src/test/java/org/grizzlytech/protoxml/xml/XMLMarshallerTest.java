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


import org.grizzlytech.protoxml.beans.Bean;
import org.grizzlytech.protoxml.beans.BeanTest;
import org.grizzlytech.protoxml.builder.BeanBuilder;
import org.grizzlytech.protoxml.builder.readers.NVPStringReader;
import org.grizzlytech.protoxml.main.ProtoAPI;
import org.grizzlytech.protoxml.util.AssertUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class XMLMarshallerTest {

    private static final Logger LOG = LoggerFactory.getLogger(XMLMarshallerTest.class);

    @Test
    public void employeeToXML() throws Exception {
        Bean bean = (new BeanTest()).createEmployeeBean();
        String[] assertions = new String[]{"city>new york", "<nse:localNumber>999"};
        toXML(bean, assertions);
    }

    @Test
    public void zooToXML() throws Exception {
        Bean bean = (new BeanTest()).createZooBean();
        String[] assertions = new String[]{"mascot xsi:type=\"nsz:penguin\"", "<nsz:length>20"};
        toXML(bean, assertions);
    }

    @Test
    public void companyToXML() throws Exception {
        Bean bean = (new BeanTest()).createCompanyBean();
        String[] assertions = new String[]{"http://www.grizzlytech.org/testdomain/company", "<nse:name>Fred"};
        toXML(bean, assertions);
    }

    private void toXML(Bean bean, String[] assertions) throws Exception {

        ProtoAPI api = new ProtoAPI();
        api.getConfig().usePrefixMapper(true);

        StringWriter xmlWriter = new StringWriter();
        api.createXML(bean, xmlWriter);
        String xmlText = xmlWriter.toString();

        LOG.info(xmlText);
        AssertUtil.assertContains(LOG, xmlText, assertions);

        // Validate
        Class elementClass = bean.unwrap().getClass();
        ElementList elementList = api.getElementList(elementClass);
        URL schemaURL = api.getSchemaURL(elementClass);
        assertTrue(elementList != null && schemaURL != null);
        StringReader xmlReader = new StringReader(xmlText);
        StringWriter valWriter = new StringWriter();
        api.validate(xmlReader, schemaURL, elementList.getResourceResolver(), valWriter);
        LOG.info("Validate: {}", valWriter.toString());
    }

    @Test
    public void toXMLWithComments() throws Exception {
        Bean bean = (new BeanTest()).createEmployeeBean();

        // Add a comment
        bean.setPathComment("address.city#", "home city");
        bean.setPathComment("phones[0]#", "mobile");

        // Generate XML
        ProtoAPI api = new ProtoAPI();
        api.getConfig().usePrefixMapper(true);

        StringWriter xmlWriter = new StringWriter();
        api.createXML(bean, xmlWriter);
        String xmlText = xmlWriter.toString();

        LOG.info("{}", xmlText);
        // Remove all whitespace to make assertion easier
        xmlText = AssertUtil.removeWhitespace(xmlText);

        AssertUtil.assertContains(LOG, xmlText,
                new String[]{"<!--homecity-->"}
        );
    }

    @Test
    public void toXMLWithCommentsComplexCases() throws Exception {
        NVPStringReader reader = new NVPStringReader();

        List<String> mappings = new ArrayList<>();
        mappings.add("doctype=testdomain.music.Library");
        mappings.add("name=top albums");
        mappings.add("format[0]&=testdomain.music.ObjectFactory#createCD");
        mappings.add("format[0]$=testdomain.music.Album");
        mappings.add("format[0].value.artist#=we like foo");
        mappings.add("format[0].value.artist=foo");
        mappings.add("format[0].value.albumTitle=bar");
        mappings.add("format[1]&=testdomain.music.ObjectFactory#createDigital");
        mappings.add("format[1]$=testdomain.music.DigitalAlbum");
        mappings.add("format[1].value.artist=c64");
        mappings.add("format[1].value.albumTitle=commando");
        mappings.add("format[1].value.bitRate=320");
        mappings.add("format[1].value.bitRate#=nice");
        mappings.add("format[2]&=testdomain.music.ObjectFactory#createCD");
        mappings.add("format[2]$=testdomain.music.Album");
        //TODO: currently the comment get mapped to the first CD, not the second
        mappings.add("format[2].value.artist#=excellent");
        mappings.add("format[2].value.artist=baz");
        mappings.add("format[2].value.albumTitle=the man");

        reader.setMappings(mappings);

        StringWriter traceWriter = new StringWriter();

        BeanBuilder builder = new BeanBuilder();
        Bean bean = builder.createBean(reader, traceWriter);
        String traceText = traceWriter.toString();

        LOG.info(traceText);

        // Generate XML
        ProtoAPI api = new ProtoAPI();
        api.getConfig().usePrefixMapper(false);

        StringWriter xmlWriter = new StringWriter();
        api.createXML(bean, xmlWriter);
        String xmlText = xmlWriter.toString();

        LOG.info("{}", xmlText);
    }
}
