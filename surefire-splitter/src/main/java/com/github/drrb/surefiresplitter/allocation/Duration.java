/**
 * Surefire Splitter
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
 * along with Surefire Splitter. If not, see <http://www.gnu.org/licenses />.
 */
package com.github.drrb.surefiresplitter.allocation;

public class Duration implements Comparable<Duration> {
    public static final Duration UNKNOWN = new Duration(0.0001);
    private final Double value;

    public static Duration of(Double value) {
        if (value == null) {
            return UNKNOWN;
        } else {
            return new Duration(value);
        }
    }

    private Duration(Double value) {
        this.value = value;
    }

    @Override
    public int compareTo(Duration that) {
        return this.value.compareTo(that.value);
    }

    public Duration plus(Duration that) {
        return new Duration(this.value + that.value);
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }
}
