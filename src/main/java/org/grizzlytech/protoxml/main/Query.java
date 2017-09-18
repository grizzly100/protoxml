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

package org.grizzlytech.protoxml.main;


import org.grizzlytech.protoxml.beans.PropertyDebugger;
import org.grizzlytech.protoxml.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Tool to print the property dictionary for a given class
 */
public class Query {

    private static final Logger LOG = LoggerFactory.getLogger(Query.class);

    public static void main(String args[]) {
        printPropertyTree(args, new PrintWriter(System.out));
    }

    public static void printPropertyTree(String args[], Writer writer) {
        for (String className : args) {
            Class clazz = ClassUtil.getClassElseNull(className);
            if (clazz != null) {
                try {
                    writer.write(PropertyDebugger.propertyTreeToString(clazz));
                } catch (IOException ex) {
                    LOG.error("Failed to write", ex);
                }
            }
        }
    }
}
