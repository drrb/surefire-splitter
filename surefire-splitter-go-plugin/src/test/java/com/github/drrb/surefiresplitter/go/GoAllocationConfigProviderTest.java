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
package com.github.drrb.surefiresplitter.go;

import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GoAllocationConfigProviderTest {

    public static final HashMap<String, String> ENV_ON_GO_WITH_SPLITTING = new HashMap<String, String>() {{
        put("GO_JOB_RUN_INDEX", "1");
        put("GO_JOB_RUN_COUNT", "2");
    }};

    public static final HashMap<String, String> ENV_ON_GO_WITHOUT_SPLITTING = new HashMap<String, String>() {{
    }};

    @Test
    public void isAvailableOnGoWhenSplittingEnabled() {
        GoAllocationConfigProvider provider = new GoAllocationConfigProvider(ENV_ON_GO_WITH_SPLITTING);
        assertThat(provider.isAvailable(), is(true));
        assertThat(provider.getDescription(), containsString("chunk 1 of 2"));
    }

    @Test
    public void isNotAvailableOnGoWhenSplittingNotEnabled() {
        GoAllocationConfigProvider provider = new GoAllocationConfigProvider(ENV_ON_GO_WITHOUT_SPLITTING);
        assertThat(provider.isAvailable(), is(false));
        assertThat(provider.getDescription(), not(containsString("chunk")));
    }
}
