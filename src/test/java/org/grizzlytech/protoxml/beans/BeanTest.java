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


import org.grizzlytech.protoxml.util.AssertUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testdomain.company.Company;
import testdomain.employee.Employee;
import testdomain.zoo.Zoo;

import static org.junit.Assert.assertEquals;

public class BeanTest {
    private static final Logger LOG = LoggerFactory.getLogger(BeanTest.class);

    public Bean createEmployeeBean() throws Exception {
        Employee employee = new Employee();
        Bean bean = new BeanImpl(employee);

        bean.setPathValue("name", "bob");
        bean.setPathValue("salary", 1234.30);
        bean.setPathValue("address.city", "new york");
        bean.setPathValue("salary", "5000.99");
        bean.setPathValue("phones[0].localNumber", "999");

        return bean;
    }

    public Bean createZooBean() throws Exception {
        Zoo zoo = new Zoo();
        Bean bean = new BeanImpl(zoo);

        bean.setPathValue("name", "London Zoo");
        bean.setPathValueClassName("mascot$", "testdomain.zoo.Penguin");
        bean.setPathValue("mascot.id", "1");
        bean.setPathValue("mascot.nickname", "Charles");
        bean.setPathValue("mascot.fishPerDay", "5");
        bean.setPathValueClassName("animals[0]$", "testdomain.zoo.Dolphin");
        bean.setPathValue("animals[0].id", "2");
        bean.setPathValue("animals[0].nickname", "Claire");
        bean.setPathValue("animals[0].length", "20");

        return bean;
    }

    public Bean createCompanyBean() throws Exception {
        Company company = new Company();
        Bean bean = new BeanImpl(company);

        bean.setPathValue("name", "Fargo Inc");
        bean.setPathValue("manager.name", "Fred");
        bean.setPathValue("manager.salary", 999.99);
        bean.setPathValue("manager.address.city", "London");
        bean.setPathValue("manager.phones[0].localNumber", "123456");
        bean.setPathValue("pet.id", "1");
        bean.setPathValue("pet.nickname", "Charles");
        bean.setPathValue("pet.colour", "black");

        return bean;
    }

    @Test
    public void testEmployeeBean() throws Exception {

        Bean bean = createEmployeeBean();

        bean.setPathValue("salary", "5000.99");
        bean.setPathValue("salary", 4242.99);
    }

    @Test
    public void testZooBean() throws Exception {
        Bean bean = createZooBean();

        // Nickname found in superclass (Animal)
        bean.setPathValue("mascot.nickname", "Works");
        assertEquals(bean.getPathValue("mascot.nickname").getValue(), "Works");

        // Nickname2 not found
        try {
            bean.setPathValue("mascot.nickname2", "Fails");
        } catch (PropertyException ex) {
            // getProperty will have checked with the base Animal class as well
            AssertUtil.assertContains(LOG, ex.getMessage(), new String[]{
                    "[nickname2]", "[testdomain.zoo.Penguin]"});
        }
    }
}
