package org.grizzlytech.protoxml.util;


import java.io.File;
import java.nio.file.Paths;

/**
 * Utility functions to access paths.
 * <p>
 * This class is only needed in test, as it resolves the installation (project dir) and and target
 * test class directories.
 *
 * @See XMLSchemaOutputResolverTest and GenerateXSD which both write files into these paths
 */
public class TestPaths {

    /**
     * E.g., C:\Dev\Projects\IDEA\protoxml\target\test-classes
     *
     * @return the directory containing the test classes
     */
    public static File getTestClassesDir() {
        return new File(TestPaths.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    }

    /**
     * Assumes the project folder is two levels higher than the test-classes folder
     * <p>
     * E.g., C:\Dev\Projects\IDEA\protoxml\
     *
     * @return the directory containing the project
     */
    public static File getProjectDir() {
        return getTestClassesDir().getParentFile().getParentFile();
    }

    /**
     * Assumes test resources folder follows Maven convention relative to project folder
     * <p>
     * E.g., C:\Dev\Projects\IDEA\protoxml\src\test\resources
     *
     * @return the directory containing the resources.
     */
    public static File getTestResourcesDir() {
        return Paths.get(getProjectDir().getAbsolutePath(), "src", "test", "resources").toFile();
    }
}
