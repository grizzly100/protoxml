package org.grizzlytech.protoxml.builder.readers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NVPStringReader extends AbstractNVPReader {

    private static final Logger LOG = LoggerFactory.getLogger(NVPStringReader.class);

    private List<String> mappings;

    public NVPStringReader() {
    }

    public List<String> getMappings() {
        return mappings;
    }

    public void setMappings(List<String> mappings) {
        this.mappings = mappings;
    }

    @Override
    public void readAll() {
        int lineNum = 0;

        for (String line : mappings) {
            lineNum++;
            handle(line, lineNum);
        }
    }
}
