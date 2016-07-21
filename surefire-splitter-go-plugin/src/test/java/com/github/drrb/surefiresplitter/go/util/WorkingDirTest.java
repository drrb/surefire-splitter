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

import java.io.File;
import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class WorkingDirTest {

    @Test
    public void returnsTheCurrentWorkingDir() throws IOException {
        String expected = new File(".").getCanonicalFile().getAbsolutePath();
        String actual = WorkingDir.get().toAbsolutePath().toString();
        assertThat(actual, is(expected));
    }
}
