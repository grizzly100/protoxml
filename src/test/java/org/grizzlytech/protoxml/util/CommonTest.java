package org.grizzlytech.protoxml.util;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;

public class CommonTest {

    private static final Logger LOG = LoggerFactory.getLogger(CommonTest.class);

    @Test
    public void fatalAssertion() {
        boolean thrown = false;
        try {
            Common.fatalAssertion(false, LOG, "Error {} occurred", "1234");
        } catch (FatalException ex) {
            assertTrue(ex.getMessage().equals("Error [1234] occurred"));
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void fatalException() {
        boolean thrown = false;
        try {
            Common.fatalException(new NullPointerException("FAIL"), LOG, "Error {} occurred", "5678");
        } catch (FatalException ex) {
            assertTrue(ex.getMessage().equals("Error [5678] occurred"));
            thrown = true;
        }
        assertTrue(thrown);
    }
}
