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

import com.github.drrb.surefiresplitter.spi.JunitReport.JunitTestSuite;
import com.github.drrb.surefiresplitter.spi.ReportRepo;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.squareup.okhttp.mockwebserver.SocketPolicy.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.rules.ExpectedException.none;

public class GoReportRepoTest {

    private static final Map<String, String> ENV_NOT_ON_GO = Collections.emptyMap();
    private static final Map<String, String> ENV_ON_GO = new HashMap<String, String>() {{
        put("GO_SERVER_URL", "https://localhost:28154/go/");
        put("GO_PIPELINE_NAME", "PipelineName");
        put("GO_PIPELINE_COUNTER", "542");
        put("GO_STAGE_NAME", "StageName");
        put("GO_STAGE_COUNTER", "1");
        put("GO_JOB_NAME", "JobName-runInstance-1");
        put("GO_USERNAME", "admin");
        put("GO_PASSWORD", "badger");
    }};

    @Rule
    public final FakeGoServer server = new FakeGoServer(28154);

    @Rule
    public final TemporaryFolder agentBasedir = new TemporaryFolder();
    @Rule
    public final ExpectedException exceptions = none();

    private Path workingDir;

    @Before
    public void setUpAgentDir() throws Exception {
        agentBasedir.create();
        File agentConfigDir = agentBasedir.newFolder("config");
        server.downloadAgentKeyStores(agentConfigDir.toPath());
        workingDir = agentBasedir.newFolder("pipelines", "PipelineName").toPath();
    }

    @Test
    public void shouldBeAvailableWhenRunningOnGo() {
        GoReportRepo.Provider provider = new GoReportRepo.Provider(ENV_ON_GO, workingDir);
        assertThat(provider.isAvailable(), is(true));
    }

    @Test
    public void shouldNotBeAvailableWhenRunningOffGo() {
        GoReportRepo.Provider provider = new GoReportRepo.Provider(ENV_NOT_ON_GO, workingDir);
        assertThat(provider.isAvailable(), is(false));
    }

    @Test
    public void shouldGetTestReportsFromAllSplitsOfCurrentJob() throws Exception {
        GoReportRepo.Provider provider = new GoReportRepo.Provider(ENV_ON_GO, workingDir);
        assertThat(provider.isAvailable(), is(true));

        List<JunitTestSuite> testSuites = provider.getExistingReports().getTestSuites();
        assertThat(testSuites.size(), is(5));
        assertThat(testSuites.get(0).getName(), is("com.example.myproject.FirstTest"));
        assertThat(testSuites.get(0).getTime(), is(0.001));
        assertThat(testSuites.get(1).getName(), is("com.example.myproject.SecondTest"));
        assertThat(testSuites.get(1).getTime(), is(0.002));
        assertThat(testSuites.get(2).getName(), is("com.example.myproject.ThirdTest"));
        assertThat(testSuites.get(2).getTime(), is(0.003));
        assertThat(testSuites.get(3).getName(), is("com.example.myproject.FifthTest"));
        assertThat(testSuites.get(3).getTime(), is(0.005));
        assertThat(testSuites.get(4).getName(), is("com.example.myproject.FourthTest"));
        assertThat(testSuites.get(4).getTime(), is(0.004));
    }

    @Test
    public void shouldGetTestReportsFromPreviousRunsOfTheTargetJobBackToTheLastPassedOne() throws Exception {
        Map<String, String> nextRunEnv = new HashMap<>(ENV_ON_GO);
        nextRunEnv.put("GO_PIPELINE_COUNTER", "543");
        GoReportRepo.Provider provider = new GoReportRepo.Provider(nextRunEnv, workingDir);
        assertThat(provider.isAvailable(), is(true));

        List<JunitTestSuite> testSuites = provider.getExistingReports().getTestSuites();
        assertThat(testSuites.size(), is(5));
        assertThat(testSuites.get(0).getName(), is("com.example.myproject.FirstTest"));
        assertThat(testSuites.get(0).getTime(), is(0.011));
        assertThat(testSuites.get(1).getName(), is("com.example.myproject.SecondTest"));
        assertThat(testSuites.get(1).getTime(), is(0.022));
        assertThat(testSuites.get(2).getName(), is("com.example.myproject.ThirdTest"));
        assertThat(testSuites.get(2).getTime(), is(0.033));
        assertThat(testSuites.get(3).getName(), is("com.example.myproject.FourthTest"));
        assertThat(testSuites.get(3).getTime(), is(0.044));
        assertThat(testSuites.get(4).getName(), is("com.example.myproject.FifthTest"));
        assertThat(testSuites.get(4).getTime(), is(0.005));
    }

    @Test
    public void shouldOnlyLookAsFarBackInHistoryAsConfigured() throws Exception {
        Map<String, String> nextRunEnv = new HashMap<>(ENV_ON_GO);
        nextRunEnv.put("GO_PIPELINE_COUNTER", "543");
        GoReportRepo.Provider provider = new GoReportRepo.Provider(nextRunEnv, workingDir, 1);
        assertThat(provider.isAvailable(), is(true));

        List<JunitTestSuite> testSuites = provider.getExistingReports().getTestSuites();
        assertThat(testSuites.size(), is(4));
        assertThat(testSuites.get(0).getName(), is("com.example.myproject.FirstTest"));
        assertThat(testSuites.get(0).getTime(), is(0.011));
        assertThat(testSuites.get(1).getName(), is("com.example.myproject.SecondTest"));
        assertThat(testSuites.get(1).getTime(), is(0.022));
        assertThat(testSuites.get(2).getName(), is("com.example.myproject.ThirdTest"));
        assertThat(testSuites.get(2).getTime(), is(0.033));
        assertThat(testSuites.get(3).getName(), is("com.example.myproject.FourthTest"));
        assertThat(testSuites.get(3).getTime(), is(0.044));
    }

