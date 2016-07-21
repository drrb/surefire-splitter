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

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StageHistory {

    public static StageHistory fromJson(String json) {
        return new Gson().fromJson(json, StageHistory.class);
    }

    private final List<StageResult> stages;

    public StageHistory(List<StageResult> stages) {
        this.stages = Collections.unmodifiableList(new ArrayList<>(stages));
    }

    public List<StageResult> getStages() {
        return stages;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
