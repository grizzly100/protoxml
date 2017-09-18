package org.grizzlytech.protoxml.main;


import org.grizzlytech.protoxml.util.TestPaths;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class MainTest {
    @Test
    public void main() throws Exception {

        String inputFileName = Paths.get(TestPaths.getTestResourcesDir().getAbsolutePath(),
                "examples", "employee-01.txt").toString();

        File inputFile = new File(inputFileName);
        assertTrue(inputFile.exists());
        assertTrue(inputFile.isFile());

        String outputFileName = Paths.get(TestPaths.getTestClassesDir().getAbsolutePath(),
                "examples", "employee-01.xml").toString();
        File outputFile = new File(outputFileName);
        File traceFile = new File(outputFile.getAbsolutePath() + ".trc");
        File validationFile = new File(outputFile.getAbsolutePath() + ".val");

        // Clear down output files
        assertTrue(!outputFile.exists() || outputFile.delete());
        assertTrue(!traceFile.exists() || traceFile.delete());
        assertTrue(!validationFile.exists() || validationFile.delete());

        // Run Main
        Main.main(new String[]{inputFileName, outputFileName});

        assertTrue(outputFile.exists());
        assertTrue(traceFile.exists());
        assertTrue(validationFile.exists());
    }
}
