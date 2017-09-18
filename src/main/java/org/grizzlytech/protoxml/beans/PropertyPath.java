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

package org.grizzlytech.protoxml.beans;


import org.grizzlytech.protoxml.util.Common;
import org.grizzlytech.protoxml.util.QualifiedPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes a "path" (which is defined as a a fully qualified property name) and determines
 * the embedded path segments and indices.
 *
 * @See BeanBuilder
 */
public class PropertyPath extends QualifiedPath {

    final static public char PATH_DELIMITER = '.';
    final static public String PATH_DELIMITER_S = String.valueOf(PATH_DELIMITER);
    final static public String PATH_REGEX = "\\."; // Need the forward slashes
    private static final Logger LOG = LoggerFactory.getLogger(PropertyPath.class);
    private String[] xmlNames;

    public PropertyPath() {
    }

    public PropertyPath(String path) {
        super(path);
    }

    @Override
    public char pathDelimiter() {
        return PATH_DELIMITER;
    }

    @Override
    protected String splitRegex() {
        return PATH_REGEX;
    }

    /**
     * Swap names for xml names (if set)
     *
     * @return new path
     */
    public PropertyPath withXmlNames() {
        PropertyPath xmlPath = cloneOf();
        for (int step = 0; step <= lastStep(); step++) {
            String xmlName = getXmlName(step);
            if (Common.notEmpty(xmlName)) {
                xmlPath.setName(step, xmlName);
                xmlPath.setXmlName(step, null);
            }
        }
        return xmlPath;
    }

    public String getXmlName(int step) {
        return this.xmlNames[step];
    }

    /**
     * BeanBuilder will use this setter to record the xml path
     * JAXB may have attribute overrides, and hence path != xmlPath
     *
     * @param step    step is zero based, a.b.c has 3 steps
     * @param xmlName the XML name
     */
    public void setXmlName(int step, String xmlName) {
        this.xmlNames[step] = xmlName;
    }

    /**
     * Converts a given path string to a Path and resolves it against this Path
     *
     * @param other .foo or ..bar
     * @return the resolved path
     */
    public PropertyPath resolve(String other) {
        PropertyPath resolvedPath;
        int backSteps = Common.leadingCharCount(other, PATH_DELIMITER);
        if (backSteps >= 1 && lastStep() >= backSteps) {
            int joinStep = lastStep() - backSteps; // steps are zero based
            String resolution = getPath(joinStep) + other.substring(backSteps - 1, other.length());
            resolvedPath = new PropertyPath(resolution);
        } else {
            throw new IllegalArgumentException(
                    Common.concatenate("Cannot resolve [", other, "] relative to [", toString(), "]"));
        }
        return resolvedPath;
    }

    /**
     * Case insensitive comparison, indices padded to 3 wide (e.g., 001)
     *
     * @return upper case path
     */
    @Override
    protected String computeComparableValue() {
        StringBuilder builder = new StringBuilder();
        for (int step = 0; step <= lastStep(); step++) {
            if (step > 0) {
                builder.append(pathDelimiter());
            }
            builder.append(getName(step).toUpperCase()); // case insensitive
            if (isIndexed(step)) {
                builder.append(CH_LB);
                builder.append(String.format("%03d", getIndex(step))); // pad 3 wide
                builder.append(CH_RB);
            }
        }
        return builder.toString();
    }

    @Override
    public void copy(int step, QualifiedPath replacement, int replacementStep) {
        super.copy(step, replacement, replacementStep);
        assert replacement instanceof PropertyPath;
        setXmlName(step, ((PropertyPath) replacement).getXmlName(replacementStep));
    }

    @Override
    protected void allocate(int length) {
        super.allocate(length);
        this.xmlNames = new String[length()];
    }
}