    @Test
    public void shouldCacheReportsBetweenMavenModuleTestRuns() throws Exception {
        List<JunitTestSuite> testSuites;
        ReportRepo repoInFirstModule = new GoReportRepo.Provider(ENV_ON_GO, workingDir).getExistingReports();
        testSuites = repoInFirstModule.getTestSuites();
        assertThat(testSuites.size(), is(5));
        assertThat(server.getServer().getRequestCount(), is(10));

        // Multiple accesses in same JVM should not check for new files
        testSuites = repoInFirstModule.getTestSuites();
        assertThat(testSuites.size(), is(5));
        assertThat(server.getServer().getRequestCount(), is(10));

        // Accesses across JVMs should check for new files, but shouldn't re-download files
        ReportRepo repoInSecondModule = new GoReportRepo.Provider(ENV_ON_GO, workingDir).getExistingReports();
        testSuites = repoInSecondModule.getTestSuites();
        assertThat(testSuites.size(), is(5));
        assertThat(server.getServer().getRequestCount(), is(15));
    }

    @Test
    public void failsUsefullyWhenTrustStoreIsMissing() throws Exception {
        agentBasedir.delete();

        exceptions.expectMessage(containsString("Couldn't find Go agent base directory (I expected it to be somewhere above"));
        new GoReportRepo.Provider(ENV_ON_GO, workingDir).getExistingReports();
    }

    @Test
    public void failsUsefullyWhenANetworkErrorHappens() throws Exception {
        server.enqueueSocketBehavior(DISCONNECT_AFTER_REQUEST);

        exceptions.expectCause(hasMessageThat(containsString("Connection to Go server failed")));
        new GoReportRepo.Provider(ENV_ON_GO, workingDir).getExistingReports().getTestSuites();
    }

    @Test
    public void failsUsefullyWhenStageHistoryListingFails() throws Exception {
        server.enqueueSocketBehavior(DISCONNECT_DURING_RESPONSE_BODY);

        exceptions.expectCause(hasMessageThat(containsString("Failed to download stage history for")));
        new GoReportRepo.Provider(ENV_ON_GO, workingDir).getExistingReports().getTestSuites();
    }

    @Test
    public void failsUsefullyWhenFileListingFails() throws Exception {
        server.enqueueSocketBehavior(KEEP_OPEN);
        server.enqueueSocketBehavior(DISCONNECT_DURING_RESPONSE_BODY);

        exceptions.expectCause(hasMessageThat(containsString("Failed to list files for")));
        new GoReportRepo.Provider(ENV_ON_GO, workingDir).getExistingReports().getTestSuites();
    }

    @Test
    public void failsUsefullyWhenDownloadFails() throws Exception {
        server.enqueueSocketBehavior(KEEP_OPEN);
        server.enqueueSocketBehavior(KEEP_OPEN);
        server.enqueueSocketBehavior(DISCONNECT_DURING_RESPONSE_BODY);

        exceptions.expectCause(hasMessageThat(containsString("Failed to download file from Go:")));
        new GoReportRepo.Provider(ENV_ON_GO, workingDir).getExistingReports().getTestSuites();
    }

    @Test
    public void failsUsefullyWhenGoIsDown() throws Exception {
        server.crash();

        exceptions.expectCause(hasMessageThat(matchesRegex("(?s:Bad status code.*500: Server Crashed.*Stack\nTrace\nHere.*)")));
        new GoReportRepo.Provider(ENV_ON_GO, workingDir).getExistingReports().getTestSuites();
    }

    @Test
    public void failsUsefullyWhenGoIsDownAndCantReadErrorPage() throws Exception {
        server.crash();
        server.enqueueSocketBehavior(DISCONNECT_DURING_RESPONSE_BODY);

        exceptions.expectCause(hasMessageThat(matchesRegex("(?s:Bad status code.*500: Server Crashed.*'unexpected end of stream'.*)")));
        new GoReportRepo.Provider(ENV_ON_GO, workingDir).getExistingReports().getTestSuites();
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void failsUsefullyWhenCantCreateDownloadCache() throws Exception {
        File pipelinesDir = getAgentDir("pipelines");
        pipelinesDir.setWritable(false);
        try {
            exceptions.expectMessage(containsString("Couldn't create download directory for reports from Go"));
            new GoReportRepo.Provider(ENV_ON_GO, workingDir).getExistingReports();
        } finally {
            pipelinesDir.setWritable(true);
        }
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void failsUsefullyWhenCantWriteToDownloadCache() throws Exception {
        ReportRepo reportRepo = new GoReportRepo.Provider(ENV_ON_GO, workingDir).getExistingReports();
        File downloadCache = getAgentDir("pipelines", ".go-downloads");
        downloadCache.setWritable(false);
        try {
            exceptions.expectCause(hasMessageThat(containsString("Failed to download file from Go")));
            reportRepo.getTestSuites();
        } finally {
            downloadCache.setWritable(true);
        }
    }

    private File getAgentDir(String... pathParts) {
        Path dir = agentBasedir.getRoot().toPath();
        for (String pathPart : pathParts) {
            dir = dir.resolve(pathPart);
        }
        return dir.toFile();
    }

    private Matcher<String> matchesRegex(final String regex) {
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String item) {
                return item.matches(regex);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("string matching regex ").appendValue(regex);
            }
        };
    }

    private Matcher<? extends Throwable> hasMessageThat(final Matcher<String> expectedMessage) {
        return new TypeSafeMatcher<Throwable>() {
            @Override
            protected boolean matchesSafely(Throwable item) {
                return expectedMessage.matches(item.getMessage());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has message that ").appendDescriptionOf(expectedMessage);
            }
        };
    }
}
