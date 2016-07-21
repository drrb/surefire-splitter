/**
 * Surefire Splitter Go Plugin
 * Copyright (C) 2016 drrb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Surefire Splitter Go Plugin. If not, see <http://www.gnu.org/licenses />.
 */
package com.github.drrb.surefiresplitter.go.util;

public class Bytes {

    public static int of(String humanReadableSize) {
        String number = humanReadableSize.replaceAll("\\s*[mM][bB]?", "000000");
        return Integer.parseInt(number);
    }

    public static String render(long number) {
        if (number < 0) {
            return "unknown size";
        }
        long gb = number / 1000000000;
        number = number % 1000000000;
        long mb = number / 1000000;
        number = number % 1000000;
        long kb = number / 1000;
        long b = number % 1000;
        if (gb > 0) {
            return gb + "."  + mb + " GB";
        } else if (mb > 0) {
            return mb + "."  + kb + " MB";
        } else if (kb > 0) {
            return kb + "." + b + " KB";
        } else {
            return b + " B";
        }
    }

    private Bytes() {
    }
}
