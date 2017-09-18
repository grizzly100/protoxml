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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class QualifiedPath implements Comparable<QualifiedPath>, Cloneable {

    final public static int NO_INDEX = -1;
    final public static Character CH_LB = '[';
    final public static Character CH_RB = ']';
    private static final Logger LOG = LoggerFactory.getLogger(QualifiedPath.class);

    /**
     * the qualified path as a string
     */
    protected String path = null;

    /**
     * a value computed on the path which is used for comparison, equality and hashing
     */
    protected String comparableValue = null;

    /**
     * the component names of the path
     */
    protected String[] names;

    /**
     * the indices of the names
     */
    protected int[] indices;

    protected QualifiedPath() {
    }

    /**
     * Create a path based on the given string, which is assumed to be delimited
     * with the pathDelimiter()
     *
     * @param path path to parse
     */
    public QualifiedPath(String path) {
        setPath(path);
    }

    /**
     * Create a path based on the given string, and provide a regex for splitting.
     * This is useful when building one QualifiedPath subclass from another
     *
     * @param path       path to parse
     * @param splitRegex regular expression to used for splitting the steps
     */
    public QualifiedPath(String path, String splitRegex) {
        setPath(path, splitRegex);
    }

    /**
     * Copy constructor
     * @param source QualifiedPath to copy
     */
    public <P extends QualifiedPath> QualifiedPath(P source) {
        allocate(source.length());
        for (int step=0; step<= lastStep(); step++)
        {
            copy(step, source, step);
        }
    }

    /**
     * Join multiple paths
     *
     * @param start starting path
     * @param paths subsequent paths
     * @return path of the same type as path
     */
    public static <P extends QualifiedPath> P join(QualifiedPath start, QualifiedPath... paths) {
        P result;
        int totalSteps = start.length();
        for (QualifiedPath next : paths) {
            assert start.getClass().equals(next.getClass()); // homogeneous joining
            totalSteps += next.length();
        }

        result = start.newInstance(totalSteps);
        int step = 0;
        for (QualifiedPath next : paths) {
            for (int i = 0; i <= next.lastStep(); i++) {
                result.copy(step++, next, i);
            }
        }
        return result;
    }

    @Override
    public Object clone() {
        QualifiedPath clone = null;
        try {
            clone = (QualifiedPath) super.clone();
            // Deep copy
            clone.allocate(length());
            for (int step = 0; step <= lastStep(); step++) {
                clone.copy(step, this, step);
            }
        } catch (CloneNotSupportedException ex) {
            Common.fatalException(ex, LOG, "Cannot clone");
        }
        return clone;
    }

    public <P extends QualifiedPath> P cloneOf() {
        return (P) clone();
    }

    /**
     * the total number of steps
     */
    final public int length() {
        return names.length;
    }

    /**
     * steps start at 0, hence lastStep = length -1
     *
     * @return the last step
     */
    final public int lastStep() {
        return length() - 1;
    }

    /**
     * Construct the string representation of the path. Lazy evaluation (on demand)
     *
     * @return string representation
     */
    final public String getPath() {
        if (this.path == null) {
            this.path = getPath(lastStep());
        }
        return this.path;
    }

    final public void setPath(String value) {
        parsePath(value);
        onChange();
    }

    final public void setPath(String value, String splitRegex) {
        parsePath(value, splitRegex);
        onChange();
    }

    final public String getPath(int endStep) {
        assert endStep <= lastStep();
        return getPath(0, endStep);
    }

    final public String getPath(int beginStep, int endStep) {
        assert endStep <= lastStep();
        assert beginStep >= 0 && beginStep <= endStep;

        StringBuilder builder = new StringBuilder();
        for (int step = beginStep; step <= endStep; step++) {
            if (step > 0) {
                builder.append(pathDelimiter());
            }
            builder.append(getName(step));
            if (isIndexed(step)) {
                builder.append(CH_LB);
                builder.append(getIndex(step));
                builder.append(CH_RB);
            }
        }
        return builder.toString();
    }

    final public String getLastName() {
        return this.names[lastStep()];
    }

    final public String getName(int step) {
        return this.names[step];
    }

    @SuppressWarnings("UnusedReturnValue")
    final public <P extends QualifiedPath> P setName(int step, String value) {
        this.names[step] = value;
        onChange();
        return (P) this;
    }

    final public boolean isIndexed(int step) {
        return this.indices[step] != NO_INDEX;
    }

    final public int getLastIndex() {
        return this.indices[lastStep()];
    }

    final public int getIndex(int step) {
        return this.indices[step];
    }

    final public <P extends QualifiedPath> P setIndex(int step, int value) {
        this.indices[step] = value;
        onChange();
        return (P) this;
    }

    final protected void onChange() {
        this.path = null;
        this.comparableValue = null;
    }

    final public String getComparableValue() {
        if (this.comparableValue == null) {
            this.comparableValue = computeComparableValue();
        }
        return this.comparableValue;
    }

    @Override
    final public int hashCode() {
        //LOG.info("Getting hash for {}->{}->{}", this, getComparableValue(), getComparableValue().hashCode());
        return getComparableValue().hashCode();
    }

    @Override
    final public boolean equals(Object obj) {
        //LOG.info("Comparing {} and {}", this, obj);
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        QualifiedPath other = (QualifiedPath) obj;
        return Common.safeEquals(this.getComparableValue(), other.getComparableValue());
    }

    public boolean startsWith(QualifiedPath other) {
        return this.getComparableValue().startsWith(other.getComparableValue());
    }

    public <P extends QualifiedPath> P subPath(int endStep) {
        return subPath(0, endStep);
    }

    /**
     * Create a sub path from the begin to end step inclusive
     *
     * @param beginStep 0..stepCount
     * @param endStep   0..stepCount
     * @return the steps inclusive of begin and end
     */
    public <P extends QualifiedPath> P subPath(int beginStep, int endStep) {
        assert beginStep >= 0 && beginStep <= lastStep();
        assert endStep >= beginStep && endStep <= lastStep();
        int resultLength = endStep - beginStep + 1;
        P result = newInstance(resultLength);
        for (int step = 0, i = beginStep; i <= endStep; i++, step++) {
            result.copy(step, this, i);
        }
        return result;
    }

    /**
     * Replace the contents of a path with a replacement path
     *
     * @param target      subpath to be replaced
     * @param replacement subpath to be used as the replacement
     * @return new path with the replacement made
     */
    public <P extends QualifiedPath> P replace(P target, P replacement) {
        //LOG.info("Replace: this={}, replace={} with={}", this, target, replacement);
        int leftIndex = getComparableValue().indexOf(target.getComparableValue());
        if (leftIndex == -1) {
            return (P) this; // no replacement
        }

        int rightIndex = leftIndex + target.getComparableValue().length();
        int endIndex = getComparableValue().length();

        int prefixSteps = (leftIndex == 0) ? 0 :
                Common.charCount(getComparableValue().substring(0, leftIndex - 1), pathDelimiter());
        int suffixSteps = (rightIndex == endIndex) ? 0 :
                Common.charCount(getComparableValue().substring(rightIndex, endIndex), pathDelimiter());
        int resultLength = prefixSteps + replacement.length() + suffixSteps;

        P result = newInstance(resultLength);

        int step = 0;
        for (int i = 0; i < prefixSteps; i++) {
            result.copy(step++, this, i);
        }
        for (int i = 0; i <= replacement.lastStep(); i++) {
            result.copy(step++, replacement, i);
        }
        for (int i = lastStep() - suffixSteps + 1; i <= lastStep(); i++) {
            result.copy(step++, this, i);
        }
        //LOG.info("result {}", result);
        return result;
    }

    /**
     * Override this method to enhance the copying (e.g., if the subclass has additional fields)
     *
     * @param step            step to copy
     * @param replacement     path containing the replacement values
     * @param replacementStep step to reference from the replacement path
     */
    public void copy(int step, QualifiedPath replacement, int replacementStep) {
        assert step >= 0 && step <= lastStep();
        assert replacementStep >= 0 && replacementStep <= replacement.lastStep();
        setName(step, replacement.getName(replacementStep));
        setIndex(step, replacement.getIndex(replacementStep));
    }

    /**
     * Create a new instance of the same QualifiedPath class
     *
     * @param length length to allocate
     * @return the new instance, allocated to the required length
     */
    protected <P extends QualifiedPath> P newInstance(int length) {
        P result = null;
        try {
            result = (P) getClass().newInstance();
            result.allocate(length);
        } catch (InstantiationException | IllegalAccessException ex) {
            Common.fatalException(ex, LOG, "Unable to create [{}]", getClass().getCanonicalName());
        }
        return result;
    }

    /**
     * override this method to change the delimiter
     */
    public char pathDelimiter() {
        return '.';
    }

    protected String splitRegex() {
        return "\\.";
    }

    /**
     * Override this method if the subclass has additional fields to allocate
     *
     * @param length length of the path
     */
    protected void allocate(int length) {
        assert length > 0;
        this.names = new String[length];
        this.indices = new int[length];
    }

    protected void parsePath(String path) {
        parsePath(path, splitRegex());
    }

    protected void parsePath(String path, String splitRegex) {
        String[] components = path.split(splitRegex);

        // Create the arrays
        int length = components.length;
        allocate(length);

        // Populate the path and check for indices
        for (int step = 0; step < length; step++) {
            String segment = components[step];
            int LB = segment.indexOf(CH_LB);
            int RB = segment.indexOf(CH_RB);
            if (LB >= 0 && RB > LB) {
                this.names[step] = segment.substring(0, LB);
                String index = segment.substring(LB + 1, RB);
                try {
                    this.indices[step] = Integer.parseInt(index);
                } catch (NumberFormatException nfe) {
                    LOG.error("Could not parse \"{}\" within \"{}\" into an integer", index, segment);
                    throw nfe;
                }
            } else {
                this.names[step] = segment;
                this.indices[step] = NO_INDEX;
            }
        }
    }

    /**
     * Override this method to enhance comparison - e.g., adding case sensitivity
     */
    protected String computeComparableValue() {
        return getPath();
    }

    public String toString() {
        return getPath();
    }

    @Override
    public int compareTo(QualifiedPath o) {
        return getComparableValue().compareTo(o.getComparableValue());
    }
}
