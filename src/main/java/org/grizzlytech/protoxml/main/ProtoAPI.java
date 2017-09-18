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


import org.grizzlytech.protoxml.beans.Bean;
import org.grizzlytech.protoxml.builder.BeanBuilder;
import org.grizzlytech.protoxml.builder.NVPReader;
import org.grizzlytech.protoxml.builder.readers.NVPFileReader;
import org.grizzlytech.protoxml.util.ClassUtil;
import org.grizzlytech.protoxml.util.Common;
import org.grizzlytech.protoxml.util.Tokens;
import org.grizzlytech.protoxml.xml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XML Prototyping API
 */
public class ProtoAPI {

    private static final Logger LOG = LoggerFactory.getLogger(ProtoAPI.class);

    private final Map<Class, ElementList> registry = new HashMap<>();

    private final Config config = new Config();

    public Config getConfig() {
        return config;
    }

    /**
     * Read properties from the NVP file, create the XML file and then validate it
     *
     * @param nvpFile the property file
     * @param xmlFile the output XML file
     * @throws IOException if reading or writing errors occur
     */
    public void createAndValidateXMLFile(File nvpFile, File xmlFile)
            throws IOException {
        Common.fatalAssertion(
                (nvpFile != null && nvpFile.exists() && nvpFile.isFile()),
                LOG, "You must specify an input file. The output file is optional. [{}] [{}]",
                nvpFile, xmlFile);

        LOG.info("Reading from {} and Writing to {}", nvpFile, xmlFile);

        // Build Bean
        File traceFile = new File(xmlFile.getAbsoluteFile() + ".trc");
        Bean bean = createBean(nvpFile, traceFile);
        Common.fatalAssertion((bean != null) && (bean.unwrap() != null), LOG,
                "Unable to create bean");

        // Create XML
        assert bean != null;
        createXML(bean, xmlFile);

        // Validate XML vs Schema
        Class elementClass = bean.unwrap().getClass();
        URL schemaURL = getSchemaURL(elementClass);
        if (schemaURL != null) {
            ElementList elementList = getElementList(elementClass);
            File validationFile = new File(xmlFile.getAbsoluteFile() + ".val");
            validate(xmlFile, schemaURL, elementList.getResourceResolver(), validationFile);
        }
    }

    /**
     * Create a Bean from the provided property reader
     *
     * @param nvpReader   property reader emits NVPs
     * @param traceWriter all lines from property reader written to trace
     * @return the populated Bean
     */
    public Bean createBean(NVPReader nvpReader, Writer traceWriter) {
        BeanBuilder builder = new BeanBuilder();
        return builder.createBean(nvpReader, traceWriter);
    }

    /**
     * Create a Bean from the provided property file, and trace to the specified file
     *
     * @param nvpFile   property file
     * @param traceFile trace file
     * @return the populated Bean
     * @throws FileNotFoundException if nvpFile does not exist
     * @throws IOException           if problems are encountered reading nvpFile or writing to the traceFile
     */
    public Bean createBean(File nvpFile, File traceFile)
            throws IOException {
        NVPFileReader reader = new NVPFileReader();
        reader.setSourceFile(nvpFile);

        try (Writer traceWriter =
                     new OutputStreamWriter(new FileOutputStream(traceFile))) {
            return createBean(reader, traceWriter);
        }
    }

    /**
     * Create the XML representation of the Bean and write out the result
     *
     * @param rootElementBean XmlType representing the root element
     * @param xmlWriter       output writer for the XML
     * @throws IOException if file cannot be written
     */
    public void createXML(Bean rootElementBean, Writer xmlWriter)
            throws IOException {
        Object rootElement = rootElementBean.unwrap();
        ElementList elementList = getElementList(rootElement.getClass());
        LOG.debug("rootElementBean {} & elementList {}", rootElementBean, elementList);

        // Marshall the Document to XML
        XMLMarshaller marshaller = new XMLMarshaller();

        // Set the prefix mapper and schema locations
        if (elementList != null) {
            if (config.usePrefixMapper()) {
                marshaller.setNamespacePrefixMapper(elementList.getNamespacePrefixMapper());
            }
            if (config.outputSchemaLocations()) {
                ElementMetadata metadata = ElementListUtil.findByElementClass(elementList, rootElement.getClass());
                if (Common.notEmpty(metadata.getNamespace())) {
                    marshaller.addSchemaLocations(metadata.getNamespace(), metadata.getSchemaURL());
                }
            }
        }

        Map<XMLPath, String> comments = rootElementBean.getXmlPathComments();

        String xmlData;
        if (comments.size() > 0) {
            // This version must marshall to a DOM in order to add comments prior to transformation to XML
            xmlData = marshaller.toXMLWithComments(rootElement, comments);
        } else {
            // This version uses straight JAXB to marshall to XML
            xmlData = marshaller.toXML(rootElement);
        }

        // Output the XML to the output file or the info log
        if ((xmlWriter != null) && (xmlData != null)) {
            xmlWriter.write(xmlData);
        } else {
            LOG.error("toXML:{}{)", Tokens.NEWLINE_S, xmlData);
        }
    }

    /**
     * Create the XML representation of the Bean and write out the result
     *
     * @param rootElementBean XmlType representing the root element
     * @param xmlFile         output file for the XML
     * @throws IOException if file cannot be written
     */
    public void createXML(Bean rootElementBean, File xmlFile)
            throws IOException {
        try (Writer outputWriter =
                     new OutputStreamWriter(new FileOutputStream(xmlFile))) {
            createXML(rootElementBean, outputWriter);
        }
    }

