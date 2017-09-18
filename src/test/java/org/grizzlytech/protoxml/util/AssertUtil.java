package org.grizzlytech.protoxml.util;


import org.slf4j.Logger;

import static org.junit.Assert.assertTrue;

public class AssertUtil {
    /**
     * Assert that a contain contains all of the contents asserted
     */
    public static void assertContains(Logger log, String container, String[] contents) {
        String content = null;
        try {
            for (String item : contents) {
                content = item;
                assertTrue(container.contains(content));
            }
        } catch (AssertionError ex) {
            log.error("assertContains: container {} does not contain {}", container, content);
            throw ex;
        }
    }

    /**
     * Remove all whitespace from a string - can be useful prior to using assertContains
     */
    public static String removeWhitespace(String s) {
        return s.replaceAll("\\s", "");
    }
}
