package org.grizzlytech.protoxml.beans;

import org.grizzlytech.protoxml.util.AssertUtil;
import org.grizzlytech.protoxml.util.NVP;
import org.grizzlytech.protoxml.xml.domainbuilder.employee.Address;
import org.grizzlytech.protoxml.xml.domainbuilder.employee.Employee;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testdomain.example.ExampleBean;
import testdomain.music.Album;
import testdomain.zoo.Animal;

import javax.xml.bind.JAXBElement;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PropertyDictionaryTest {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyDictionaryTest.class);

    @Test
    public void dictionaryTest() {
        PropertyDictionary dict = PropertyDictionary.getInstance();

        // Map and extract a the city property from the Address class
        Map<String, Property> mapAddress = dict.getPropertyMap(Address.class);
        Property propCity = mapAddress.get(PropertyDictionary.toKey("city"));
        assertNotNull(propCity);
        Assert.assertEquals(String.class, propCity.getValueClass());

        // Walk the child classes of Employee
        String classes = PropertyDebugger.classToString(Employee.class);
        AssertUtil.assertContains(LOG, classes, new String[]
                {"+ salary[double]", "+ address[Address]", "+ phones[List<Phone>]"});

        // Walk the child properties of Employee
        String properties = PropertyDebugger.propertyTreeToString(Employee.class);
        AssertUtil.assertContains(LOG, properties, new String[]
                {"#salary=double", "#address.line1=String", "#phones[0].countryCode=String"});
    }

    @Test
    public void genericCases() {
        PropertyDictionary dict = PropertyDictionary.getInstance();

        Property propClazz = dict.getProperty(ExampleBean.class, "clazz");
        Assert.assertEquals(Class.class, propClazz.getValueClass());
        Assert.assertEquals(Object.class, propClazz.getValueParameterClass());

        Property propThing = dict.getProperty(ExampleBean.class, "thing");
        Assert.assertEquals(JAXBElement.class, propThing.getValueClass());
        Assert.assertEquals(Object.class, propThing.getValueParameterClass());

        Property propThingList = dict.getProperty(ExampleBean.class, "thingList");
        Assert.assertEquals(List.class, propThingList.getValueClass());
        Assert.assertEquals(JAXBElement.class, propThingList.getValueParameterClass());

        Property propPetList = dict.getProperty(ExampleBean.class, "petList");
        Assert.assertEquals(List.class, propPetList.getValueClass());
        Assert.assertEquals(Animal.class, propPetList.getValueParameterClass());

        Property propBeanHolder = dict.getProperty(ExampleBean.class, "beanHolder");
        Assert.assertEquals(NVP.class, propBeanHolder.getValueClass());
        Assert.assertEquals(Bean.class, propBeanHolder.getValueParameterClass());
    }

    @Test
    public void complexCases() {
        PropertyDictionary dict = PropertyDictionary.getInstance();

        Map<String, Property> albumDict = dict.getPropertyMap(Album.class);

        // Check xmlName
        assertEquals("title", albumDict.get("ALBUMTITLE").getXmlName());
        assertEquals(true, albumDict.get("ALBUMTITLE").isRequired());
    }
}