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

import org.junit.Test;

import static org.junit.Assert.*;

public class QualifiedPathTest {
    @Test
    public void simple() {
        String t = "apple.banana[2].pear[3].strawberry";
        QualifiedPath p = new QualifiedPath(t);
        assertEquals(4, p.length());
        assertEquals(t, p.getPath());
        assertTrue(p.isIndexed(1));
        assertEquals(2, p.getIndex(1));
    }

    @Test
    public void getPath() {
        String t = "apple.banana[2].pear[3].strawberry";
        QualifiedPath p = new QualifiedPath(t);
        p.setName(1, "melon");
        p.setIndex(2,5);
        String s = "apple.melon[2].pear[5].strawberry";
        assertEquals(s, p.getPath());
    }

}