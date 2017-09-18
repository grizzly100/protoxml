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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility classes for substituting parts of QualifiedPaths
 * <p>
 * This is needed as protoxml can type substitute Bean property classes, and hence path names have to be adjusted
 */
public class SubstitutionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(SubstitutionUtil.class);

    public static <P extends QualifiedPath> P apply(P path, Map<P, P> substitutions) {
        P target = path.cloneOf();

        for (P substitutionKey : orderedPathList(substitutions)) {
            if (target.startsWith(substitutionKey)) {
                P substitutionValue = substitutions.get(substitutionKey);
                target = target.replace(substitutionKey, substitutionValue);
            }
        }
        return target;
    }

    /**
     * If the map contains {a.b.c -> a.b.x} and {a.b.c.d.e -> a.b.c.d.z } then
     * the compression will replace the second mapping with {a.b.c.d.e -> a.b.x.d.z}
     *
     * @param source set of path to path substitution mappings
     * @return compressed set of mappings
     */
    public static <P extends QualifiedPath> Map<P, P> compress(Map<P, P> source) {
        Map<P, P> target = new HashMap<>(source.size());
        for (P newKey : orderedPathList(source)) {
            P newValue = source.get(newKey);
            for (P substitutionKey : orderedPathList(target)) {
                if (newKey.startsWith(substitutionKey)) {
                    P substitutionValue = target.get(substitutionKey);
                    newKey = newKey.replace(substitutionKey, substitutionValue);
                    newValue = newValue.replace(substitutionKey, substitutionValue);
                }
            }
            target.put(newKey, newValue);
        }
        return target;
    }

    public static <P extends QualifiedPath, T> List<P> orderedPathList(Map<P, T> source) {
        return source.keySet().stream().sorted().collect(Collectors.toList());
    }

    public static <P extends QualifiedPath, T> List<String> orderedPathValueList(Map<P, T> source) {
        return source.keySet().stream().sorted()
                .map(k -> Common.concatenate(k, "='", source.get(k), "'"))
                .collect(Collectors.toList());
    }
}
