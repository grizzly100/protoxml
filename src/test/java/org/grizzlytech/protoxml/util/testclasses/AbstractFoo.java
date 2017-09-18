package org.grizzlytech.protoxml.util.testclasses;


/**
 * This class is used as part of the test for ClassUtil
 */
public abstract class AbstractFoo implements Foo {
    @Override
    public String getMessage() {
        return "Hello, World";
    }
}
