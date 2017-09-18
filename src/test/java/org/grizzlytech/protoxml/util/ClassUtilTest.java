package org.grizzlytech.protoxml.util;


import org.grizzlytech.protoxml.util.testclasses.AbstractFoo;
import org.grizzlytech.protoxml.util.testclasses.Bar;
import org.grizzlytech.protoxml.util.testclasses.ConcreteFoo;
import org.grizzlytech.protoxml.util.testclasses.Foo;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class ClassUtilTest {

    private static Logger LOG = LoggerFactory.getLogger(ClassUtilTest.class);

    @Test
    public void getAllInterfaces() {
        List<Class> interfaces = ClassUtil.getAllInterfaces(ConcreteFoo.class);
        assertTrue(interfaces.contains(Foo.class));
        assertTrue(interfaces.contains(Bar.class));
    }

    @Test
    public void getClasses() {
        List<Class> classes = ClassUtil.getClasses("org.grizzlytech.protoxml", x -> x.contains("Foo"));
        assertTrue(classes.contains(Foo.class));
        assertTrue(classes.contains(AbstractFoo.class));
        assertTrue(classes.contains(ConcreteFoo.class));
    }

    @Test
    public void getClassesInJars() {
        List<Class> classes = ClassUtil.getClasses("org.slf4j", x -> x.contains("Logger"));
        assertTrue(classes.contains(Logger.class));
        assertTrue(classes.contains(LoggerFactory.class));
    }

    @Test
    public void getImplementations() {
        List<Class> classes = ClassUtil.getImplementations("org.grizzlytech.protoxml", x -> x.contains("Foo"), Foo.class);
        assertTrue(!classes.contains(Foo.class));
        assertTrue(!classes.contains(AbstractFoo.class));
        assertTrue(classes.contains(ConcreteFoo.class));
    }

}
