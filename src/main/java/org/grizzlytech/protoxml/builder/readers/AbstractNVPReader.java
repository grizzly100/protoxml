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

import org.grizzlytech.protoxml.beans.Bean;
import org.grizzlytech.protoxml.beans.PropertyPath;
import org.grizzlytech.protoxml.builder.NVPReader;
import org.grizzlytech.protoxml.util.Common;
import org.grizzlytech.protoxml.util.NVP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of the reader.
 * <p>
 * Supports the @With directive
 */
public abstract class AbstractNVPReader implements NVPReader {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractNVPReader.class);

    protected Callback handler;

    protected String withContext = null;

    protected String lastError = null;

    public void read(Callback handler) {
        this.handler = handler;
        readAll();
    }

    protected abstract void readAll();

    /**
     * readAll() should call handle on ever record
     *
     * @param text    text item read
     * @param lineNum line#
     */
    protected void handle(String text, int lineNum) {
        if (Common.isEmpty(text) || text.startsWith(COMMENT_PREFIX_S)) {
            // Return the blank / comment
            handleComment(text.trim(), lineNum);
        } else {
            int separatorIndex = text.indexOf(NVP_DELIMITER_S);
            if (separatorIndex >= 0) {
                // Parse name and value into a NVP
                String name = text.substring(0, separatorIndex).trim();
                String value = text.substring(separatorIndex + 1, text.length()).trim();
                NVP<String> pair = new NVP<>(name, value);

                // Handle the directive or NVP
                if (name.startsWith(DIRECTIVE_PREFIX_S)) {
                    handleComment(pair.toString(), lineNum);
                    if (!handleDirective(pair, lineNum)) {
                        if (Common.isEmpty(getLastError())) {
                            setLastError("Unknown Directive");
                        }
                        handleComment(pair.toString(), lineNum); // comment failure
                    }
                } else {
                    handleNVP(pair, lineNum);
                }
            } else {
                LOG.error("Line {}: Text [{}] not an NPV", lineNum, text);
                setLastError("Not an NPV");
                handleComment(text, lineNum);
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean handleDirective(NVP<String> pair, int lineNum) {
        boolean handled = false;
        if (WITH_DIRECTIVE.equalsIgnoreCase(pair.getName())) {
            handled = handleWithDirective(pair, lineNum);
        }
        return handled;
    }

    protected void handleNVP(NVP<String> pair, int lineNum) {
        applyWithDirective(pair, lineNum);
        handler.onNVP(pair, lineNum);
    }

    protected void handleComment(String comment, int lineNum) {
        if (Common.notEmpty(getLastError())) {
            comment += " [FAIL: " + getLastError() + "]";
            setLastError(null);
        }
        handler.onComment(comment, lineNum);
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    private boolean handleWithDirective(NVP<String> pair, int lineNum) {
        boolean handled = false;
        if (!pair.getValue().endsWith(PropertyPath.PATH_DELIMITER_S)) {
            this.withContext = pair.getValue();
            handled = true;
            LOG.debug("@with [{}]", withContext);
        } else {
            LOG.error("@with value [{}] must not ends with [{}]", pair.getValue(), PropertyPath.PATH_DELIMITER_S);
            setLastError("ends with " + PropertyPath.PATH_DELIMITER_S);
        }
        return handled;
    }

    private void applyWithDirective(NVP<String> pair, int lineNum) {
        String name = pair.getName();
        // Apply With directive, if in place
        if (Common.notEmpty(this.withContext)) {
            if (name.startsWith(PropertyPath.PATH_DELIMITER_S) ||
                    name.startsWith(Bean.CLASS_SUFFIX_S)) {
                pair.setName(this.withContext + name);
            } else {
                this.withContext = null; // end of with block
            }
        }
    }
}
