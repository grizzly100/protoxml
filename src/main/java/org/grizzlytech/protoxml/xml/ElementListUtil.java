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


import org.grizzlytech.protoxml.util.ClassUtil;
import org.grizzlytech.protoxml.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collector;

/**
 * Helper class to create ElementLists and find element classes within those lists
 */
final public class ElementListUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ElementListUtil.class);

    /**
     * Create a named ElementList and initialise it
     *
     * @param className name of class that implements ElementList interface
     * @return an initialised ElementList
     */
    public static ElementList createElementList(String className) {
        ElementList elementList = null;

        try {
            elementList = (ElementList) (Class.forName(className).newInstance());
            elementList.init();
            LOG.info("Created and initialized: {}", elementList);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException ex) {
            Common.fatalException(ex, LOG, "Cannot find or instantiate ElementList implementation {}", className);
        }
        return elementList;
    }

    /**
     * Create all ElementList classes found in the classpath
     *
     * @param packageRoot package to start searching from
     * @return list of matching ElementList implementations
     */
    public static List<ElementList> createAllElementLists(String packageRoot) {
        final String ASSUMED_CLASS_SUFFIX = "ElementList";
        final Predicate<String> CLASSNAME_FILTER = (x) -> x.contains(ASSUMED_CLASS_SUFFIX);

        List<ElementList> list = new ArrayList<>();

        ClassUtil.getImplementations(packageRoot, CLASSNAME_FILTER, ElementList.class)
                .forEach(c -> list.add(createElementList(c.getCanonicalName())));

        return list;
    }

    public static ElementMetadata findByElementClass(ElementList elementList, Class elementClass) {
        return elementList.getElements().stream()
                .filter(e -> e.getElementClass().equals(elementClass))
                .findFirst().orElse(null);
    }

    public static ElementMetadata findByElementClassName(ElementList elementList, String elementClassName) {
        assert (elementList != null);
        return elementList.getElements().stream()
                .filter(e -> e.getElementClass().getName().equalsIgnoreCase(elementClassName))
                .findFirst().orElse(null);
    }

    public static String toString(ElementList elementList) {
        final String DELIMITER = ", ";
        return elementList.getElements().stream()
                .collect(Collector.of(StringBuilder::new,
                        (b, e) -> b.append(e.toString()).append(DELIMITER),
                        StringBuilder::append,
                        StringBuilder::toString));
    }
}
