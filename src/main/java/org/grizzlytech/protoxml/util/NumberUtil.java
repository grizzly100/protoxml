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

public class NumberUtil {

    /**
     * Return true if candidate contains only sign (optional), digits and a decimal point (optional)
     *
     * @param candidate  string to be examined
     * @param attributes attributes observed
     * @return true if a number
     */
    public static boolean isNumber(String candidate, NumberAttributes attributes) {
        attributes.reset();
        boolean positive = true;
        int precision = 0;
        int scale = 0;
        boolean hasDecimal = false;
        boolean hasSign = false;

        for (char c : candidate.toCharArray()) {
            if (c == '+' || c == '-') {
                if (!hasSign) {
                    positive = (c == '+');
                    hasSign = true;
                } else {
                    return false; // second sign
                }
            } else if (c == '.') {
                if (!hasDecimal) {
                    hasDecimal = true;
                } else {
                    return false; // second period
                }
            } else if (Character.isDigit(c)) {
                precision++;
                if (hasDecimal) {
                    scale++;
                }
            } else {
                return false; // any other character
            }
        }
        // Set the attributes
        attributes.setPositive(positive);
        attributes.setPrecision(precision);
        attributes.setScale(scale);
        attributes.setTrailingZeros( // only if scale > 0
                (scale > 0) ? Common.trailingCharCount(candidate, '0') : 0);
        return true;
    }

    /**
     * Remove trailing zeros, and decimal point (if number is an integer)
     *
     * @param number e.g., -200.00
     * @return -200
     */
    public static String removeTrailingZeros(String number) {
        NumberAttributes attributes = new NumberAttributes();
        boolean isNumber = isNumber(number, attributes);
        assert isNumber;

        // Truncate the zeros, and if the number is an Integer, remove the decimal point as well
        int truncateChars = attributes.getTrailingZeros() + (attributes.isInteger() ? 1 : 0);
        return (truncateChars > 0) ? number.substring(0, number.length() - truncateChars) : number;
    }

    /**
     * @See isNumber()
     */
    public static class NumberAttributes {
        private boolean positive;
        private int precision;
        private int scale;
        private int trailingZeros;

        public NumberAttributes() {
            reset();
        }

        public boolean isPositive() {
            return positive;
        }

        public void setPositive(boolean positive) {
            this.positive = positive;
        }

        public int getPrecision() {
            return precision;
        }

        public void setPrecision(int precision) {
            this.precision = precision;
        }

        public int getScale() {
            return scale;
        }

        public void setScale(int scale) {
            this.scale = scale;
        }

        public int getTrailingZeros() {
            return trailingZeros;
        }

        public void setTrailingZeros(int trailingZeros) {
            this.trailingZeros = trailingZeros;
        }

        public boolean isInteger() {
            return (scale == 0) || (scale == trailingZeros);
        }

        public void reset() {
            positive = false;
            precision = -1;
            scale = -1;
            trailingZeros = 0;
        }
    }

}
