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

import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.util.Map;

import static java.nio.file.Files.isDirectory;

public class GoAgent {
    private final Map<String, String> env;
    private final Path baseDir;

    public GoAgent(Map<String, String> env, Path workingDir) {
        this.env = env;
        this.baseDir = findBaseDir(workingDir);
    }

    public Path getPipelinesDir() {
        return baseDir.resolve("pipelines");
    }

    public GoServer getGoServer() {
        return new GoServer(this);
    }

    public String getGoServerUrl() {
        return getEnvString("GO_SERVER_URL");
    }

    public String getPipelineName() {
        return getEnvString("GO_PIPELINE_NAME");
    }

    public String getPipelineCounter() {
        return getEnvString("GO_PIPELINE_COUNTER");
    }

    public String getStageName() {
        return getEnvString("GO_STAGE_NAME");
    }

    public String getStageCounter() {
        return getEnvString("GO_STAGE_COUNTER");
    }

    public String getJobName() {
        return getEnvString("GO_JOB_NAME");
    }

    public String getServerUsername() {
        return env.get("GO_USERNAME");
    }

    public String getServerPassword() {
        return env.get("GO_PASSWORD");
    }

    private String getEnvString(String envVar) {
        String value = env.get(envVar);
        if (value == null) {
            throw new IllegalStateException("Expected to find environment variable '" + envVar + "', but it wasn't there");
        }
        return value;
    }

    private static Path findBaseDir(Path fileUnderBaseDir) {
        Path potentialBaseDir = fileUnderBaseDir;
        while (!looksLikeAnAgentBaseDir(potentialBaseDir)) {
            potentialBaseDir = potentialBaseDir.getParent();
            if (potentialBaseDir == null) {
                throw new RuntimeException("Couldn't find Go agent base directory (I expected it to be somewhere above '" + fileUnderBaseDir + "'. I was looking for the 'pipelines' and 'config' directories in there)");
            }
        }
        System.out.println("Found agent base directory at " + potentialBaseDir.toAbsolutePath());
        return potentialBaseDir;
    }

    private static boolean looksLikeAnAgentBaseDir(Path maybeAgentBaseDir) {
        return isDirectory(maybeAgentBaseDir)
                && isDirectory(maybeAgentBaseDir.resolve("pipelines"))
                && isDirectory(maybeAgentBaseDir.resolve("config"));
    }

    public SSLContext getSslContext() {
        return new SslContextBuilder("to connect to the Go server")
                .withKeyStore(getAgentConfigFile("agent.jks"))
                .withKeyStorePassword("agent5s0repa55w0rd")
                .withTrustStore(getAgentConfigFile("trust.jks"))
                .withTrustStorePassword("agent5s0repa55w0rd")
                .build();
    }

    public JobRun getCurrentJobRun() {
        return new JobRun(getPipelineName(), getPipelineCounter(), getStageName(), getStageCounter(), getJobName());
    }

    private Path getAgentConfigFile(String fileName) {
        return baseDir.resolve("config").resolve(fileName);
    }
}
