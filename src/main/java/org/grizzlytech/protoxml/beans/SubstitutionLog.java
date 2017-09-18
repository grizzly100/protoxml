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

import org.grizzlytech.protoxml.util.QualifiedPath;
import org.grizzlytech.protoxml.util.SubstitutionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBElement;
import java.util.HashMap;
import java.util.Map;

/**
 * BeanImpl will use a SubstitutionLog of record property name (aka type/element) substitutions
 */
public class SubstitutionLog {

    private static final Logger LOG = LoggerFactory.getLogger(SubstitutionLog.class);

    private final Map<PropertyPath, PropertyPath> substitutionMap = new HashMap<>();

    private Map<PropertyPath, PropertyPath> compressedMap = new HashMap<>();

    public PropertyPath apply(PropertyPath path) {
        return SubstitutionUtil.apply(path, getCompressedMap());
    }

    public Map<PropertyPath, PropertyPath> getSubstitutionMap() {
        return this.substitutionMap;
    }

    public Map<PropertyPath, PropertyPath> getCompressedMap() {
        if (this.compressedMap == null) {
            this.compressedMap = SubstitutionUtil.compress(this.substitutionMap);
        }
        return compressedMap;
    }

    public void recordElementSubstitution(PropertyPath path, int step, Property prop, int index, JAXBElement element) {
        // Log the substitution against the next step in the path, which will be ".value" for JAXBElements
        PropertyPath key = path.subPath(step + 1);
        assert "value".equals(key.getLastName());
        this.substitutionMap.computeIfAbsent(key, k -> {
            // By definition this is an Element
            String substitutionName = element.getName().getLocalPart();
            return getSubstitutionPath(key, step, prop, index, substitutionName);
        });
    }

    public void recordTypeSubstitution(PropertyPath path, int step, Property prop, int index, Class xmlType) {
        PropertyPath key = new PropertyPath(path.getPath(step));
        this.substitutionMap.computeIfAbsent(key, k -> {
            // The property field may be annotated with XmlElements, otherwise SimpleName is used
            String substitutionName = prop.getXmlName(xmlType);
            return getSubstitutionPath(key, step, prop, index, substitutionName);
        });
    }

    // Construct the PropertyPath that the key path should map to
    PropertyPath getSubstitutionPath(PropertyPath key, int step, Property prop, int index, String SubstitutionName) {
        int stubStep = step - 1;
        String stubPath = (stubStep >= 0) ? key.getPath(stubStep) + PropertyPath.PATH_DELIMITER_S : "";

        PropertyPath substitutionPath = new PropertyPath(stubPath + SubstitutionName);
        substitutionPath.setIndex(substitutionPath.lastStep(),
                getSubstitutionIndex(key, step, index, substitutionPath));
        LOG.debug("Substitution: path [{}] substituted with [{}]", key, substitutionPath);
        this.compressedMap = null; // invalidate cache
        return substitutionPath;
    }

    // There are cases where substitutionIndex != index in situations where items in an element list can be
    // substituted using a Substitution Group - e.g., "list of A" is replaced by "B, C, D, B, C..."
    int getSubstitutionIndex(PropertyPath path, int step, int index, PropertyPath substitutionPath) {
        // If the property is not indexed, then the substitutionPath should not be indexed
        if (index == QualifiedPath.NO_INDEX) return QualifiedPath.NO_INDEX;

        int substitutionIndex = 0; // assume this is the first index for the substitutionPath

        PropertyPath keyPath = path.cloneOf(); // make a copy as we need to modify the index
        for (int keyIndex = 0; keyIndex < index; keyIndex++) {
            // Determine what the previous index was mapped to
            keyPath.setIndex(step, keyIndex);
            PropertyPath previousSubstitutionPath = this.substitutionMap.get(keyPath);
            // The substitutionIndex for the substitutionPath must be +1 more than the previous value
            if (substitutionPath.getLastName().equalsIgnoreCase(previousSubstitutionPath.getLastName())) {
                substitutionIndex = previousSubstitutionPath.getLastIndex() + 1;
            }
        }
        if (substitutionIndex != index) { // edge case has been encountered
            LOG.debug("FYI: substitutionIndex({}) != index({}) when {} -> {}[{}]", substitutionIndex, index,
                    path, substitutionPath, substitutionIndex);
        }
        return substitutionIndex;
    }
}
