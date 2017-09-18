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

package org.grizzlytech.protoxml.builder;


import org.grizzlytech.protoxml.util.NVP;

public interface NVPReader {

    /**
     * Lines containing comments are ignored (traced but not processed)
     */
    String COMMENT_PREFIX_S = "#";

    /**
     * Lines containing the NVP delimiter are assumed to contain a name and a value
     */
    String NVP_DELIMITER_S = "=";

    /**
     * Lines starting with "@" are NVP directives for the NVPReader
     */
    String DIRECTIVE_PREFIX_S = "@";

    /**
     * Prepend the directives "value" to all subsequent properties beginning with PropertyPath.PATH_DELIMITER
     */
    String WITH_DIRECTIVE = "@with";

    /**
     * Read a data set and invoke the callback handler as NVPs and comments are read
     *
     * @param handler the callback handler to invoke when NVP and Comments are encountered
     */
    void read(Callback handler);

    interface Callback {
        void onNVP(NVP<String> pair, int line);

        /**
         * the full comment, including the comment delimiter is returned
         */
        void onComment(String comment, int line);
    }
}
