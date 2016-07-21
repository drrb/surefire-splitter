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
package com.github.drrb.surefiresplitter.go.model;

public class JobRun {
    public final String pipelineName;
    public final String pipelineCounter;
    public final String stageName;
    public final String stageCounter;
    public final String jobName;

    public JobRun(String pipelineName, String pipelineCounter, String stageName, String stageCounter, String jobName) {
        this.pipelineName = pipelineName;
        this.pipelineCounter = pipelineCounter;
        this.stageName = stageName;
        this.stageCounter = stageCounter;
        this.jobName = jobName;
    }

    @Override
    public String toString() {
        return String.format("%s/%s/%s/%s/%s", pipelineName, pipelineCounter, stageName, stageCounter, jobName);
    }
}
