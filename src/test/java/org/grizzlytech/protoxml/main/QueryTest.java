package org.grizzlytech.protoxml.main;


import org.grizzlytech.protoxml.util.AssertUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;

public class QueryTest {

    private static final Logger LOG = LoggerFactory.getLogger(QueryTest.class);

    @Test
    public void main() {
        StringWriter writer = new StringWriter();
        Query.printPropertyTree(new String[]{"testdomain.employee.Employee"}, writer);
        String result = writer.toString();

        AssertUtil.assertContains(LOG, result, new String[]
                {"#salary=double", "#address.city=String"});
    }
}
