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

package org.grizzlytech.protoxml.builder.readers;

import org.grizzlytech.protoxml.util.NVP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper class to read files that contain name=value pairs
 * <p>
 * Supports the @include directive, which enables other files to be read
 */
final public class NVPFileReader extends AbstractNVPReader {

    private static final Logger LOG = LoggerFactory.getLogger(NVPFileReader.class);
    // Read properties from the provided filename
    public final String INCLUDE_DIRECTIVE = "@include";
    private File sourceFile;

    /**
     * Read the file and invoke the callback as NVPs are read
     */
    public void readAll() {
        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile))) {
            String line;
            int lineNum = 0;

            while ((line = br.readLine()) != null) {
                lineNum++;
                line = line.trim();
                super.handle(line, lineNum);
            }
        } catch (IOException ex) {
            setLastError(ex.getMessage());
            LOG.error("Error reading file [{}]", this.sourceFile, ex);
        }
    }

    @Override
    protected boolean handleDirective(NVP<String> pair, int lineNum) {
        boolean handled;
        if (INCLUDE_DIRECTIVE.equalsIgnoreCase(pair.getName())) {
            handled = handleIncludeDirective(pair, lineNum);
        } else {
            handled = super.handleDirective(pair, lineNum);
        }
        return handled;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    private boolean handleIncludeDirective(NVP<String> pair, int lineNum) {
        boolean handled = false;
        NVPFileReader includedReader = new NVPFileReader();

        Path includedPath = Paths.get(pair.getValue());
        if (!includedPath.isAbsolute()) {
            String workingDir = this.sourceFile.getParent();
            includedPath = Paths.get(workingDir, includedPath.toString());
        }
        File includedFile = includedPath.toFile();

        if (includedFile.exists()) {
            includedReader.setSourceFile(includedFile);
            includedReader.read(this.handler);
            handled = true;
        } else {
            LOG.error("Line {}: File does not exist: [{}]", lineNum, includedFile.getAbsolutePath());
            setLastError(includedFile.getAbsolutePath() + " does not exist");
        }
        return handled;
    }
}
