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
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static org.grizzlytech.protoxml.util.Tokens.*;

/**
 * Utility class for scanning classes within the classpath.
 */
public class ClassUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ClassUtil.class);

    /**
     * @return all interfaces implemented by this and all parent classes
     */
    public static List<Class> getAllInterfaces(Class c) {
        return getAllInterfaces(c, new ArrayList<>());
    }

    private static List<Class> getAllInterfaces(Class c, List<Class> result) {
        // Add interfaces of class
        Collections.addAll(result, c.getInterfaces());

        // Add interfaces of parent class
        Class p = getSuperclassElseNull(c);
        if (p != null) {
            getAllInterfaces(p, result);
        }
        return result;
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     */
    public static List<Class> getClasses(String packageName, Predicate<String> classNameFilter) {
        ArrayList<Class> classes = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            assert classLoader != null;

            String path = packageName.replace(PACKAGE_NAME_DELIMITER, RESOURCE_PATH_DELIMITER);
            Enumeration<URL> resources = classLoader.getResources(path);

            List<File> dirs = new ArrayList<>();
            List<JarFile> jars = new ArrayList<>();

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                switch (resource.getProtocol()) {
                    case URL_PROTOCOL_FILE:
                        dirs.add(new File(resource.getFile()));
                        break;
                    case URL_PROTOCOL_JAR:
                        // Retrieve the path of the jar removing
                        // 1. the package information included after the path separator
                        // 2. the Jar protocol prefix
                        String jarPath = resource.getPath().split(JAR_PATH_SEPARATOR_S)[0]
                                .replace(URL_PROTOCOL_FILE + URL_PROTOCOL_DELIMITER, "");
                        jars.add(new JarFile(jarPath));
                        break;
                    default:
                        LOG.info("Ignoring {}", resource.toString());
                }
            }

            for (File directory : dirs) {
                classes.addAll(findClassesInDir(directory, packageName, classNameFilter));
            }
            for (JarFile jarFile : jars) {
                classes.addAll(findClassesInJar(jarFile, packageName, classNameFilter));
            }
        } catch (IOException ex) {
            Common.fatalException(ex, LOG, "Error creating class");
        }
        return classes;
    }

    private static List<Class> findClassesInDir(File directory, String packageName, Predicate<String> classNameFilter)
            throws IOException {
        // Determine the rooPath which contains the classes
        String packagePath = packageName.replace(PACKAGE_NAME_DELIMITER_S, File.separator);
        String rootPath = directory.getAbsolutePath().replace(packagePath, "");

        // Can Walk record the relative descent? Code below needs to manually infer packageName
        // by taking the rootPath from the absolutePath of the parent directory
        return Files.walk(directory.toPath())
                // Map the Path to File
                .map(Path::toFile)
                // Filter out files that are not classes
                .filter(x -> x.getName().endsWith(CLASS_FILE_SUFFIX))
                // Retrieve the canonical classname and apply the classnameFilter
                .map(x -> getClassName(x, getPackageName(x, rootPath)))
                .filter(classNameFilter)
                // Additionally filter out inner classes
                .filter(x -> x.indexOf(INNER_CLASS_DELIMITER) == -1)
                // Create and collect the corresponding classes
                .map(ClassUtil::getClassElseNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static String getClassName(File classFile, String packageName) {
        String fileName = classFile.getName();
        return packageName + (Common.notEmpty(packageName) ? PACKAGE_NAME_DELIMITER : "") +
                fileName.substring(0, fileName.length() - CLASS_FILE_SUFFIX.length());
    }

    private static String getPackageName(File classFile, String rootPath) {
        // Find the relative path of the package
        String packagePath = classFile
                .getParentFile()
                .getAbsolutePath()
                .replace(rootPath, "");

        // Remove a leading path separator if one exists
        // Edge case when search package is ""
        if (packagePath.startsWith(File.separator)) {
            packagePath = packagePath.substring(1, packagePath.length());
        }

        return packagePath.replace(File.separator, PACKAGE_NAME_DELIMITER_S);
    }

    private static List<Class> findClassesInJar(JarFile jarFile, String packageName, Predicate<String> classNameFilter) {
        String jarPath = packageName.replace(PACKAGE_NAME_DELIMITER, JAR_ENTRY_DELIMITER);

        return jarFile.stream()
                // Filter out entries that are not a) located within jarPath and b) classes
                .filter(x -> x.getName().contains(jarPath) && x.getName().endsWith(CLASS_FILE_SUFFIX))
                // Retrieve the canonical classname and apply the classnameFilter
                .map(ClassUtil::getClassName)
                .filter(classNameFilter)
                // Additionally filter out inner classes
                .filter(x -> x.indexOf(INNER_CLASS_DELIMITER) == -1)
                // Create and collect the corresponding classes
                .map(ClassUtil::getClassElseNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Map org/slf4j/event/Level.class to org.slf4j.event.Level
     *
     * @param jarEntry the entry to map
     * @return the canonical package name
     */
    private static String getClassName(JarEntry jarEntry) {
        String entryName = jarEntry.getName();
        return entryName.replace(JAR_ENTRY_DELIMITER, PACKAGE_NAME_DELIMITER)
                .substring(0, entryName.length() - CLASS_FILE_SUFFIX.length());
    }

    /**
     * Java 8 Lambdas do not support checked exceptions (without extension)
     * Hence either load the class, or log the error and return null
     *
     * @param className name of the class to lookup
     * @return the corresponding Class object or null (on ClassNotFoundException error)
     */
    public static Class getClassElseNull(String className) {
        Class result = null;
        try {
            result = Class.forName(className);
        } catch (ClassNotFoundException ex) {
            LOG.error("Cannot load class [{}]. Error [{}]", className, ex.getMessage());
        }
        return result;
    }

    /**
     * Return the superClass or null.
     *
     * @param clazz class whose superclass is requested
     * @return the superclass or null
     */
    public static Class getSuperclassElseNull(Class clazz) {
        Class superclass = clazz.getSuperclass();
        return ((superclass != clazz) && (superclass != Object.class)) ? superclass : null;
    }

    public static Object newInstanceElseNull(Class clazz) {
        Object result = null;
        try {
            result = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            LOG.error("Cannot instantiate class [{}]. Error [{}]", clazz.getCanonicalName(), ex.getMessage());
        }
        return result;
    }

    /**
     * Find all classes whose name matches a predicate and that implements a given interface
     *
     * @param packageName     the package to start searching from
     * @param classNameFilter predicate to filter class names, typically the interface name
     * @param interfaceFilter the Interface to be filtered on
     * @return a list of classes
     */
    public static List<Class> getImplementations(String packageName, Predicate<String> classNameFilter,
                                                 Class interfaceFilter) {
        List<Class> list = new ArrayList<>();

        List<Class> candidates = ClassUtil.getClasses(packageName, classNameFilter);
        for (Class c : candidates) {
            // Skip abstract classes as they cannot be instantiated
            if (Modifier.isAbstract(c.getModifiers())) {
                continue;
            }
            // Verify the class does indeed implement the interface before attempting to add it
            List<Class> interfaces = ClassUtil.getAllInterfaces(c);
            if (interfaces.contains(interfaceFilter)) {
                list.add(c);
            }
        }
        return list;
    }

    public static boolean isCoreJavaClass(Class clazz) {
        return clazz.getCanonicalName().startsWith("java") || clazz.isPrimitive();
    }

    public static boolean isCoreJavaClass(Object target) {
        return isCoreJavaClass(target.getClass());
    }

    /**
     * For example, domain.iso20022.sese would have a parent package of domain.iso20022
     *
     * @param packageName package whose parent package is requested
     * @return name of parent package or "" if there is no parent
     */
    public static String getParentPackageName(String packageName) {
        // domain.iso20022[.]sese
        int delimiter = packageName.lastIndexOf(Tokens.PACKAGE_NAME_DELIMITER);

        // domain.iso20022
        return (delimiter >= 1) ? packageName.substring(0, delimiter) : "";
    }

    /**
     * Return all methods that match the required signature
     *
     * @param clazz           class implementing the method(s)
     * @param nameFilter      method name filter
     * @param parameterTypes  method parameters
     * @param attributeFilter attribute filter (e.g., to test getModifiers)
     * @return methods that match
     */
    public static List<Method> getMethods(Class<?> clazz, Predicate<String> nameFilter, Class[] parameterTypes,
                                          Predicate<Method> attributeFilter) {
        return Arrays.stream(clazz.getMethods())
                .filter(m -> nameFilter.test(m.getName()))
                .filter(m -> Arrays.equals(m.getParameterTypes(), parameterTypes))
                .filter(attributeFilter)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
