/**
 * Surefire Splitter File Plugin
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
 * along with Surefire Splitter File Plugin. If not, see <http://www.gnu.org/licenses />.
 */
package com.github.drrb.surefiresplitter.file;

import com.github.drrb.surefiresplitter.spi.JunitReport.JunitTestSuite;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.rules.ExpectedException.none;

public class DirectoryReportRepoTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    @Rule
    public final ExpectedException exceptions = none();

    @Test
    public void readsAllXmlFilesInDir() throws Exception {
        List<JunitTestSuite> parsedReports = new DirectoryReportRepo("src/test/resources/com/github/drrb/surefiresplitter/sample-reports").getTestSuites();
        assertThat(parsedReports.size(), is(9));
    }

    @Test
    public void raisesExceptionIfDirectoryNotReadable() throws Exception {
        File reportsDir = tempDir.newFolder("reports");
        DirectoryReportRepo repo = new DirectoryReportRepo(reportsDir.toString());
        reportsDir.delete();

        exceptions.expectMessage("Couldn't read from JUnit report directory");
        repo.getTestSuites();
    }
}
