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


import org.junit.Test;
import testdomain.employee.Address;
import testdomain.employee.Employee;

import javax.xml.bind.JAXBElement;

import static org.junit.Assert.assertTrue;

@SuppressWarnings("ALL")
public class ObjectFactoryHelperTest {
    @Test
    public void createElement() {
        Address address = XMLObjectFactory.getInstance().createObject(Address.class);
        assertTrue(address != null);
    }

    @Test
    public void createJAXBElement() {
        Employee employee = XMLObjectFactory.getInstance().createObject(Employee.class);
        assertTrue(employee != null);

        JAXBElement<Employee> jaxbEmployee = XMLObjectFactory.getInstance()
                .createObjectW(JAXBElement.class, employee);
        assertTrue(employee != null);
    }

}
