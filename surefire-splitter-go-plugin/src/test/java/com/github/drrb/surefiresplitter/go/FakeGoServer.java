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

import com.github.drrb.surefiresplitter.go.model.*;
import com.github.drrb.surefiresplitter.go.ssl.TestGoKeyStores;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import com.squareup.okhttp.mockwebserver.SocketPolicy;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.KeyStore;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.drrb.surefiresplitter.go.model.GoFile.FileType.file;
import static com.github.drrb.surefiresplitter.go.model.GoFile.FileType.folder;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.readAllBytes;
import static java.util.Arrays.asList;
import static java.util.Collections.reverse;
import static java.util.Collections.sort;

public class FakeGoServer extends TestWatcher {
    private final Queue<SocketPolicy> enqueuedSocketBehavior = new LinkedList<>();
    private final int port;
    private final MockWebServer server;
    private final TestGoKeyStores keyStores;
    private boolean crashed;

    public FakeGoServer(int port) {
        this.port = port;
        this.server = new MockWebServer();
        this.keyStores = TestGoKeyStores.get();
        server.useHttps(keyStores.getServerSslSocketFactory(), false);
        server.setDispatcher(new com.squareup.okhttp.mockwebserver.Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                return FakeGoServer.this.dispatch(request);
            }
        });
    }

    public MockWebServer getServer() {
        return server;
    }

    public FakeGoServer enqueueSocketBehavior(SocketPolicy behavior) {
        enqueuedSocketBehavior.add(behavior);
        return this;
    }

    public void downloadAgentKeyStores(Path agentConfigDir) {
        keyStores.setUpAgentKeyStores(agentConfigDir);
    }

    public void crash() {
        this.crashed = true;
    }

    @Override
    protected void starting(Description description) {
        start();
    }

    @Override
    protected void finished(Description description) {
        stop();
    }

    private void start() {
        try {
            server.start(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void stop() {
        try {
            server.shutdown();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MockResponse dispatch(RecordedRequest request) {
        if (this.crashed) {
            return response(500, "Server Crashed").setBody("Stack\nTrace\nHere");
        }
        if (!Objects.equals(request.getHeader("Authorization"), "Basic YWRtaW46YmFkZ2Vy")) {
            return response(401, "Unauthorized");
        }

        Matcher stageHistoryPatterMatcher = Pattern.compile("/api/stages/(\\w+)/(\\w+)/history").matcher(request.getPath());
        if (stageHistoryPatterMatcher.find()) {
            return serveStageHistory(stageHistoryPatterMatcher.group(1), stageHistoryPatterMatcher.group(2));
        }

        Matcher fileListPatternMatcher = Pattern.compile("/go/files/(\\w+/\\d+/\\w+/\\d+/\\w+(?:-runInstance-\\d+)?).json").matcher(request.getPath());
        if (fileListPatternMatcher.find()) {
            return serveJobDir(fileListPatternMatcher.group(1));
        }

        Matcher fileDownloadPatternMatcher = Pattern.compile("/go/files/(\\w+/\\d+/\\w+/\\d+/\\w+(?:-runInstance-\\d+)?)/(.*)").matcher(request.getPath());
        if (fileDownloadPatternMatcher.find()) {
            Path jobDir = Paths.get(fileDownloadPatternMatcher.group(1));
            Path jobFile = Paths.get(fileDownloadPatternMatcher.group(2));

            return serveJobFile(jobDir.resolve(jobFile));
        }

        return response(400, "Unrecognized path: " + request.getPath());
    }

    private MockResponse serveStageHistory(String pipelineName, String stageName) {
        List<StageResult> stages = new LinkedList<>();
        Path pipelineDir = getPipelinesDir().resolve(pipelineName);
        for (File pipelineRunDir : childrenInReverseOrder(pipelineDir)) {
            for (File stageRunDir : childrenInReverseOrder(pipelineRunDir.toPath().resolve(stageName))) {
                List<JobResult> jobs = new LinkedList<>();
                for (File jobDir : stageRunDir.listFiles()) {
                    JobResult jobResult = new JobResult(jobDir.getName());
                    jobs.add(jobResult);
                }
                StageResult stageResult = new StageResult(
                        pipelineName,
                        pipelineRunDir.getName(),
                        stageName,
                        stageRunDir.getName(),
                        jobs
                );
                stages.add(stageResult);
            }
        }
        StageHistory stageHistory = new StageHistory(stages);
        return response(200, "OK")
                .setHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(stageHistory.toJson());
    }

    private List<File> childrenInReverseOrder(Path dir) {
        List<File> pipelineRunDirs = new ArrayList<>(asList(dir.toFile().listFiles()));
        sort(pipelineRunDirs);
        reverse(pipelineRunDirs);
        return pipelineRunDirs;
    }

    private MockResponse serveJobDir(String jobDirPath) {
        try {
            Path jobDir = getPipelinesDir().resolve(jobDirPath);
            if (!Files.isDirectory(jobDir)) {
                return response(404, "Artifacts for pipeline not found at " + jobDirPath);
            }
            GoFilesCreatingFileVisitor visitor = new GoFilesCreatingFileVisitor(getUrl("/files/" + jobDirPath), jobDir);
            Files.walkFileTree(jobDir, visitor);
            GoFiles goFiles = visitor.getGoFiles();
            String responseBody = goFiles.toJson();
            return response(200, "OK")
                    .setBody(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
            return response(500, "Error: " + e.getMessage());
        }
    }

    private MockResponse serveJobFile(Path jobFile) {
        try {
            Path file = getPipelinesDir().resolve(jobFile);
            if (Files.exists(file)) {
                MockResponse response = response(200, "OK")
                        .setBody(new String(readAllBytes(file), UTF_8));
                if (file.toString().contains("TooLarge")) {
                    response.setHeader("Content-Length", "5000000");
                }
                return response;
            } else {
                return response(404, "Not found: " + jobFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return response(500, "Error: " + e.getMessage());
        }
    }

    private MockResponse response(int statusCode, Object message) {
        MockResponse response = new MockResponse().setStatus("HTTP/1.1 " + statusCode + " " + message);
        if (!enqueuedSocketBehavior.isEmpty()) {
            response.setSocketPolicy(enqueuedSocketBehavior.remove());
        }
        return response;
    }

    private URI getUrl(String path) {
        return server.url("/go" + path).uri();
    }

    private Path getPipelinesDir() {
        return Paths.get("src/test/resources/com/github/drrb/surefiresplitter/pipelines");
    }
}

class GoFilesCreatingFileVisitor extends SimpleFileVisitor<Path> {
    private final Map<Path, GoFiles> directories = new HashMap<>();
    private final URI baseUrl;
    private final Path root;

    public GoFilesCreatingFileVisitor(URI baseUrl, Path root) {
        this.baseUrl = baseUrl;
        this.root = root;
    }


    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.equals(root)) {
            directories.put(dir, new GoFiles());
        } else {
            GoFile goFile = toGoFile(dir);
            directories.get(dir.getParent()).add(goFile);
            directories.put(dir, goFile.getFiles());
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        directories.get(file.getParent()).add(toGoFile(file));
        return CONTINUE;
    }

    private GoFile toGoFile(Path path) {
        return new GoFile(
                Files.isDirectory(path) ? folder : file,
                path.getFileName().toString(),
                baseUrl.toString() + "/" + root.relativize(path)
        );
    }

    public GoFiles getGoFiles() {
        return directories.get(root);
    }
}
