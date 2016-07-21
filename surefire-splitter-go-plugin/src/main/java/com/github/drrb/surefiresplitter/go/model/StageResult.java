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

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Integer.parseInt;

public class StageResult {
    @SerializedName("pipeline_name")
    public final String pipelineName;

    @SerializedName("pipeline_counter")
    public final String pipelineCounter;

    @SerializedName("name")
    public final String stageName;

    @SerializedName("counter")
    public final String stageCounter;

    public final List<JobResult> jobs;

    public StageResult(String pipelineName, String pipelineCounter, String stageName, String stageCounter, List<JobResult> jobs) {
        this.pipelineName = pipelineName;
        this.pipelineCounter = pipelineCounter;
        this.stageName = stageName;
        this.stageCounter = stageCounter;
        this.jobs = Collections.unmodifiableList(new ArrayList<>(jobs));
    }

    public List<JobResult> getJobs() {
        return jobs;
    }

    public boolean isBefore(String pipelineCounter, String stageCounter) {
        if (parseInt(this.pipelineCounter) < parseInt(pipelineCounter)) {
            return true;
        } else if (parseInt(this.pipelineCounter) == parseInt(pipelineCounter)) {
            return parseInt(this.stageCounter) < parseInt(stageCounter);
        } else {
            return false;
        }
    }
}
