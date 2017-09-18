package org.grizzlytech.protoxml.beans;


import org.grizzlytech.protoxml.util.FatalException;
import org.grizzlytech.protoxml.xml.domainbuilder.employee.Employee;
import org.grizzlytech.protoxml.xml.domainbuilder.employee.Phone;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PropertyTest {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyTest.class);

    @Test
    public void metadataTest1() throws Exception {

        Method getter = Employee.class.getMethod("getSalary");
        Method setter = Employee.class.getMethod("setSalary", double.class);

        Property.Accessor[] accessorHolder = Property.createAccessorHolder();
        String propertyName = Property.extractPropertyName(getter, accessorHolder);

        Property prop = new Property(propertyName);
        prop.setGetter(getter);
        prop.setSetter(setter);
        prop.storeTypeInfo();

        assertEquals("salary", prop.getName());
        assertEquals(double.class, prop.getValueClass());

        Employee employee = new Employee();
        prop.setValue(employee, 1234.123);
        assertTrue(employee.getSalary() == 1234.123);
    }

    @Test
    public void metadataTest2() throws Exception {

        Method getter = Employee.class.getMethod("getPhones");
        // There is no setter for Lists (hence no setPhones)

        Property.Accessor[] accessorHolder = Property.createAccessorHolder();
        String propertyName = Property.extractPropertyName(getter, accessorHolder);

        Property prop = new Property(propertyName);
        prop.setGetter(getter);
        prop.storeTypeInfo();

        assertTrue(prop.getName().equals("phones"));
        assertTrue(prop.getValueClass().equals(List.class));
        assertTrue(prop.getValueParameterClass().equals(Phone.class));
        assertTrue(prop.isCollection());
    }

    @Test(expected = FatalException.class)
    public void metadataTest3() throws Exception {

        Method setter = this.getClass().getMethod("setComplex", Map.class);

        Property.Accessor[] accessorHolder = Property.createAccessorHolder();
        String propertyName = Property.extractPropertyName(setter, accessorHolder);

        Property prop = new Property(propertyName);
        prop.setSetter(setter);
        prop.storeTypeInfo();
    }

    public void setComplex(Map<String, Long> mapping) {
        LOG.info("Mapping entries:", mapping.size());
    }
}
