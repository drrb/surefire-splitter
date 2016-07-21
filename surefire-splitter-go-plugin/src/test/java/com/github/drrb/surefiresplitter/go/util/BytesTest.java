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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class BytesTest {

    @Test
    public void parsesHumanReadableAmounts() {
        assertThat(Bytes.of("10m"), is(10000000));
        assertThat(Bytes.of("10 MB"), is(10000000));
    }

    @Test
    public void rendersAsHumanReadable() {
        assertThat(Bytes.render(100000), is("100.0 KB"));
        assertThat(Bytes.render(1234000), is("1.234 MB"));
    }

    @Test
    public void rendersNegativeAsUnknown() {
        assertThat(Bytes.render(-1), is("unknown size"));
    }
}
