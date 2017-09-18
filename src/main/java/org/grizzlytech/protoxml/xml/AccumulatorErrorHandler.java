package org.grizzlytech.protoxml.xml;


import org.grizzlytech.protoxml.util.Tokens;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.LinkedList;
import java.util.List;

/**
 * Accumulate exceptions whilst building XML document.
 */
public class AccumulatorErrorHandler implements ErrorHandler {

    final List<SAXParseException> exceptions = new LinkedList<>();
    private int warningCount = 0;
    private int errorCount = 0;
    private int fatalCount = 0;

    @Override
    public void warning(SAXParseException e) throws SAXException {
        exceptions.add(e);
        warningCount++;
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        exceptions.add(e);
        errorCount++;
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        exceptions.add(e);
        fatalCount++;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        // Scores
        text.append(String.format("Summary: Fatal (%s) Error (%s) Warning (%s)%s", fatalCount,
                errorCount, warningCount, Tokens.NEWLINE_S));

        for (SAXParseException e : this.exceptions) {
            text.append(String.format("Line %s (%s): %s", e.getLineNumber(), e.getColumnNumber(), e.getLocalizedMessage()));
            text.append(Tokens.NEWLINE_S);
        }
        return text.toString();
    }
}
