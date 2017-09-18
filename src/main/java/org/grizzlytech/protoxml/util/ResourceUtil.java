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
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static org.grizzlytech.protoxml.util.Tokens.*;

/**
 * NOTE: jar:file:/C:/Dev/Projects/IDEA/protoxml-test/lib/protoxml-iso20022-1.0.jar!/schema/head/head.001.001.01.AppHdr.xsd
 */
public class ResourceUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceUtil.class);


    /**
     * Get a resourceURL using the resourceName (e.g., employee.xsd).
     * <p>
     * This method will find the named resource within a list of relative paths, then retrieve the URL from
     * the provided ClassLoader.
     *
     * @param classLoader   the source of available resources
     * @param resourceName  the resource to find
     * @param resourcePaths the set of available (relative) paths
     * @return the URL for the resource
     */
    public static URL getResourceURLByName(ClassLoader classLoader, String resourceName, List<String> resourcePaths) {
        URL resourceURL = null;

        String resourcePath = getResourcePath(resourceName, resourcePaths);
        if (resourcePath == null) {
            LOG.error("Could not locate resource {} in resourcePath list", resourceName);
        } else {
            resourceURL = classLoader.getResource(resourcePath);
            if (resourceURL == null) {
                LOG.error("Could not locate resource {} in ClassLoader", resourcePath);
            }
        }
        return resourceURL;
    }

    /**
     * Return the path of a named resource within a list of paths
     *
     * @param resourceName  the resource to find
     * @param resourcePaths the set of available (relative) paths
     * @return the matching path else null
     */
    public static String getResourcePath(String resourceName, List<String> resourcePaths) {
        return resourcePaths.stream()
                .filter(x -> x.toUpperCase().contains(resourceName.toUpperCase()))
                .findFirst().orElse(null);
    }

    /**
     * Return a list of available resource paths located under searchPath and matching
     * the extensionFilter predicate
     *
     * @param classLoader     the source of available resources
     * @param searchPath      the relative starting point
     * @param extensionFilter the filename filter to apply
     * @return the list of matching paths
     */
    public static List<String> getResourcePaths(ClassLoader classLoader, Path searchPath, Predicate<String> extensionFilter) {
        assert classLoader != null;
        assert searchPath != null;

        List<String> resources = new ArrayList<>();

        try {
            Enumeration<URL> resourceURLs = classLoader.getResources(searchPath.toString());

            List<File> dirs = new ArrayList<>();
            List<JarFile> jars = new ArrayList<>();

            while (resourceURLs.hasMoreElements()) {
                URL resourceURL = resourceURLs.nextElement();
                switch (resourceURL.getProtocol()) {
                    case URL_PROTOCOL_FILE:
                        String dirPath = URLDecoder.decode(resourceURL.getFile(), "UTF-8");
                        dirs.add(new File(dirPath));
                        break;
                    case URL_PROTOCOL_JAR:
                        // Retrieve the path of the jar removing
                        // 1. the resource path information included after the path separator
                        // 2. the Jar protocol prefix
                        String jarPath = resourceURL.getPath().split(JAR_PATH_SEPARATOR_S)[0]
                                .replace(URL_PROTOCOL_FILE + URL_PROTOCOL_DELIMITER, "");
                        jars.add(new JarFile(jarPath));
                        break;
                    default:
                        LOG.info("Ignoring {}", resourceURL.toString());
                }
            }
            for (File directory : dirs) {
                resources.addAll(findResourcesInDir(directory, searchPath, extensionFilter));
            }
            for (JarFile jarFile : jars) {
                resources.addAll(findResourcesInJar(jarFile, extensionFilter));
            }
        } catch (IOException ex) {
            LOG.error("PROBLEM", ex);
        }

        return resources;
    }

    /**
     * @param directory       directory to be walked
     * @param searchPath      requested relative starting point
     * @param extensionFilter filename filter to apply
     * @return List of matching resources
     * @throws IOException if there is an error walking the directory
     */
    static List<String> findResourcesInDir(File directory, Path searchPath, Predicate<String> extensionFilter)
            throws IOException {
        Common.fatalAssertion(directory.exists(), LOG, "The directory {} does not exist",
                directory.getAbsoluteFile().toString());

        // Determine the base path that contains the resources. This is needed, as we need to collect
        // the paths of the resources relative to the base path
        Path basePath = !Common.isEmpty(searchPath) ? Common.removePathEnding(directory.toPath(), searchPath)
                : directory.toPath();

        // Create a matcher that applies the extensionFilter to the last part of the path name
        final BiPredicate<Path, BasicFileAttributes> MATCHER =
                (p, a) -> extensionFilter.test(p.getName(p.getNameCount() - 1).toString());
        final int MAX_DEPTH = 10; // assume 10 levels beneath searchPath is sufficient!

        return Files.find(directory.toPath(), MAX_DEPTH, MATCHER)
                // Determine the relative path of the resource vs the base path
                .map(basePath::relativize)
                .map(Path::toString)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<String> findResourcesInJar(JarFile jarFile, Predicate<String> extensionFilter) {
        return jarFile.stream()
                .map(JarEntry::toString)
                .filter(extensionFilter)
                .collect(Collectors.toCollection(ArrayList::new));
    }

}


