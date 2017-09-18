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


import org.grizzlytech.protoxml.util.Common;
import org.grizzlytech.protoxml.util.Tokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

/**
 * Execute a batch of XML processing jobs
 */
public class Batch {

    private static final Logger LOG = LoggerFactory.getLogger(Batch.class);

    private static final String DEFAULT_SOURCE_EXT = "txt";
    private static final String XML_EXT = "xml";

    private final File sourceDir;
    private final File targetDir;
    private final FilenameFilter filenameFilter;

    private ProtoAPI api;

    public Batch(File sourceDir, File targetDir, FilenameFilter filenameFilter) {
        this.sourceDir = sourceDir;
        this.targetDir = targetDir;
        this.filenameFilter = filenameFilter;
    }

    public static void main(String args[]) {

        if (args.length < 1) {
            LOG.error("USAGE: [source-dir] [target-dir] (source-filter]");
            System.exit(-1);
        }

        File sourceDir = getDirectory(args[0]);
        File targetDir = (args.length >= 2) ? getDirectory(args[1]) : sourceDir;

        // Check sourceExtension
        String sourceExtension = (args.length >= 3) ? args[2].toLowerCase() : DEFAULT_SOURCE_EXT;
        Common.fatalAssertion(
                Common.notEmpty(sourceExtension) && !sourceExtension.contains(Tokens.FILE_EXTENSION_DELIMITER_S),
                LOG, "sourceExtension {} cannot contain " + Tokens.FILE_EXTENSION_DELIMITER_S, sourceExtension);

        FilenameFilter filter = (dir, name) -> name.toLowerCase().endsWith(sourceExtension);

        if (sourceDir != null & targetDir != null) {
            (new Batch(sourceDir, targetDir, filter)).run();
        }
    }

    protected static File getDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists() && !dir.isDirectory()) {
            LOG.error("Path is not an existing directory: [{}]", path);
            dir = null;
        }
        return dir;
    }

    /**
     * Process all files in the sourceDir that match the required extension
     */
    public void run() {
        LOG.info("Batch: Source[{}] and Target[{}]", sourceDir.getAbsolutePath(), targetDir.getAbsolutePath());

        this.api = new ProtoAPI();

        File[] files = this.sourceDir.listFiles(filenameFilter);
        if (files != null && files.length > 0) {
            Arrays.stream(files).forEach(this::process);
        }
    }

    /**
     * Invoke the ProtoXML createAndValidateXMLFile on the source file
     * @param sourceFile file to be processed
     */
    public void process(File sourceFile) {
        // Determine the targetFile, based on the targetDir and by replacing the source file extension
        String targetFileName = this.targetDir.getAbsolutePath() + File.separator +
                Common.setFilenameExtension(sourceFile.getName(), XML_EXT);
        File targetFile = new File(targetFileName);

        // Remove existing target file (if applicable)
        if (targetFile.exists()) {
            if (!targetFile.delete())
            {
                LOG.error("Unable to delete existing file [{}]", targetFile.toString());
                return;
            }
        }

        // Invoke API
        try {
            api.createAndValidateXMLFile(sourceFile, targetFile);
        } catch (IOException ex) {
            LOG.error("Problem [{}]->[{}] Error[{}]", sourceFile.getAbsolutePath(), targetFile.getAbsolutePath(),
                    ex.getMessage());
        }
    }
}
