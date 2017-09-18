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


import org.grizzlytech.protoxml.util.QualifiedPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A path into an XML document
 */
public class XMLPath extends QualifiedPath {

    private static final Logger LOG = LoggerFactory.getLogger(XMLPath.class);

    final static public char XPATH_DELIMITER = '/';
    final static public String XPATH_DELIMITER_S = String.valueOf(XPATH_DELIMITER);

    public XMLPath(String path) {
        super(path);
    }

    public <P extends QualifiedPath> XMLPath(P path) {
        super(path);
    }

    public XMLPath(String path, String splitRegex) {
        super(path, splitRegex);
    }

    @Override
    public char pathDelimiter() {
        return XPATH_DELIMITER;
    }

    @Override
    protected String splitRegex() {
        return XPATH_DELIMITER_S;
    }

    /**
     * Case insensitive comparison
     * Indices padded to 3 wide (e.g., 001)
     * "No index" mapped to 0
     *
     * @return comparable string
     */
    @Override
    protected String computeComparableValue() {
        StringBuilder builder = new StringBuilder();
        for (int step = 0; step <= lastStep(); step++) {
            if (step > 0) {
                builder.append(pathDelimiter());
            }
            builder.append(getName(step).toUpperCase()); // case insensitive
            builder.append(CH_LB);
            builder.append(String.format("%03d", isIndexed(step) ? getIndex(step) : 0)); // pad 3 wide
            builder.append(CH_RB);
        }
        return builder.toString();
    }
}