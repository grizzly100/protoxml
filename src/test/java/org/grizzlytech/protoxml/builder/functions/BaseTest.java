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

package org.grizzlytech.protoxml.builder.functions;

import org.grizzlytech.protoxml.beans.Bean;
import org.grizzlytech.protoxml.beans.BeanImpl;
import org.grizzlytech.protoxml.beans.PropertyException;
import org.junit.Test;
import testdomain.example.NumberBean;

import static org.junit.Assert.*;

@SuppressWarnings("UnnecessaryBoxing")
public class BaseTest {
    @Test
    public void copy() throws PropertyException {
        Bean bean = new BeanImpl(new NumberBean());

        bean.setPathValue("myLong", new Long(100));

        Object result = Base.copy(bean, "myLong2", new String[]{"myLong"});

        assertEquals(new Long(100), result);
    }
}