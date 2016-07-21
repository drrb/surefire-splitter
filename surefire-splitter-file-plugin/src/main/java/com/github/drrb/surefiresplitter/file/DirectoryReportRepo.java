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

import com.github.drrb.surefiresplitter.file.util.ListDirectory;
import com.github.drrb.surefiresplitter.spi.JunitReport;
import com.github.drrb.surefiresplitter.spi.JunitReport.JunitTestSuite;
import com.github.drrb.surefiresplitter.spi.ReportRepo;
import com.github.drrb.surefiresplitter.spi.ReportRepoProvider;

import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.List;

import static com.github.drrb.surefiresplitter.spi.FileFilters.XML_FILES;
import static java.nio.file.Files.isDirectory;

public class DirectoryReportRepo implements ReportRepo {

    public static class Provider implements ReportRepoProvider {

        @Override
        public boolean isAvailable() {
            return getReportDirectoryPath() != null && isDirectory(getReportDirectory());
        }

        @Override
        public ReportRepo getExistingReports() {
            return new DirectoryReportRepo(getReportDirectory());
        }

        private Path getReportDirectory() {
            return Paths.get(getReportDirectoryPath());
        }

        private String getReportDirectoryPath() {
            return System.getProperty("surefire.existingReportsDir");
        }

        @Override
        public String getDescription() {
            return "Surefire Report Directory splitter plugin" + (isAvailable() ? ": sourcing from '" + getReportDirectory() + "'" : "");
        }
    }

    private final Path directory;

    public DirectoryReportRepo(String directory) {
        this(Paths.get(directory));
    }

    private DirectoryReportRepo(Path directory) {
        if (!isDirectory(directory)) {
            throw new IllegalArgumentException("directory: Expected a directory, got " + directory);
        }
        this.directory = directory;
    }

    public List<JunitTestSuite> getTestSuites() {
        List<JunitTestSuite> reports = new LinkedList<>();
        for (Path reportFile : getReportFiles()) {
            try {
                reports.add(JunitReport.parse(reportFile));
            } catch (JunitReport.ReadFailure readFailure) {
                System.out.println(readFailure + " (" + readFailure.getCause() + ")");
            }
        }
        return reports;
    }

    private List<Path> getReportFiles() {
        try {
            return ListDirectory.listDirectory(directory, XML_FILES);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read from JUnit report directory '" + directory + "'", e);
        }
    }
}
