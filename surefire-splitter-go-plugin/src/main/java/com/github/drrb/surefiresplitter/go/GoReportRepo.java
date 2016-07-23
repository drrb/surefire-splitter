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

import com.github.drrb.surefiresplitter.go.model.JobRun;
import com.github.drrb.surefiresplitter.go.util.WorkingDir;
import com.github.drrb.surefiresplitter.spi.JunitReport;
import com.github.drrb.surefiresplitter.spi.JunitReport.JunitTestSuite;
import com.github.drrb.surefiresplitter.spi.ReportRepo;
import com.github.drrb.surefiresplitter.spi.ReportRepoProvider;

import java.nio.file.Path;
import java.util.*;

import static com.github.drrb.surefiresplitter.spi.FileFilters.SUREFIRE_REPORT_FILES;

public class GoReportRepo implements ReportRepo {

    private static final int DEFAULT_NUMBER_OF_RUNS_TO_LOOK_BACK_FOR_REPORTS = 5;

    public static class Provider implements ReportRepoProvider {
        private final Map<String, String> env;
        private final Path workingDir;
        private final int numberOfRunsToLookBackForReports;

        @SuppressWarnings("unused") // Used by plugin SPI
        public Provider() {
            this(System.getenv(), WorkingDir.get());
        }

        Provider(Map<String, String> env, Path workingDir) {
            this(env, workingDir, DEFAULT_NUMBER_OF_RUNS_TO_LOOK_BACK_FOR_REPORTS);
        }

        Provider(Map<String, String> env, Path workingDir, int numberOfRunsToLookBackForReports) {
            this.env = env;
            this.workingDir = workingDir;
            this.numberOfRunsToLookBackForReports = numberOfRunsToLookBackForReports;
        }

        @Override
        public boolean isAvailable() {
            return env.containsKey("GO_SERVER_URL");
        }

        @Override
        public ReportRepo getExistingReports() {
            return new GoReportRepo(new GoAgent(env, workingDir, numberOfRunsToLookBackForReports));
        }

        @Override
        public String getDescription() {
            return "Go Report splitter plugin";
        }

    }

    private final GoAgent goAgent;
    private final GoServer goServer;
    private final List<JunitTestSuite> cachedTestSuites = new LinkedList<>();

    private GoReportRepo(GoAgent goAgent) {
        this.goAgent = goAgent;
        this.goServer = goAgent.getGoServer();
    }

    @Override
    public List<JunitTestSuite> getTestSuites() {
        if (cachedTestSuites.isEmpty()) {
            try {
                cachedTestSuites.addAll(downloadTestHistory());
            } catch (GoServer.CommunicationError | JunitReport.ReadFailure e) {
                throw new RuntimeException("Failed to get test history from Go", e);
            }
        }
        return new ArrayList<>(cachedTestSuites);
    }

    private List<JunitTestSuite> downloadTestHistory() throws JunitReport.ReadFailure, GoServer.CommunicationError {
        Set<JunitTestSuite> suites = new LinkedHashSet<>();

        for (JobRun previousJobRun : goServer.getPreviousJobRuns(goAgent.getCurrentJobRun())) {
            List<Path> jobFiles = goServer.downloadFiles(previousJobRun, SUREFIRE_REPORT_FILES);

            for (Path reportFile : jobFiles) {
                suites.add(JunitReport.parse(reportFile));
            }
        }
        return new ArrayList<>(suites);
    }
}
