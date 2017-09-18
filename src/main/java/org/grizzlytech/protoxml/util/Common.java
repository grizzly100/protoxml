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


import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility methods used across the code base.
 */
public class Common {

    // FATAL ASSERTION AND ERROR HANDLING

    /**
     * Call when making an assertion that cannot be recovered from
     *
     * @param assertion      predicate to validate (test as being true)
     * @param log            log to write to, should the assertion prove false
     * @param messageOnFalse message to log
     * @param args           arguments referenced in the log message
     */
    public static void fatalAssertion(boolean assertion, Logger log, String messageOnFalse, Object... args) {
        if (!assertion) {
            log.error(messageOnFalse, args);
            throw new FatalException(MessageFormatter.format(messageOnFalse, args).getMessage());
        }
    }

    public static void fatalException(Throwable ex, Logger log, String message, Object... args) {
        log.error(message, args, ex);
        throw new FatalException(MessageFormatter.format(message, args).getMessage(), ex);
    }

    public static void argumentAssertion(boolean assertion, Logger log, String messageOnFalse, Object... args) {
        if (!assertion) {
            log.error(messageOnFalse, args);
            throw new IllegalArgumentException(MessageFormatter.format(messageOnFalse, args).getMessage());
        }
    }

    // STRING HANDLING

    public static String safeToString(Object o) {
        return (o != null) ? o.toString() : "null";
    }

    public static String safeToString(String s) {
        return (s != null) ? s : "null";
    }

    public static String safeToName(Object o) {
        return (o != null) ? o.getClass().getCanonicalName() : "null";
    }

    public static String safeToName(Class c) {
        return (c != null) ? c.getCanonicalName() : "null";
    }

    public static String safeToName(Method m) {
        return (m != null) ? m.getDeclaringClass().getCanonicalName() + "#" + m.getName() : "null";
    }

    public static String safeToName(Field f) {
        return (f != null) ? f.getDeclaringClass().getCanonicalName() + "#" + f.getName() : "null";
    }

    public static boolean safeEquals(Object a, Object b) {
        return (a != null && b != null) && a.equals(b);
    }

    public static boolean isEmpty(String s) {
        return (s == null) || s.length() == 0;
    }

    public static boolean notEmpty(String s) {
        return (s != null) && (s.length() > 0);
    }

    public static String camelCase(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1, s.length());
    }

    public static String listToString(List<?> list, String delimiter) {
        return (list == null || list.size() == 0) ? "empty" :
                list.stream().map(x -> (x != null) ? x.toString() : "null").collect(Collectors.joining(delimiter));
    }

    public static String listToString(Object[] list, String delimiter) {
        return (list == null || list.length == 0) ? "empty" :
                Arrays.stream(list).map(x -> (x != null) ? x.toString() : "null")
                        .collect(Collectors.joining(delimiter));
    }

    public static String concatenate(String... strings) {
        StringBuilder builder = new StringBuilder();
        for (String s : strings) builder.append(s);
        return builder.toString();
    }

    public static String concatenate(Object... objects) {
        StringBuilder builder = new StringBuilder();
        for (Object o : objects) builder.append(safeToString(o));
        return builder.toString();
    }

    @SafeVarargs
    public static <T> List<T> addToList(List<T> target, T... items) {
        Collections.addAll(target, items);
        return target;
    }

    // PATHS

    public static boolean isEmpty(Path p) {
        return (p == null) || "".equals(p.toString());
    }

    /**
     * Remove ending from path, using Path methods.
     * Objective of this function is to safely handle the operation in a platform independent way
     * <p>
     * NOTE: Please call normalize() on the path before calling this method.
     *
     * @param path   path to be cut
     * @param ending relative path element to cut
     * @return the reduced path
     */
    public static Path removePathEnding(Path path, Path ending) {
        assert path.isAbsolute();
        assert path.endsWith(ending);

        int levels = ending.getNameCount();
        while (levels-- > 0) {
            path = path.getParent();
        }
        return path;
    }

    /**
     * Filename Extension is the text AFTER the FILE_EXTENSION_DELIMITER
     *
     * @param filename for example, "foo.txt"
     * @return txt
     */
    public static String getFilenameExtension(String filename) {
        String extension = "";
        int i = filename.lastIndexOf(Tokens.FILE_EXTENSION_DELIMITER);
        if (i > 0 && i < filename.length() - 1) {
            extension = filename.substring(i + 1);
        }
        return extension;
    }

    /**
     * Filename Extension is the text AFTER the FILE_EXTENSION_DELIMITER
     * If an extension does not exist, then append one, otherwise switch the extension to that provided
     *
     * @param filename  the filename to modify, for example, "foo.txt"
     * @param extension the requested extension, for example, "xml"
     * @return the modified filename, for example, "foo.xml"
     */
    public static String setFilenameExtension(String filename, String extension) {
        assert Common.notEmpty(extension) && !extension.contains(Tokens.FILE_EXTENSION_DELIMITER_S);
        String targetFileName;
        String current = Tokens.FILE_EXTENSION_DELIMITER + getFilenameExtension(filename);
        String target = Tokens.FILE_EXTENSION_DELIMITER + extension;

        if (current.length() == 1) { // just .
            targetFileName = filename + target;
        } else {
            targetFileName = filename.replaceAll(current, target);
        }
        return targetFileName;
    }

    // count the number of leading characters c
    public static int leadingCharCount(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) count++;
            else break;
        }
        return count;
    }

    // count the number of characters c
    public static int charCount(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) count++;
        }
        return count;
    }

    // count the number of trailing characters c
    public static int trailingCharCount(String s, char c) {
        int count = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == c) count++;
            else break;
        }
        return count;
    }
}
