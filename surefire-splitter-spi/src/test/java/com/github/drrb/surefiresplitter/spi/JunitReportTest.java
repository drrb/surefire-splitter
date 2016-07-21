/**
 * surefire-splitter-spi
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
 * along with surefire-splitter-spi. If not, see <http://www.gnu.org/licenses />.
 */
package com.github.drrb.surefiresplitter.spi;

import com.github.drrb.surefiresplitter.spi.JunitReport.JunitTestCase;
import com.github.drrb.surefiresplitter.spi.JunitReport.JunitTestSuite;
import org.junit.Test;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JunitReportTest {

    @Test
    public void shouldParseXmlFile() throws Exception {
        JunitTestSuite suite = JunitReport.parse(Paths.get("src/test/resources/com/github/drrb/surefiresplitter/spi/TEST-com.github.tlb.sampleprojects.CommutativeTest.xml"));
        assertThat(suite.getName(), is("com.github.tlb.sampleprojects.CommutativeTest"));
        assertThat(suite.getTime(), is(0.012));
        assertThat(suite.getCases().size(), is(4));
        JunitTestCase testCase = suite.getCases().get(0);
        assertThat(testCase.getName(), is("shouldShowThatDivisionIsNotCommutative"));
        assertThat(testCase.getClassName(), is("com.github.tlb.sampleprojects.CommutativeTest"));
    }

}
