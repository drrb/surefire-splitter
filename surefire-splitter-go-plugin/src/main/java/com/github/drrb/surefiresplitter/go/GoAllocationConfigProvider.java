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

import com.github.drrb.surefiresplitter.spi.AllocationConfigProvider;

import java.util.Map;

public class GoAllocationConfigProvider implements AllocationConfigProvider {

    private final Map<String, String> environmentVariables;

    @SuppressWarnings("unused") // Used by SurefireSplitter SPI
    public GoAllocationConfigProvider() {
        this(System.getenv());
    }

    GoAllocationConfigProvider(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public boolean isAvailable() {
        return getChunkNumber() != null && getTotalChunks() != null;
    }

    public Integer getChunkNumber() {
        return getEnvironmentVariableInt("GO_JOB_RUN_INDEX");
    }

    public Integer getTotalChunks() {
        return getEnvironmentVariableInt("GO_JOB_RUN_COUNT");
    }

    private Integer getEnvironmentVariableInt(String envVarName) {
        String envVar = environmentVariables.get(envVarName);
        return envVar == null ? null : Integer.parseInt(envVar);
    }

    public String getDescription() {
        if (isAvailable()) {
            return "Go Job Split splitter plugin : chunk " + getChunkNumber() + " of " + getTotalChunks();
        } else {
            return "Go Job Split splitter plugin";
        }
    }
}