    /**
     * Validate that an XML document passes the XSD validation rules
     *
     * @param xmlReader        reader providing access to the XML
     * @param schemaURL        URL to the XSD
     * @param resolver         resolver, needed should other XSDs be referenced
     * @param validationWriter output writer for validation results
     */
    public void validate(Reader xmlReader, URL schemaURL, LSResourceResolver resolver, Writer validationWriter) {
        Source xmlSource = new StreamSource(xmlReader);
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // Associate the schema factory with the resource resolver, which is responsible for resolving
        // the imported XSDs
        schemaFactory.setResourceResolver(resolver);

        try {
            LOG.info("schemaURL:{}", schemaURL);
            Schema schema = schemaFactory.newSchema(schemaURL);

            Validator validator = schema.newValidator();

            ErrorHandler handler = new AccumulatorErrorHandler();
            validator.setErrorHandler(handler);

            validator.validate(xmlSource);

            String results = handler.toString();

            validationWriter.write(results);
        } catch (IOException | SAXException ex) {
            LOG.error(xmlSource.getSystemId() + " is NOT valid", ex);
        }
    }

    /**
     * Validate that an XML document passes the XSD validation rules
     *
     * @param xmlFile        file holding the XML
     * @param schemaURL      URL to the XSD
     * @param resolver       resolver, needed should other XSDs be referenced
     * @param validationFile output file for validation results
     */
    public void validate(File xmlFile, URL schemaURL, LSResourceResolver resolver, File validationFile)
            throws IOException {

        try (Reader xmlReader =
                     new InputStreamReader(new FileInputStream(xmlFile));
             Writer validationWriter =
                     new OutputStreamWriter(new FileOutputStream(validationFile))
        ) {
            validate(xmlReader, schemaURL, resolver, validationWriter);
        }
    }

    /**
     * Get the SchemaURL associated with the given elementClass
     *
     * @param elementClass the element whose associated schemaURL is required
     * @return the URL
     */
    public URL getSchemaURL(Class elementClass) {
        URL schemaURL = null;
        ElementList elementList = getElementList(elementClass);
        ElementMetadata metadata = ElementListUtil.findByElementClass(elementList, elementClass);

        if (metadata != null) {
            schemaURL = metadata.getSchemaURL();
        } else {
            LOG.error("Cannot find elementClass [{}] within ElementList [{}]", elementClass.getCanonicalName(),
                    elementList.getClass().getCanonicalName());
        }

        return schemaURL;
    }

    /**
     * Create an XSD for the given class and output it to the provided Writer
     *
     * @param classes   array of classes to output
     * @param systemId  TBD
     * @param xsdWriter output writer
     * @throws JAXBException on XSD generation error
     * @throws IOException   on writing error
     */
    public void createXSD(Class[] classes, String systemId, Writer xsdWriter)
            throws JAXBException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance(classes);

        XMLSchemaOutputResolver sor = new XMLSchemaOutputResolver();
        sor.setSystemId(systemId);
        sor.setXSDWriter(xsdWriter);

        jaxbContext.generateSchema(sor);
    }

    /**
     * Create an XSD for the given class and output it to the provided file
     *
     * @param classes  array of classes to output
     * @param systemId TBD
     * @param xsdFile  output file
     * @throws JAXBException on XSD generation error
     * @throws IOException   on writing error
     */
    public void createXSD(Class[] classes, String systemId, File xsdFile)
            throws JAXBException, IOException {
        try (Writer xsdWriter =
                     new OutputStreamWriter(new FileOutputStream(xsdFile))
        ) {
            createXSD(classes, Common.isEmpty(systemId) ? xsdFile.getAbsolutePath() : systemId, xsdWriter);
        }
    }

    /**
     * Register the ElementList against all elementClasses that it registers
     *
     * @param elementList the list to register
     */
    public void setElementList(ElementList elementList) {
        elementList.getElements().forEach(e -> this.registry.put(e.getElementClass(), elementList));
    }

    /**
     * Get the ElementList associated with the elementClass
     * <p>
     * If a suitable ElementList has not been registered, then search the classpath for a match.
     *
     * @param elementClass the ElementList must register the elementClass
     * @return the ElementList (if registered)
     */
    public ElementList getElementList(Class elementClass) {
        ElementList elementList = this.registry.get(elementClass);

        if (elementList == null) {
            // Search the classpath
            elementList = findElementList(elementClass, elementClass.getPackage().getName());
            // If successful, register it
            if (elementList != null) {
                setElementList(elementList);
            }
        }
        return elementList;
    }

    /**
     * Find the ElementList that registered the elementClass by searching the classpath
     * <p>
     * The search starts in the same package as as elementClass, and widens upwards if a
     * suitable ElementList cannot be found.
     *
     * @param elementClass  the ElementList must register elementClass
     * @param searchPackage the package to search from
     * @return the ElementList (if found)
     */
    ElementList findElementList(Class elementClass, String searchPackage) {
        LOG.info("Searching package [{}] for an ElementList registering {}", searchPackage,
                elementClass.getCanonicalName());

        ElementList result;

        List<ElementList> allLists = ElementListUtil.createAllElementLists(searchPackage);

        result = allLists.stream()
                .filter(e -> ElementListUtil.findByElementClass(e, elementClass) != null)
                .findFirst().orElse(null);

        if (result == null && Common.notEmpty(searchPackage)) {
            // Widen the search scope to the parent package
            searchPackage = ClassUtil.getParentPackageName(searchPackage);
            result = findElementList(elementClass, searchPackage);
        }

        if (result == null) {
            LOG.error("FAILED: Searching package [{}] for an ElementList registering [{}]", searchPackage,
                    elementClass.getCanonicalName());
        }

        return result;
    }
}
