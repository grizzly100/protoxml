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
import org.grizzlytech.protoxml.util.Common;
import org.grizzlytech.protoxml.util.Tokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.w3c.dom.Element;

import javax.xml.bind.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Marshall a populated BeanImpl into an XML document
 */
public class XMLMarshaller {

    private static final Logger LOG = LoggerFactory.getLogger(XMLMarshaller.class);
    private final List<String> schemaLocations = new ArrayList<>();
    private NamespacePrefixMapper namespacePrefixMapper = null;

    public XMLMarshaller() {
    }

    public void addSchemaLocations(String namespace, URL schemaURL) {
        assert Common.notEmpty(namespace);
        try {
            String location = URLDecoder.decode(schemaURL.getFile(), "UTF-8");
            this.schemaLocations.add(namespace);
            this.schemaLocations.add(location);
        } catch (UnsupportedEncodingException ex) {
            LOG.error("decoding URL", ex);
        }
    }

    public NamespacePrefixMapper getNamespacePrefixMapper() {
        return namespacePrefixMapper;
    }

    /**
     * Check with prefixMappingAdvised prior to setting
     *
     * @param namespacePrefixMapper the mapper to use
     */
    public void setNamespacePrefixMapper(NamespacePrefixMapper namespacePrefixMapper) {
        this.namespacePrefixMapper = namespacePrefixMapper;
    }

    protected void applyCommonProperties(Marshaller marshaller)
            throws PropertyException {
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty("com.sun.xml.internal.bind.xmlHeaders", getHeaderComment());

        // Prefix namespaces (if required)
        if (this.namespacePrefixMapper != null) {
            LOG.debug("Using namespacePrefixMapper");
            marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", this.namespacePrefixMapper);
        }

        // Specify schemaLocations (if provided)
        if (this.schemaLocations.size() > 0) {
            String schemaLocations = Common.listToString(this.schemaLocations, " ");
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocations);
        }
    }

    /**
     * Generate the XML text from the Document
     *
     * @return am XML String
     */
    public String toXML(Object rootElement) {
        LOG.info("toXML: {}", rootElement);
        String xmlData = "";

        // Create the header
        JAXBElement<?> header = XMLObjectFactory.getInstance().createObjectW(JAXBElement.class,
                rootElement, null);
        Common.fatalAssertion(header != null, LOG, "Unable to create JAXBElement for {}",
                Common.safeToName(rootElement));

        try {
            JAXBContext context = JAXBContext.newInstance(rootElement.getClass());

            // Create a marshaller and set properties
            Marshaller marshaller = context.createMarshaller();
            applyCommonProperties(marshaller);

            // Marshall
            StringWriter writer = new StringWriter();

            marshaller.marshal(header, writer);

            xmlData = writer.toString();
        } catch (JAXBException ex) {
            LOG.error("Unable to marshal Header", ex);
        }
        return xmlData;
    }

    /**
     * Marshall to a DOM, then iterate over the DOM, adding comments
     */

    public String toXMLWithComments(Object rootElement, Map<XMLPath, String> comments) {
        String xmlData = "";
        try {
            JAXBElement<?> header = XMLObjectFactory.getInstance().createObjectW(JAXBElement.class,
                    rootElement, null);

            Common.fatalAssertion(header != null, LOG, "Unable to create JAXBElement for {}",
                    Common.safeToName(rootElement));

            JAXBContext context = JAXBContext.newInstance(rootElement.getClass());

            // Create a marshaller and set properties
            Marshaller marshaller = context.createMarshaller();
            applyCommonProperties(marshaller);

            // Marshall into the DOM
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            marshaller.marshal(header, doc);

            // Decorate with comments. Start by finding the starting element
            Element startElementNode = doc.getDocumentElement();
            //comments.forEach((key, value) -> LOG.debug("Start comment: {}={}", key, value));
            decorateDOMWithComments(doc, startElementNode, null, comments);
            comments.forEach((key, value) -> LOG.warn("Unused comment: {}={}", key, value));

            // Transform
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();

            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // Marshall
            StringWriter writer = new StringWriter();
            t.transform(new DOMSource(doc), new StreamResult(writer));

            xmlData = writer.toString();
        } catch (JAXBException | ParserConfigurationException | TransformerException ex) {

            LOG.error("Unable to marshal Header: VIM.", ex);
        }

        return xmlData;
    }

    /**
     * NEED class in testdomain the uses substitution group and attribute renaming
     * <p>
     * Insert Comment nodes for children where a comment is needed
     * <p>
     * doc     the DOM document being edited
     *
     * @param element starting element node
     * @param path    null if root, else a.b.c.d
     *                getCommentText
     */
    private void decorateDOMWithComments(Document doc, Node element, String path, Map<XMLPath, String> comments) {
        // Only process Element nodes
        assert (element.getNodeType() == Node.ELEMENT_NODE);

        // Determine the number of children this element node has
        NodeList childNodes = element.getChildNodes();

        int siblingCount = childNodes.getLength();
        Node childNode = null;

        // First pass, count the children
        Map<String, Counter> counterMap = countChildElements(element);

        // Second pass, look for comments
        for (int child = 0; child < siblingCount; child++) {
            // Get the next child
            childNode = (childNode == null) ? childNodes.item(0) : childNode.getNextSibling();

            // If the child is en element, add a comment, and follow that element down
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                // Determine the element name and increment the associated "found" count
                String localName = childNode.getLocalName();
                Counter count = counterMap.computeIfPresent(localName, (k, v) -> v.incrementFound());

                // Construct the XML path to the element (zero indexing all elements)
                String childPath = ((path != null) ? path + XMLPath.XPATH_DELIMITER_S : "") + localName;
                if (count.total > 1) {
                    // Keep zero based for now, strictly speaking XPath is "1" based
                    childPath += "[" + (count.found - 1) + "]";
                }
                XMLPath childElementXMLPath = new XMLPath(childPath);

                // Lookup comments against the XML path
                // LOG.debug("Checking for comments: {}", childElementXMLPath);
                String commentText = comments.get(childElementXMLPath);

                // Decorate if a comment provided
                if (commentText != null) {
                    Comment comment = doc.createComment(commentText);
                    element.insertBefore(comment, childNode);
                    comments.remove(childElementXMLPath);
                }

                // Decorate child elements of this child element
                decorateDOMWithComments(doc, childNode, childPath, comments);
            }
        }
    }

    // Count the immediate child elements this node has
    private Map<String, Counter> countChildElements(Node element) {
        assert (element.getNodeType() == Node.ELEMENT_NODE);
        Map<String, Counter> result = new HashMap<>();
        NodeList childNodes = element.getChildNodes();
        int siblingCount = childNodes.getLength();
        // Loop through each, incrementing the count
        Node childNode = null;
        for (int child = 0; child < siblingCount; child++) {
            childNode = (childNode == null) ? childNodes.item(0) : childNode.getNextSibling();
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                String localName = childNode.getLocalName();
                // Create a new Counter for the name (if required) and increment it
                result.compute(localName, (k, v) -> (v == null ? new Counter() : v).incrementTotal());
            }
        }
        return result;
    }

    private String firstElseSecond(String s, String t) {
        return (Common.notEmpty(s) ? s : t);
    }

    private String getHeaderComment() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("<!-- Generated at %s -->%s", now.toString(), Tokens.NEWLINE_S);
    }

    private static class Counter {
        public int total = 0;
        public int found = 0;

        public Counter incrementTotal() {
            total++;
            return this;
        }

        public Counter incrementFound() {
            found++;
            return this;
        }

        public String toString() {
            return found + " of " + total;
        }
    }

}
