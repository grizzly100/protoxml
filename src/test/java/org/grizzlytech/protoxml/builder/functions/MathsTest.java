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
import org.grizzlytech.protoxml.util.Common;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testdomain.example.NumberBean;

import java.util.List;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("UnnecessaryBoxing")
public class MathsTest {

    private static final Logger LOG = LoggerFactory.getLogger(MathsTest.class);

    @Test
    public void parseNumbers() throws PropertyException {
        Bean bean = new BeanImpl(new NumberBean());

        bean.setPathValue("myLong", new Long(100));

        bean.setPathValue("myDouble", new Double(99.9));

        List<Number> numbers = Maths.parseNumbers(bean, "myBigDecimal",
                new String[]{"myLong", "34", "6.07", "myDouble"});

        String contents = Common.listToString(numbers, ", ");
        assertEquals("100, 34, 6.07, 99.9", contents);
    }

    @Test
    public void add() throws PropertyException {
        Bean bean = new BeanImpl(new NumberBean());

        bean.setPathValue("myLong", new Long(100));

        bean.setPathValue("myDouble", new Double(99.9));

        String sum = Maths.add(bean, "myBigDecimal",
                new String[]{"myLong", "34", "6.07", "myDouble"});
        assertEquals("239.97", sum);
    }

    @Test
    public void multiply() throws PropertyException {
        Bean bean = new BeanImpl(new NumberBean());

        bean.setPathValue("myLong", new Long(8));

        bean.setPathValue("myDouble", new Double(12.5));

        String product = Maths.multiply(bean, "myBigDecimal",
                new String[]{"myLong", "0.5", "-4", "myDouble"});
        assertEquals("-200", product);
    }

}