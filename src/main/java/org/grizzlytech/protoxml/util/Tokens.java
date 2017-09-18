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

package org.grizzlytech.protoxml.util;


/**
 * Token constants
 */
public class Tokens {

    /**
     * For example, file:, jar:
     */
    public static final char URL_PROTOCOL_DELIMITER = ':';

    public static final String URL_PROTOCOL_FILE = "file";

    public static final String URL_PROTOCOL_JAR = "jar";

    /**
     * For example, org.grizzlytech.protoxml
     */
    public static final char PACKAGE_NAME_DELIMITER = '.';
    public static final String PACKAGE_NAME_DELIMITER_S = String.valueOf(PACKAGE_NAME_DELIMITER);

    /**
     * For example, org/sjf4j
     */
    public static final char JAR_ENTRY_DELIMITER = '/';

    /**
     * @link http://docs.oracle.com/javase/8/docs/api/java/net/JarURLConnection.html
     */
    public static final char JAR_PATH_SEPARATOR = '!';
    public static final String JAR_PATH_SEPARATOR_S = String.valueOf(JAR_PATH_SEPARATOR);

    public static final char RESOURCE_PATH_DELIMITER = '/';

    public static final String CLASS_FILE_SUFFIX = ".class";

    public static final char INNER_CLASS_DELIMITER = '$';

    public static final char CLASS_METHOD_DELIMITER = '#';
    public static final String CLASS_METHOD_DELIMITER_S = String.valueOf(CLASS_METHOD_DELIMITER);

    public static final String NEWLINE_S = System.lineSeparator();

    public static final char FILE_EXTENSION_DELIMITER = '.';
    public static final String FILE_EXTENSION_DELIMITER_S = String.valueOf(FILE_EXTENSION_DELIMITER);

}
