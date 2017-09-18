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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SubstitutionLogTest {

    private static final Logger LOG = LoggerFactory.getLogger(SubstitutionLogTest.class);

    @Test
    public void substitute()  {

        Map<PropertyPath,PropertyPath> substitutions = new HashMap<>();

        addSub(substitutions, "a.b.c.d", "a.b.x");
        addSub(substitutions, "a.b.c.d.e.f", "a.b.c.d.e.y");

        Map<PropertyPath,PropertyPath> compressed = SubstitutionUtil.compress(substitutions);

        QualifiedPath result1 = SubstitutionUtil.apply(toPath("a.b.c.d.m.n"),compressed);
        QualifiedPath result2 = SubstitutionUtil.apply(toPath("a.b.c.d.e.f.g.h"),compressed);

        assertEquals(toPath("A.B.X.M.N"), result1);
        assertEquals(toPath("a.b.x.e.y.g.h"), result2);
    }

    protected void addSub(Map<PropertyPath,PropertyPath> target, String a, String b)
    {
        target.put(new PropertyPath(a), new PropertyPath(b));
    }

    protected PropertyPath toPath(String p)
    {
        return new PropertyPath(p);
    }
}