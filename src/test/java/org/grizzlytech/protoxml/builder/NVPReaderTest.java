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

package org.grizzlytech.protoxml.builder;

import org.grizzlytech.protoxml.builder.readers.NVPFileReader;
import org.grizzlytech.protoxml.util.AssertUtil;
import org.grizzlytech.protoxml.util.NVP;
import org.grizzlytech.protoxml.util.TestPaths;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;

public class NVPReaderTest {

    private static final Logger LOG = LoggerFactory.getLogger(NVPReaderTest.class);

    @Test
    public void read() {

        NVPFileReader reader = new NVPFileReader();

        File inputFile = Paths.get(TestPaths.getTestResourcesDir().getAbsolutePath(), "examples",
                "filereadertest-01.txt").toFile();

        reader.setSourceFile(inputFile);

        CallbackCollector collector = new CallbackCollector();
        reader.read(collector);

        String trace = collector.toString();

        AssertUtil.assertContains(LOG, trace, new String[]{
                "name=Fargo Inc", "manager.phones[0].localNumber=123456",
                "@with=manager", "@include=includes\\filereadertest-02.txt",
                "manager.phones[1].localNumber=9172756975", "manager.salary=1024.99"
        });
    }

    public static class CallbackCollector implements NVPReader.Callback {
        final StringBuilder builder = new StringBuilder();
        int lineNum = 0;

        @Override
        public void onNVP(NVP<String> pair, int line) {
            if (lineNum++ > 0) {
                builder.append("\n");
            }
            builder.append(pair.toString());
        }

        @Override
        public void onComment(String comment, int line) {
            if (lineNum++ > 0) {
                builder.append("\n");
            }
            builder.append(comment);
        }

        @Override
        public String toString() {
            return builder.toString();
        }
    }


}