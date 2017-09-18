package org.grizzlytech.protoxml.beans;


import org.grizzlytech.protoxml.util.Common;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class PropertyPathTest {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyPathTest.class);

    @Test
    public void pathCompare()
    {
        PropertyPath p1 = new PropertyPath("apple.banana[2].pear[3].strawberry");
        PropertyPath p2 = new PropertyPath("APPLE.BANANA[2].PEAR[3].STRAWBERRY");
        //LOG.info("{} = {}", p1.getComparableValue(), p2.getComparableValue());
        assertTrue(p1.equals(p2));
    }

    @Test
    public void pathTest() {
        String t = "abc.def.ghi[1].jk[2].z";
        PropertyPath path = new PropertyPath(t);

        String walk = "";
        for (int step = 0; step <= path.lastStep(); step++) {
            String delim = (step > 0) ? "." : "";
            String propertyName = path.getName(step);
            int propertyIndex = path.getIndex(step);
            //noinspection StringConcatenationInLoop
            walk += delim + propertyName + ((propertyIndex >= 0) ? "[" + propertyIndex + "]" : "");
        }
        assertEquals(path.getPath(), walk);
    }

    @Test
    public void resolve() {
        PropertyPath path = new PropertyPath("abc.def.ghi[1].jk[2].z");
        PropertyPath resolvedPath = path.resolve(".s.t.u");
        assertEquals("abc.def.ghi[1].jk[2].s.t.u", resolvedPath.getPath());
        PropertyPath resolvedPath2 = path.resolve("...s.t.u");
        assertEquals("abc.def.s.t.u", resolvedPath2.getPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveFail() {
        PropertyPath path = new PropertyPath("abc.def.ghi[1].jk[2].z");
        PropertyPath resolvedPathFail = path.resolve("......s.t.u");
    }

    @Test
    public void ordering()
    {
        PropertyPath p1 = new PropertyPath("Apple.Banana[2].Pear[3].Strawberry");
        PropertyPath p2 = new PropertyPath("apple.banana[12].pear[2].strawberry");
        PropertyPath p3 = new PropertyPath("APPLE.banana[1].pear[01].strawberry");
        PropertyPath p4 = new PropertyPath("Aardvark.Banana.Pear[3].Strawberry");

        List<PropertyPath> original = Common.addToList(new ArrayList<>(), p1, p2, p3, p4);
        List<PropertyPath> sorted = original.stream().sorted().collect(Collectors.toList());
        List<PropertyPath> manual = Common.addToList(new ArrayList<>(), p4, p3, p1, p2);
        assertEquals(manual, sorted);
    }
}
