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
import org.grizzlytech.protoxml.beans.PropertyException;
import org.grizzlytech.protoxml.beans.PropertyPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base functions used by the Dates and Maths etc. functions
 */
public class Base {

    private static final Logger LOG = LoggerFactory.getLogger(Base.class);

    /**
     * Resolve an argument into an absolute property path
     *
     * @param path     path which is the context of the function call
     * @param argument an argument containing a relative or absolute path
     * @return an absolute path
     */
    public static String resolvePath(String path, String argument) {
        return (argument.startsWith(PropertyPath.PATH_DELIMITER_S)) ?
                (new PropertyPath(path)).resolve(argument).getPath() : argument;
    }

    /**
     * Return the value of the first argument (interpreted as a path)
     *
     * @param bean bean being constructed
     * @param path path the request is for
     * @param args only the first argument is used
     * @return the value of the first argument
     * @throws PropertyException on error
     */
    public static Object copy(Bean bean, String path, String[] args)
            throws PropertyException {
        assert args != null && args.length >= 1;
        String resolved = resolvePath(path, args[0]);
        return bean.getPathValue(resolved).getValue();
    }
}
