//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.05.25 at 11:58:46 PM BST 
//


package testdomain.company;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import testdomain.employee.Employee;
import testdomain.zoo.Zoo;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the testdomain.company package. 
 * <p>An AbstractObjectFactory allows you to programatically
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Product_QNAME = new QName("http://www.grizzlytech.org/testdomain/company", "product");
    private final static QName _Employee_QNAME = new QName("http://www.grizzlytech.org/testdomain/employee", "employee");
    private final static QName _Company_QNAME = new QName("http://www.grizzlytech.org/testdomain/company", "company");
    private final static QName _Zoo_QNAME = new QName("http://www.grizzlytech.org/testdomain/zoo", "zoo");

    /**
     * Create a new AbstractObjectFactory that can be used to create new instances of schema derived classes for package: testdomain.company
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Product }
     * 
     */
    public Product createProduct() {
        return new Product();
    }

    /**
     * Create an instance of {@link Company }
     * 
     */
    public Company createCompany() {
        return new Company();
    }

    /**
     * Create an instance of {@link Dog }
     * 
     */
    public Dog createDog() {
        return new Dog();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Product }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.grizzlytech.org/testdomain/company", name = "product")
    public JAXBElement<Product> createProduct(Product value) {
        return new JAXBElement<Product>(_Product_QNAME, Product.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Employee }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.grizzlytech.org/testdomain/employee", name = "employee")
    public JAXBElement<Employee> createEmployee(Employee value) {
        return new JAXBElement<Employee>(_Employee_QNAME, Employee.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Company }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.grizzlytech.org/testdomain/company", name = "company")
    public JAXBElement<Company> createCompany(Company value) {
        return new JAXBElement<Company>(_Company_QNAME, Company.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Zoo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.grizzlytech.org/testdomain/zoo", name = "zoo")
    public JAXBElement<Zoo> createZoo(Zoo value) {
        return new JAXBElement<Zoo>(_Zoo_QNAME, Zoo.class, null, value);
    }

}
