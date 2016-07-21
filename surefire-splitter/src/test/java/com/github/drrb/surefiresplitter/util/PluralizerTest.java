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
package com.github.drrb.surefiresplitter.util;

import org.junit.Test;

import static com.github.drrb.surefiresplitter.util.Pluralizer.pluralize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PluralizerTest {

    @Test
    public void shouldNotPluralizeWhenThereIsOne() {
        assertThat(pluralize("chunk", 1), is("1 chunk"));
    }

    @Test
    public void shouldPluralizeWhenThereAreZero() {
        assertThat(pluralize("chunk", 0), is("0 chunks"));
    }

    @Test
    public void shouldPluralizeWhenThereAreMoreThanOne() {
        assertThat(pluralize("chunk", 2), is("2 chunks"));
    }
}
