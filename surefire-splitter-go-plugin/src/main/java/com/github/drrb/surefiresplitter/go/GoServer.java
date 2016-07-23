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

import com.github.drrb.surefiresplitter.go.GoServer.DownloadCache.Download;
import com.github.drrb.surefiresplitter.go.model.*;
import com.github.drrb.surefiresplitter.go.util.Bytes;
import com.squareup.okhttp.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class GoServer {

    public static class CommunicationError extends Exception {
        public CommunicationError(String message) {
            super(message);
        }

        public CommunicationError(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final String baseUrl;
    private final String username;
    private final String password;
    private final OkHttpClient httpClient;
    private final DownloadCache downloadCache;
    private final int numberOfRunsToLookBackForReports;

    public GoServer(GoAgent config) {
        this.baseUrl = config.getGoServerUrl();
        this.numberOfRunsToLookBackForReports = config.getNumberOfRunsToLookBackForReports();
        this.downloadCache = DownloadCache.create(baseUrl, config.getPipelinesDir().resolve(".go-downloads"));
        this.username = config.getServerUsername();
        this.password = config.getServerPassword();
        this.httpClient = new GoServerTrustingHttpClient(config.getSslContext());
    }

    public List<JobRun> getPreviousJobRuns(JobRun mostRecentJobRun) throws CommunicationError {
        List<JobRun> previousJobRuns = new LinkedList<>();
        for (StageResult stageResult : getPreviousStageRuns(mostRecentJobRun.pipelineName, mostRecentJobRun.stageName)) {
            if (stageResult.isBefore(mostRecentJobRun.pipelineCounter, mostRecentJobRun.stageCounter)) {
                for (JobResult jobResult : stageResult.getJobs()) {
                    if (jobResult.isInstanceOf(mostRecentJobRun.jobName)) {
                        previousJobRuns.add(new JobRun(stageResult.pipelineName, stageResult.pipelineCounter, stageResult.stageName, stageResult.stageCounter, jobResult.jobName));
                    }
                }
            }
        }
        return previousJobRuns;
    }

    public List<Path> downloadFiles(JobRun jobRun, FilenameFilter filenameFilter) throws CommunicationError {
        GoFiles files = listFiles(jobRun);
        List<GoFile> filesToDownload = files.filter(filenameFilter);
        return downloadAll(filesToDownload);
    }

    private List<StageResult> getPreviousStageRuns(String pipelineName, String stageName) throws CommunicationError {
        try (ResponseBody response = get(url("/api/stages/%s/%s/history", pipelineName, stageName))) {
            List<StageResult> stages = StageHistory.fromJson(response.string()).getStages();
            if (stages.size() <= numberOfRunsToLookBackForReports) {
                return stages;
            } else {
                return stages.subList(0, numberOfRunsToLookBackForReports);
            }
        } catch (IOException e) {
            throw new CommunicationError(String.format("Failed to download stage history for %s/%s", pipelineName, stageName));
        }
    }

    private GoFiles listFiles(JobRun jobRun) throws CommunicationError {
        String url = url("/files/%s/%s/%s/%s/%s.json", jobRun.pipelineName, jobRun.pipelineCounter, jobRun.stageName, jobRun.stageCounter, jobRun.jobName);
        try (ResponseBody response = get(url)) {
            return GoFiles.fromJson(response.string());
        } catch (IOException e) {
            throw new CommunicationError("Failed to list files for job " + jobRun);
        }
    }

    private List<Path> downloadAll(List<GoFile> files) throws CommunicationError {
        List<Path> downloadedFiles = new LinkedList<>();
        for (GoFile file : files) {
            String url = file.getUrl();
            try {
                downloadedFiles.add(download(url));
            } catch (Download.Skipped skipped) {
                System.out.println("Download skipped: '" + url + "' (" + skipped.getMessage() + ")");
            }
        }
        return downloadedFiles;
    }

    private Path download(String url) throws CommunicationError, Download.Skipped {
        return downloadCache.getOrDownload(url, new Download() {

            @Override
            public void download(String url, Path downloadTarget) throws CommunicationError, Skipped {
                try (ResponseBody response = get(url)) {
                    Files.createDirectories(downloadTarget.getParent());
                    if (response.contentLength() > Bytes.of("4 MB")) {
                        throw new Skipped("File too large. Probably not actually a surefire report (" + Bytes.render(response.contentLength()) + ")");
                    }
                    Files.copy(response.byteStream(), downloadTarget, REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new CommunicationError("Failed to download file from Go: (" + url + " -> " + downloadTarget + ")", e);
                }
            }
        });
    }

    private ResponseBody get(String url) throws CommunicationError {
        Request.Builder requestBuilder = new Request.Builder().get().url(url);
        if (username != null && password != null) {
            requestBuilder.addHeader("Authorization", Credentials.basic(username, password));
        }
        Response response = execute(requestBuilder.build());
        if (response.isSuccessful()) {
            return response.body();
        } else {
            String errorMessage = String.format("Bad status code when requesting: %s (%d: %s)", url, response.code(), response.message());
            try (ResponseBody responseBody = response.body()) {
                errorMessage += ("\n" + responseBody.string());
            } catch (IOException e) {
                errorMessage += ". Tried to read the response body for a helpful message, but couldn't (Error was '" + e.getMessage() + "').";
            }
            throw new CommunicationError(errorMessage);
        }
    }

    private Response execute(Request request) throws CommunicationError {
        System.out.println(" -> " + request.url());
        try {
            Response response = httpClient.newCall(request).execute();
            System.out.println(" <- " + response.code() + ": " + response.message() + " (" + Bytes.render(response.body().contentLength()) + ")");
            return response;
        } catch (IOException e) {
            throw new CommunicationError("Connection to Go server failed", e);
        }
    }

    private String url(String format, Object... args) {
        return baseUrl.replaceFirst("/$", "") + String.format(format, args);
    }

    static class DownloadCache {

        public interface Download {
            class Skipped extends Exception {
                public Skipped(String message) {
                    super(message);
                }
            }

            void download(String url, Path downloadTarget) throws Skipped, CommunicationError;
        }

        public static DownloadCache create(String baseUrl, Path baseDir) {
            if (!Files.isDirectory(baseDir)) {
                try {
                    Files.createDirectories(baseDir);
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't create download directory for reports from Go at " + baseDir.toAbsolutePath(), e);
                }
            }
            return new DownloadCache(baseUrl, baseDir);
        }

        private final String baseUrl;
        private final Path baseDir;

        public DownloadCache(String baseUrl, Path baseDir) {
            this.baseUrl = baseUrl;
            this.baseDir = baseDir;
        }

        public Path getOrDownload(String url, Download download) throws CommunicationError, Download.Skipped {
            Path downloadTarget = getCachePath(url);
            if (!Files.exists(downloadTarget)) {
                download.download(url, downloadTarget);
            }
            return downloadTarget;
        }

        private Path getCachePath(String downloadUrl) {
            URI url = URI.create(downloadUrl);
            URI filesBaseUrl = URI.create(baseUrl + "files");
            String filePath = filesBaseUrl.relativize(url).getPath();
            String canonicalFilePath = filePath.replaceFirst("-runInstance-\\d+/", "/");
            return baseDir.resolve(canonicalFilePath);
        }
    }

    private static class GoServerTrustingHttpClient extends OkHttpClient {
        public GoServerTrustingHttpClient(SSLContext agentSslContext) {
            setFollowRedirects(false); // Otherwise we get a 200 if we're forwarded to the login page if the auth is bad
            setSslSocketFactory(agentSslContext.getSocketFactory()); // Trust the server's cert using the agent's trust store
            // Don't check the server's hostname: the GO_SERVER_URL env variable on the agent isn't necessarily the CN
            // in the server's certificate and we're pinning the certificate anyway
            setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hosName, SSLSession sslSession) {
                    return true;
                }
            });
        }
    }
}
